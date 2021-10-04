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


/**
 * Represents a received message.
 * @author Petri Kannisto
 */
public class ReceivedMessage
{
	private final String m_routingKey;
	private final byte[] m_body;
	
	
	/**
	 * Constructor.
	 * @param rkey Routing key.
	 * @param body Message body.
	 */
	ReceivedMessage(String rkey, byte[] body)
	{
		m_routingKey = rkey;
		m_body = body;
	}
	
	/**
	 * Returns the routing key.
	 * @return Routing key.
	 */
	public String getRoutingKey()
	{
		return m_routingKey;
	}
	
	/**
	 * Returns the message body.
	 * @return Message body.
	 */
	public byte[] getBody()
	{
		return m_body;
	}
}
