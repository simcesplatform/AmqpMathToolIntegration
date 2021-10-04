//
// Copyright (c) 2021 Tampere University
// MIT license
// Please make sure to read and understand the files README.md and LICENSE in the root of the repository.
// 
// This file was prepared in the research project ProCemPlus; https://www.senecc.fi/projects/procemplus
//
// Author: Petri Kannisto, Tampere University, Finland
// http://kannisto.org / petri.kannisto@tuni.fi
// File created: 11/2020
// Last modified: 11/2020

package fi.procemplus.amqp2math;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

/**
 * A synchronous AMQP connector class.
 * @author Petri Kannisto
 */
public class AmqpTopicConnectorSync
{
	// The topics being listened to.
	private final AmqpPropsManager m_amqpProperties;
	private final List<String> m_topics;
	
	private boolean m_objectAlreadyClosed = false;
	
	// The name of the currently active queue if any
	private String m_queueName = null;
	
	// Connection-related variables. These are synchronized with m_connLock.
	private Connection m_connection = null;
	private Channel m_channel = null;
	
	// This synchronizes all connection-related variables, so that
	// these cannot be assigned to or read when locked.
	private final Object m_connLock = new Object();
	
	// Whether the object assumes the connection is currently OK.
	// This requires synchronization, because the shutdown callback uses this.
	private boolean m_connSupposedlyConnected = false;
	
	
	
	/**
	 * Constructor.
	 * @param amqpProps Connection properties.
	 * @param topics Topics to listen to.
	 */
	public AmqpTopicConnectorSync(AmqpPropsManager amqpProps, String[] topics)
	{
		// This constructor overload exists because it presumably helps utilisation in Matlab.
		this(amqpProps, Arrays.asList(topics));
	}
	
	/**
	 * Constructor.
	 * @param amqpProps Connection properties.
	 * @param topics Topics to listen to.
	 */
	public AmqpTopicConnectorSync(AmqpPropsManager amqpProps, List<String> topics)
	{
		m_amqpProperties = amqpProps;
		m_topics = topics;
	}
	
	/**
	 * Opens the connection if not open.
	 * @throws CommunicationException Thrown if connecting fails.
	 */
	public void openConnectionIfNotOpen() throws CommunicationException
	{
		expectNotClosed();
		
		// Already connected?
		synchronized (m_connLock)
		{
			if (m_connSupposedlyConnected) return;
		}
		
		cleanConnectionRelatedObjects();
		setUpConnection(); // throws CommunicationException
		setUpQueue(); // throws CommunicationException
	}
	
	/**
	 * Closes the object. After this, the object can no longer be used.
	 */
	public void close()
	{
		m_objectAlreadyClosed = true;
		cleanConnectionRelatedObjects();
	}
	
	/**
	 * Gets a message received from one of the topics. If the connection is not open, there is
	 * an attempt to open it.
	 * @return Message, or null if nothing has been received from the topics.
	 * @throws CommunicationException Thrown if communication with the broker fails.
	 */
	public ReceivedMessage getMessage() throws CommunicationException
	{
		expectNotClosed();
		openConnectionIfNotOpen();
		
		boolean autoAck = true;
		
		try
		{
			GetResponse getResponse = m_channel.basicGet(m_queueName, autoAck);
			
			if (getResponse == null)
			{
				return null;
			}
			else
			{
				return new ReceivedMessage(getResponse.getEnvelope().getRoutingKey(), getResponse.getBody());
			}
		}
		catch (ShutdownSignalException | IOException e)
		{
			throw new CommunicationException("Failed to get message from queue: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Sends a message to given topic. If the connection is not open, there is
	 * an attempt to open it.
	 * @param topic Topic.
	 * @param msg Message.
	 * @throws CommunicationException Thrown if communication with the broker fails.
	 */
	public void sendMessage(String topic, byte[] msg) throws CommunicationException
	{
		expectNotClosed();
		openConnectionIfNotOpen();
		
		// Sending.
		// Use a TTL of 15 minutes for the messages.
		int ttlMilliseconds = 15 * 60 * 1000; // 15 minutes
		BasicProperties props = new BasicProperties().builder()
				.expiration(Integer.toString(ttlMilliseconds))
				.build();
		
		try
		{
			// TODO: This will not fail immediately after losing the connection.
			// It is unclear what happens to messages sent before the connection is
			// declared lost. :/
			m_channel.basicPublish(m_amqpProperties.getExchange(), topic, props, msg);
		}
		catch (ShutdownSignalException | IOException e)
		{
			throw new CommunicationException("Failed to send message: " + e.getMessage(), e);
		}
	}
	
	private void setUpConnection() throws CommunicationException
	{
		ConnectionFactory factory = new ConnectionFactory();
		
		try
		{
			if (m_amqpProperties.getSecure())
			{
				// Due to calling this function, no certificate verification will be performed
				factory.useSslProtocol();
			}
	        
			factory.setUri(m_amqpProperties.getUrl());
		}
		catch (KeyManagementException | NoSuchAlgorithmException | URISyntaxException e)
		{
			throw new CommunicationException("Connection setup failed: " + e.getMessage(), e);
		}
		
		// Cleaning old connection-related objects if these exist
		cleanConnectionRelatedObjects();
		
		try
		{
			// Opening a connection
			m_connection = factory.newConnection();
			m_channel = m_connection.createChannel();
			
			// Adding shutdown listeners
			ShutdownListener shutdownListener = new ShutdownListener()
			{				
				@Override
				public void shutdownCompleted(ShutdownSignalException arg0)
				{
					// Connection lost!
					synchronized (m_connLock)
					{
						m_connSupposedlyConnected = false; // TODO: Re-connect immediately
					}
				}
			};
			m_connection.addShutdownListener(shutdownListener);
			m_channel.addShutdownListener(shutdownListener);
			
			// Declaring the desired exchange
			m_channel.exchangeDeclare(m_amqpProperties.getExchange(), "topic",
			m_amqpProperties.getExchangeDurable(), m_amqpProperties.getExchangeAutoDelete(), null);
			
			synchronized (m_connLock)
			{
				m_connSupposedlyConnected = true;
			}
		}
		catch (TimeoutException | IOException e)
		{
			handleConnectError(e); // throws CommunicationException
		}
	}
	
	private void setUpQueue() throws CommunicationException
	{
		// TODO: If a queue already exists in the server, re-use it
		
		// Creating a queue
		String explicitName = ""; // Empty value; the name will be generated
        boolean durable = false; // The queue does not survive a broker restart
        boolean exclusive = true; // Exclusive to this app, delete on exit
        boolean autoDelete = true; // Delete the queue if no consumer uses it
		
        try
        {
	        String queueName = m_channel.queueDeclare(explicitName, durable, exclusive, autoDelete, null).getQueue();
			
			// Binding the queue to topics
			for (String t : m_topics)
			{
				m_channel.queueBind(queueName, m_amqpProperties.getExchange(), t);
			}
			
			m_queueName = queueName;
        }
        catch (IOException e)
        {
			throw new CommunicationException("Failed to set up message queue: " + e.getMessage(), e);
		}
	}
	
	private void expectNotClosed() throws RuntimeException
	{
		if (m_objectAlreadyClosed)
		{
			throw new RuntimeException("Cannot use connector because the user has closed it");
		}
	}
	
	private void handleConnectError(Exception e) throws CommunicationException
	{
		String msgStart = "Failed to create AmqpConnector";
		String errMsg = String.format("%s: %s: %s", msgStart, e.getClass().getSimpleName(), e.getMessage());
		printError(errMsg);
		
		if (e.getCause() != null)
		{
			Throwable cause = e.getCause();
			printError("-- Error cause: " + cause.getMessage());
		}
		
		throw new CommunicationException(msgStart, e);
	}
	
	private void printError(String msg)
	{
		String fullMsg = getMessageForPrint("ERR", msg);
		System.err.println(fullMsg);
	}
	
	private String getMessageForPrint(String tag, String msg)
	{
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
		String timeString = LocalTime.now().format(formatter);
		return String.format("%s [AmqpTopicConnectorSync] (%s) %s", timeString, tag, msg);
	}
	
	private void cleanConnectionRelatedObjects()
	{
		// Cleaning up AMQP resources
		
		try
		{
			// Cleaning up AMQP resources
			if (m_channel != null)
			{
				m_channel.close();
			}
		}
		catch (ShutdownSignalException | IOException | TimeoutException e)
		{
			// No can do!
		}
		
		m_channel = null;
		
		try
		{
			// Cleaning up AMQP resources
			if (m_connection != null)
			{
				m_connection.close();
			}
		}
		catch (ShutdownSignalException | IOException e)
		{
			// No can do!
		}
		
		m_connection = null;
	}
}
