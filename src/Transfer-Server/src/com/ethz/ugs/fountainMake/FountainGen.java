//*************************************************************************************
//*********************************************************************************** *
//author Aritra Dhar 																* *
//PhD Researcher																  	* *
//ETH Zurich													   				    * *
//Zurich, Switzerland															    * *
//--------------------------------------------------------------------------------- * * 
///////////////////////////////////////////////// 									* *
//This program is meant to do world domination... 									* *
///////////////////////////////////////////////// 									* *
//*********************************************************************************** *
//*************************************************************************************

package com.ethz.ugs.fountainMake;

import java.io.File;

public class FountainGen {
	
	public static boolean fountain_generated = false;
	
	File[] files;
	
	public FountainGen(String directoryLocation)
	{
		File dir = new File(directoryLocation);
		if(!dir.isDirectory())
			throw new RuntimeException("Invalid argument");
		
		this.files = dir.listFiles();
	}
	public FountainGen(File[] files) 
	{
		this.files = files;
	}
	
	
}
