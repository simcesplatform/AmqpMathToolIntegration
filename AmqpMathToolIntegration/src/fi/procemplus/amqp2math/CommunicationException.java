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

package fi.procemplus.amqp2math;

/**
* Represents a communication failure.
* @author Petri Kannisto
*/
public class CommunicationException extends Exception
{
	// Eclipse/compiler thinks this variable should exist:
	private static final long serialVersionUID = -4811616337751651743L;
	

	/**
	 * Constructor.
	 * @param msg Error message.
	 * @param ie Inner exception.
	 */
	CommunicationException(String msg, Exception ie)
	{
		super(msg, ie);
		
		// Otherwise, empty ctor body
	}
}
