//
// Copyright (c) 2021 Tampere University
// MIT license
// Please make sure to read and understand the files README.md and LICENSE in the root of the repository.
// 
// This file was prepared in the research project ProCemPlus.
//
// Author: Petri Kannisto, Tampere University, Finland
// http://kannisto.org / petri.kannisto@tuni.fi
// File created: 11/2020
// Last modified: 11/2020

package fi.procemplus.amqp2mathtest;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import fi.procemplus.amqp2math.AmqpPropsManager;
import fi.procemplus.amqp2math.AmqpTopicConnectorSync;
import fi.procemplus.amqp2math.CommunicationException;
import fi.procemplus.amqp2math.ReceivedMessage;

public class TestLogic
{
	// Constants
	private final String CLASS_NAME = "TestLogic";
	private final String TOPIC_A = "topic_a";
	private final String TOPIC_B = "topic_b";
	private final Charset EXPECTED_CHARSET = StandardCharsets.UTF_8;
	
	private final Ui m_ui;
	
	private boolean m_alreadyClosed = false;
	private AmqpTopicConnectorSync m_connectorSend = null;
	private AmqpTopicConnectorSync m_connectorRecv = null;
	
	
	/**
	 * Constructor.
	 * @param ui User interface.
	 */
	public TestLogic(Ui ui)
	{
		m_ui = ui;
	}
	
	/**
	 * Releases all the resources of the object.
	 */
	public void close()
	{
		m_alreadyClosed = true;
		
		// Cleaning up resources
		try
		{
			if (m_connectorSend != null)
			{
				m_connectorSend.close();
				m_connectorSend = null;
			}
			if (m_connectorRecv != null)
			{
				m_connectorRecv.close();
				m_connectorRecv = null;
			}
		}
		catch (Exception e)
		{
			// No can do!
		}
	}
	
	/**
	 * Runs the test logic.
	 * @throws IOException Thrown if the math tool connector fails.
	 */
	public void run() throws IOException
	{
		if (m_alreadyClosed)
		{
			throw new IllegalStateException("Test logic object already closed");
		}
		
		myPrintMessage("Started.");
		
		// Creating connectors...
		if (!createAllConnectorsAndOpen())
		{
			return;
		}
		
		while (true)
		{
			myPrintMessage("q: quit; r: check for receives msgs; s: send");
			String userInput = m_ui.promptUserInput("Input", "");
			
			if (userInput.equals("q"))
			{
				break;
			}
			else if (userInput.equals("s"))
			{
				send();
			}
			else if (userInput.equals("r"))
			{
				receive();
			}
		}
		
		myPrintMessage("Now quitting.");
	}
	
	private void send()
	{
		// Sending some messages
        try
        {
			sendToTopic(TOPIC_A);
			sendToTopic(TOPIC_B);
        }
        catch (CommunicationException e)
        {
			myPrintError("Failed to send: " + e.getMessage());
		}
	}
	
	private void sendToTopic(String topic) throws CommunicationException
	{
		Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        String messageString = topic + " at " + dateFormat.format(calendar.getTime());
        
		m_connectorSend.sendMessage(topic, messageString.getBytes(EXPECTED_CHARSET));
		myPrintMessage("Sent message: " + messageString);
	}
	
	private void receive()
	{
		ReceivedMessage msg = null;
		
		try
		{
			// Seeing if any messages have been received
			msg = m_connectorRecv.getMessage();
		}
		catch (CommunicationException e)
		{
			myPrintError("Failed to check for received messages: " + e.getMessage());
			return;
		}
		
		if (msg == null)
		{
			myPrintMessage("There were no received messages.");
			return;
		}
		
		String msgBodyString = new String(msg.getBody(), EXPECTED_CHARSET);
		myPrintMessage("From " + msg.getRoutingKey() + ", got: " + msgBodyString);
	}
	
	private boolean createAllConnectorsAndOpen()
	{
		// Return connection params if connecting succeeds. Otherwise, return null.
		
		// Get user input to connect
		
		String host = m_ui.promptUserInput("Host", "localhost");
		boolean isSecure = getBoolFromUserInput(m_ui.promptUserInput("Secure connection? 'y' for true, any other for not", ""));
		
		String defaultPort = isSecure ? "5671" : "5672";
		int port = Integer.parseInt(m_ui.promptUserInput("Port", defaultPort));
		
		String exchange = m_ui.promptUserInput("Exchange", "my.exchange");
		boolean isDurableExchange = getBoolFromUserInput(m_ui.promptUserInput("Durable exchange? 'y' for true, any other for false", ""));
		boolean isAutoDeleteExchange = getBoolFromUserInput(m_ui.promptUserInput("Auto delete exchange? 'y' for auto delete, any other for not", ""));
		String username = m_ui.promptUserInput("Username", "guest");
		
        while (true)
        {
        	// Get password. This is inside the loop, so there is a chance to retry.
        	String password = m_ui.promptUserInput("Password", "guest");
        	
        	AmqpPropsManager amqpProps = new AmqpPropsManager(host, exchange, username, password);
        	amqpProps.setSecure(isSecure);
        	amqpProps.setPort(port);
        	amqpProps.setExchangeDurable(isDurableExchange);
        	amqpProps.setExchangeAutoDelete(isAutoDeleteExchange);
        	
        	myPrintMessage("Creating connectors and opening connections...");
        	
        	try
        	{
        		// Trying to connect
        		ArrayList<String> topicsToListen = new ArrayList<>();
        		topicsToListen.add(TOPIC_A);
        		topicsToListen.add(TOPIC_B);
        		m_connectorRecv = createConnectorAndOpen(amqpProps, topicsToListen);
        		m_connectorSend = createConnectorAndOpen(amqpProps, new ArrayList<String>());
        		
        		myPrintMessage("Connectors created and opened.");
        		return true;
        	}
        	catch (IOException e)
        	{
        		myPrintError("Connecting failed.");
        		myPrintError("Error message: " + e.getMessage());
        		
        		// Retry connection?
        		myPrintMessage("Retry with another password?");
        		String userInputForRetry = m_ui.readUserInput("'y' to retry, any other to quit");
        		
        		if (!userInputForRetry.trim().toLowerCase().equals("y"))
        		{
        			return false; // Do not retry
        		}
			}
        }
	}
	
	private AmqpTopicConnectorSync createConnectorAndOpen(AmqpPropsManager props, List<String> topicsToListen) throws IOException
	{
		AmqpTopicConnectorSync retval = new AmqpTopicConnectorSync(props, topicsToListen);
		
		try
		{
			retval.openConnectionIfNotOpen();
		}
		catch (CommunicationException e)
		{
			retval.close();
			throw new IOException("Failed to connect: " + e.getMessage(), e);
		}
		
		return retval;
	}
	
	private boolean getBoolFromUserInput(String input)
	{
		return input.trim().toLowerCase().equals("y");
	}
	
	private void myPrintMessage(String msg)
	{
		m_ui.printMessage(msg, CLASS_NAME);
	}
	
	private void myPrintError(String msg)
	{
		m_ui.printError(msg, CLASS_NAME);
	}
}
