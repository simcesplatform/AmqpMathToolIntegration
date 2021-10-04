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
 * Holds message bus properties.
 * @author Petri Kannisto
 */
public class AmqpPropsManager
{
	// Constants
	private static final int defaultPortSecure = 5671;
	private static final int defaultPortNotSecure = 5672;
	private static final int portUnspecified = -1;
	
	private final String m_host;
	private final String m_exchange;
	private final String m_username;
	private final String m_password;
	
	private boolean m_secure = true; // Secure by default
	private boolean m_exchangeDurable = false; // not durable by default
	private boolean m_exchangeAutoDelete = false; // no autodelete by default
	
	// This indicates the port if set explicitly. Otherwise, a default port is assumed.
	private int m_explicitPort = portUnspecified;
	
	
	
	/**
	 * Constructor.
	 * @param host Host.
	 * @param excName Exchange name.
	 * @param user Username.
	 * @param pwd Password.
	 */
	public AmqpPropsManager(String host, String excName, String user, String pwd)
	{
		m_host = host;
		m_exchange = excName;
		m_username = user;
		m_password = pwd;
	}
	
	
	// *** Getters ***
	
	/**
	 * Sets AMQP server URL.
	 */
	String getUrl()
	{
		int port = m_explicitPort;
		
		// Use default port?
		if (m_explicitPort == portUnspecified)
		{
			if (m_secure)
			{
				port = defaultPortSecure;
			}
			else
			{
				port = defaultPortNotSecure;
			}
		}
		
		// Choosing URI scheme
		String scheme = m_secure ? "amqps" : "amqp";
		
		// Building the URI
		return String.format("%s://%s:%s@%s:%d", scheme, m_username, m_password, m_host, port);
	}
	
	/**
	 * Sets the exchange on the AMQP server.
	 */
	String getExchange()
	{
		return m_exchange;
	}
	
	
	// *** Setters ***
	
	/**
	 * Sets the AMQP server port. If not set, a default port will be chosen.
	 * The default depends on whether the connection is secure or not.
	 * @param i Port number.
	 */
	public void setPort(int i)
	{
		m_explicitPort = i;
	}
	
	/**
	 * Sets whether a secure connection is used. The default is "true".
	 * @param sec True if secure, otherwise false.
	 */
	public void setSecure(boolean sec)
	{
		m_secure = sec;
	}
	
	/**
	 * Gets whether a secure connection is used. The default is "true".
	 * @return True if secure, otherwise false.
	 */
	boolean getSecure()
	{
		return m_secure;
	}
	
	/**
	 * Sets whether the exchange shall be durable (i.e., survive a broker
	 * restart).
	 * @param dur True if durable, otherwise false.
	 */
	public void setExchangeDurable(boolean dur)
	{
		m_exchangeDurable = dur;
	}
	
	/**
	 * Gets whether the exchange shall be durable (i.e., survive a broker
	 * restart).
	 * @return True if durable, otherwise false.
	 */
	boolean getExchangeDurable()
	{
		return m_exchangeDurable;
	}
	
	/**
	 * Gets whether the exchange shall apply "auto delete" (i.e., be
	 * automatically deleted when all queues have been deleted).
	 * @param aut True if enabled, otherwise false.
	 */
	public void setExchangeAutoDelete(boolean aut)
	{
		m_exchangeAutoDelete = aut;
	}
	
	/**
	 * Sets whether the exchange shall apply "auto delete" (i.e., be
	 * automatically deleted when all queues have been deleted).
	 * @return True if enabled, otherwise false.
	 */
	boolean getExchangeAutoDelete()
	{
		return m_exchangeAutoDelete;
	}
}
