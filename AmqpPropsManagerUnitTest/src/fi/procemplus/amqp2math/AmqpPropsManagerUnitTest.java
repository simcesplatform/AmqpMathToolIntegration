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

import static org.junit.Assert.*;

import org.junit.Test;

public class AmqpPropsManagerUnitTest
{
	// *** Secure URLs ***
	
	@Test
	public void defaultValues()
	{
		AmqpPropsManager testObject = new AmqpPropsManager("1.2.3.4", "foo", "user", "password");
		
		// Assert the URL
		assertEquals("amqps://user:password@1.2.3.4:5671", testObject.getUrl());
	}
	
	@Test
	public void customPortOtherwiseDefault()
	{
		AmqpPropsManager testObject = new AmqpPropsManager("1.2.3.4", "foo", "user", "password");
		testObject.setPort(123);
		
		// Assert the URL
		assertEquals("amqps://user:password@1.2.3.4:123", testObject.getUrl());
	}
	
	
	// *** Non-secure URLs ***
	
	@Test
	public void notSecureOtherwiseDefault()
	{
		AmqpPropsManager testObject = new AmqpPropsManager("1.2.3.5", "foo", "user", "password");
		testObject.setSecure(false);
		
		// Assert the URL
		assertEquals("amqp://user:password@1.2.3.5:5672", testObject.getUrl());
	}
	
	@Test
	public void notSecureCustomPort()
	{
		AmqpPropsManager testObject = new AmqpPropsManager("1.2.3.5", "foo", "user", "password");
		testObject.setSecure(false);
		testObject.setPort(124);
		
		// Assert the URL
		assertEquals("amqp://user:password@1.2.3.5:124", testObject.getUrl());
	}
	
	
	// *** Flags ***
	
	@Test
	public void exchangeDurableFlag()
	{
		AmqpPropsManager testObject = new AmqpPropsManager("1.2.3.4", "foo", "user", "password");
		
		// The default values of all flags
		assertFalse(testObject.getExchangeAutoDelete());
		assertFalse(testObject.getExchangeDurable());
		
		// Changing the value
		testObject.setExchangeDurable(true);
		
		// Assert values of flags (make sure the other did not change)
		assertFalse(testObject.getExchangeAutoDelete());
		assertTrue(testObject.getExchangeDurable());
		
		// Changing the value
		testObject.setExchangeDurable(false);
		
		// Assert values
		assertFalse(testObject.getExchangeAutoDelete());
		assertFalse(testObject.getExchangeDurable());
	}
	
	@Test
	public void exchangeAutoDeleteFlag()
	{
		AmqpPropsManager testObject = new AmqpPropsManager("1.2.3.4", "foo", "user", "password");
		
		// The default values of all flags
		assertFalse(testObject.getExchangeAutoDelete());
		assertFalse(testObject.getExchangeDurable());
		
		// Changing the value
		testObject.setExchangeAutoDelete(true);
		
		// Assert values of flags (make sure the other did not change)
		assertFalse(testObject.getExchangeDurable());
		assertTrue(testObject.getExchangeAutoDelete());
		
		// Changing the value
		testObject.setExchangeAutoDelete(false);
		
		// Assert values
		assertFalse(testObject.getExchangeAutoDelete());
		assertFalse(testObject.getExchangeDurable());
	}
}
