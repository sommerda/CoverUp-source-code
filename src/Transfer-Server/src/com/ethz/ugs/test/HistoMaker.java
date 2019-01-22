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
import java.util.ArrayList;
import java.util.List;

/**
 * @author Aritra
 *
 */
public class HistoMaker {
	
	
	public static void main(String[] args) throws Exception{
		
		String fileName = "csv_data//windows_loading_clevo//withExt_merge.pcap.dpkt.csv";
		String histoName = fileName + ".histo";
		
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		String str = null;
		List<Double> scores1 = new ArrayList<>();
		
		double min = Double.MAX_VALUE, max = 0;
		
		while((str = br.readLine()) != null)
		{
			if(str.length() == 0)
				continue;
			try
			{
				double data = Double.parseDouble(str); 
				scores1.add(data);
				if(min >= data)
					min = data;

				if(max < data)
					max = data;
			}
			catch(NumberFormatException ex)
			{
				continue;
			}
		}
		br.close();
		
		//min= 
		System.out.println("Min : " + min);
		System.out.println("Max : " + max);
		
		
		
		int bucketLen = 1;
		int bucketCount = (int) (((max - min) % bucketLen == 0) ? ((max - min) / bucketLen) : ((max - min) / bucketLen) + 1);

		System.out.println(bucketCount);
		int[] bucket = new int[bucketCount + 1];
		for(double i : scores1)
		{
			double diff = i - min;
			int pos = (int) ((diff % bucketLen == 0) ? (diff / bucketLen) : (diff / bucketLen) + 1);
			bucket[pos]++;
		}
		
		for(int buck : bucket)
			System.out.println(buck);
	}

}
