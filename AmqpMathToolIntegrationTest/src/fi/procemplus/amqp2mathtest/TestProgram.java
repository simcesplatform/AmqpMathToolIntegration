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

package fi.procemplus.amqp2mathtest;

/**
 * Entry point for the test program.
 * @author Petri Kannisto
 */
public class TestProgram
{
	public static void main(String[] args)
	{
		Ui ui = null;
		TestLogic testLogic = null;
		
		try
		{
			// Create objects
			ui = new Ui();
			testLogic = new TestLogic(ui);
			
			// Run test logic
			testLogic.run();
		}
		catch (Exception e)
		{
			e.printStackTrace(System.err);
		}
		finally
		{
			// Perform cleanup
			if (testLogic != null)
			{
				testLogic.close();
				testLogic = null;
			}
			if (ui != null)
			{
				ui.close();
				ui = null;
			}
		}
	}
}
