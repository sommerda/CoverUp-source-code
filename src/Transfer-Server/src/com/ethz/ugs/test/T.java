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
package com.ethz.ugs.test;

import java.io.FileWriter;
import java.util.Random;

/**
 * @author Aritra
 *
 */
public class T {
	
	public static void main(String[] args) throws Exception{
		
		Random rand = new Random();
		FileWriter fw = new FileWriter("gau.csv");
		
		for(int i = 0; i < 500000; i++)
			fw.append(Math.abs(Math.round(rand.nextGaussian() * 3 + 12)) + "\n");
		
		fw.close();
	}

}
