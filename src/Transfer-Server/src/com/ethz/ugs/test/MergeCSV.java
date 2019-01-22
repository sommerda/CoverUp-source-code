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
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

/**
 * @author Aritra
 *
 */
public class MergeCSV {
	public static void main(String[] args) throws Exception {
		File f1 = new File("C:\\Users\\Aritra\\workspace_Mars_new\\DeniableCommChannel\\Measurements\\Data\\JS_new\\nw noise\\Large Data Set\\int");
		File f2 = new File("C:\\Users\\Aritra\\workspace_Mars_new\\DeniableCommChannel\\Measurements\\Data\\JS_new\\nw noise\\Large Data Set\\no_int");
		
		File[] f_1 = f1.listFiles();
		File[] f_2 = f2.listFiles();
		
		FileWriter fw_1 = new FileWriter("C:\\Users\\Aritra\\workspace_Mars_new\\DeniableCommChannel\\Measurements\\Data\\JS_new\\nw noise\\Large Data Set\\int\\m1.csv");
		FileWriter fw_2 = new FileWriter("C:\\Users\\Aritra\\workspace_Mars_new\\DeniableCommChannel\\Measurements\\Data\\JS_new\\nw noise\\Large Data Set\\no_int\\m1.csv");
		
		for(File file : f_1)
		{
			BufferedReader br = new BufferedReader(new FileReader(file));
			String str = null;
			while((str = br.readLine()) != null)
			{
				if(str.length() == 0)
					continue;
				fw_1.append(str + "\n");
			}
			br.close();
		}
		
		for(File file : f_2)
		{
			BufferedReader br = new BufferedReader(new FileReader(file));
			String str = null;
			while((str = br.readLine()) != null)
			{
				if(str.length() == 0)
					continue;
				fw_2.append(str + "\n");
			}
			br.close();
		}
		
		fw_1.close();
		fw_2.close();
	}

}
