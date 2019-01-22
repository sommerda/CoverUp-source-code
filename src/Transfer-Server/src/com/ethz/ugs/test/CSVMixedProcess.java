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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;

/**
 * @author Aritra
 *
 */
public class CSVMixedProcess {
	
	public static void main(String[] args) throws Exception
	{
		BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\Aritra\\"
				+ "workspace_Mars_new\\deniableComChannel\\Measurements\\Data\\JS_new\\"
				+ "with extension\\data_125000_200_mixed_1475990931006.csv"));
		
		FileWriter fw_i = new FileWriter("C:\\Users\\Aritra\\"
				+ "workspace_Mars_new\\deniableComChannel\\Measurements\\Data\\JS_new\\with extension\\data_62500_200_int_" 
				+ System.currentTimeMillis() + ".csv");
		
		FileWriter fw_n = new FileWriter("C:\\Users\\Aritra\\"
				+ "workspace_Mars_new\\deniableComChannel\\Measurements\\Data\\JS_new\\with extension\\data_62500_200_noInt_" 
				+ System.currentTimeMillis() + ".csv");
		
		String str = null;
		
		int counter = 0;
		while((str = br.readLine()) != null)
		{
			if(counter % 2 == 0)
				fw_i.append(str + "\n");
			else
				fw_n.append(str + "\n");
			
			
			if(counter % 1000 == 0)
				System.out.println(counter);
			counter++;
		}
		
		fw_n.close();
		fw_i.close();
		br.close();
	}

}
