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
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import sun.misc.Unsafe;

public class LogReader {
	
    public static Unsafe getUnsafe() throws NoSuchFieldException, IllegalAccessException {
        try {

            Field singleoneInstanceField = Unsafe.class.getDeclaredField("theUnsafe");
            singleoneInstanceField.setAccessible(true);
            return (Unsafe) singleoneInstanceField.get(null);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (SecurityException e) {
            throw e;
        } catch (NoSuchFieldException e) {
            throw e;
        } catch (IllegalAccessException e) {
            throw e;
        }
    }

	
	public static void main(String[] args) throws IOException, NoSuchFieldException, IllegalAccessException {
		
		//getUnsafe();
		
		BufferedReader br = new BufferedReader(new FileReader("MainServer.log.8"));
		List<Long> el = new ArrayList<>();
		
		String st = null;
		long k = 0;
		long tot = 0;
		while((st = br.readLine()) != null)
		{
			//only consider sample size upto 50k
			if(k == 50000)
				break;
			
			if(!st.startsWith("INFO"))
				continue;
			if(st.length() == 0)
				continue;
			
			st = st.split(":")[2].trim().split(" ")[0].trim();
			k++;
			long l = Long.parseLong(st);
			tot += l;
			el.add(l);
			//System.out.println(l);
		}
		br.close();
		double mean = (double) tot/k;
		
		double s = 0;
		long min = el.get(0), max = 0;
		for(long i : el)
		{
			if(min >= i)
				min = i;
			
			if(max < i)
				max = i;
			s += Math.pow((mean - i), 2);
		}
		
		long blockLen = 500;
		int blockSize = (int) (((max - min) % blockLen == 0) ? ((max - min) / blockLen) : ((max - min) / blockLen) + 1);
		
		//System.out.println(blockSize);
		int[] block = new int[blockSize + 1];
		for(long i : el)
		{
			long diff = i - min;
			int pos = (int) ((diff % blockLen == 0) ? (diff / blockLen) : (diff / blockLen) + 1);
			//System.out.println(pos);
			block[pos]++;
			//System.out.println("---" + block[pos]);
		}
		
		FileWriter fw = new FileWriter("out.txt");
		for(int i : block)
		{
			fw.append(i + "\n");
		}	
		fw.close();
		
		FileWriter fw1 = new FileWriter("out1.txt");
		//float[] blockF = new float[block.length];
		for(int i : block)
		{
			float pr = (float )i/tot;
			fw1.append(String.format("%.30f", pr) + "\n");
		}	
		fw1.close();
		
		
		double var = (double) s/ (k-1);
		double sd = Math.sqrt(var);
		
		System.out.println("sample size : " + k);
		System.out.println("Mean : " + mean);
		System.out.println("sd : " + sd + " ns");
		
		System.out.println("Min : " + min);
		System.out.println("Max : " + max);
	}
	
	
}
