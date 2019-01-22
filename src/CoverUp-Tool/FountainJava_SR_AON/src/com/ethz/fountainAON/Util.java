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

package com.ethz.fountainAON;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class Util {

	public static final int KEY_LENGTH = 16;
	public static final int IV_LENGTH = 16;

	public static List<Integer> randChunkNums(SecureRandom rand, int num_chunks)
	{	
		num_chunks = (num_chunks == 1) ? 2 : num_chunks;
		
		int size = rand.nextInt(Math.min(4, num_chunks - 1 )) + 1;
		 
		List<Integer> list = new ArrayList<>();

		for(int i = 0 ; i < num_chunks; i ++)
			list.add(i);

		Collections.shuffle(list, rand);
		return list.subList(0, size);
	}

	public static byte[] xorBytes(byte[] a1, byte[] a2)
	{
		if(a1 == null || a2 == null)
			throw new IllegalArgumentException("Null argument");
		
		byte[] a3 = new byte[(a1.length >= a2.length) ? a1.length : a2.length];
		
		if(a1.length >= a2.length)
			System.arraycopy(a1, 0, a3, 0, a1.length);
		else
			System.arraycopy(a2, 0, a3, 0, a2.length);

		for(int i = 0; i < Math.min(a1.length, a2.length); i ++)
			a3[i] = (byte) (a1[i] ^ a2[i]);

		return a3;
	}

}
