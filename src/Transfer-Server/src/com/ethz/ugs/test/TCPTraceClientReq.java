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

import jdk.jfr.events.FileWriteEvent;

/**
 * @author Aritra
 *
 */
public class TCPTraceClientReq {

	public static void main(String[] args) throws Exception{

		//browser to tildem

		BufferedReader br = new BufferedReader(new FileReader("Traces\\TCP trace\\2.txt"));
		FileWriter fw = new FileWriter("Traces\\TCP trace\\2.log");
		double val = 0.0d;
		String str = null;
		int i = 0;
		while((str = br.readLine())!= null)
		{
			str.replaceAll("	", "");

			if(str.contains("TLS	TLS:TLS Rec Layer-1 SSL Application Data"))
			{
				double temp = Double.parseDouble(str.split(" ")[2].trim().replaceAll("	", " ").split(" ")[1]);
				//System.out.println((temp - val));
				if(temp-val > 2 || temp-val < 0.8)
					continue;
				fw.append(String.format("%7f",(temp - val)) + "\n");
				val = temp;
				
				//i++;
				//System.out.println(str.split(" ")[2].trim().replaceAll("	", " ").split(" ")[1]);
			}
		}
		
		//System.out.println(i);
		br.close();
		fw.close();
	}

}
