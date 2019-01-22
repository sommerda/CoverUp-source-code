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
public class Log2CSV {
	
	public static void main(String[] args) throws Exception{
		
		BufferedReader br = new BufferedReader(new FileReader("P:\\TCP Dumps\\ServerTime\\interactive_2.log"));
		FileWriter fw = new FileWriter("P:\\TCP Dumps\\ServerTime\\interactive_2.csv");
		String str = null;
		
		while ((str = br.readLine()) != null) {
			if(str.contains("INFO"))
			{
				str = str.split(":")[2].trim().replaceAll("ms", "").trim();
				fw.append(str + "\n");
				//System.out.println(str);
			}
			
		}
		
		fw.close();
		br.close();
		
		
		BufferedReader br1 = new BufferedReader(new FileReader("P:\\TCP Dumps\\ServerTime\\interactive_2.csv"));
		FileWriter fw1 = new FileWriter("P:\\TCP Dumps\\ServerTime\\d4.csv");
		
		int tot = 0;
		float []A = new float [100]; 
		while ((str = br1.readLine()) != null) {
			
			if(str.length() == 0)
				continue;
			
			//if(Integer.parseInt(str) > 55)
			//	continue;
			
			A[Integer.parseInt(str)]++;	
			tot++;
		}
		
		fw1.append("t v\n");
		for(int i = 40; i < 60; i++)
		{
			//if(A[i] == 0)
			//	continue;
			
			A[i] = (float) (A[i]/tot);
			System.out.println(i + " " + A[i]);
			fw1.append(i +" " +A[i]+  "\n");
		}
		fw1.close();
		br1.close();	
	}
	

}
