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
public class MixedLogProcess {

	public static void main(String[] args) throws Exception {

		BufferedReader br = new BufferedReader(new FileReader("Traces\\bigTrace\\MainServer.log.m.12"));
		String str = null;

		FileWriter fwNI = new FileWriter("Traces\\bigTrace\\noInt.log.12");
		FileWriter fwI = new FileWriter("Traces\\bigTrace\\int.log.12");
		int counter = 0, i = 0;
		while((str = br.readLine()) != null)
		{
			
			if(counter % 1000 == 0)
				System.out.println(counter);
			i = counter % 4;

			switch (i) {
			case 0:
				fwI.append(str + "\n");
				break;

			case 1:
				fwI.append(str + "\n");
				break;

			case 2:
				fwNI.append(str + "\n");
				break;

			case 3:
				fwNI.append(str + "\n");
				break;
			default:
				break;
			}
			
			counter++;
		}
		fwI.close();
		fwNI.close();
		br.close();
	}

}
