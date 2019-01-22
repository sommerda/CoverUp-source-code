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

package com.ethz.fountain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Glass {
	
	
	int num_chunks;
	List<Droplet> entries;
	public static byte[][] chunks;
	
	public Glass(int num_chunks)
	{
		this.entries = null;
		this.num_chunks = num_chunks;
		chunks = new byte[this.num_chunks][];
	}
	
	public void addDroplet(Droplet d)
	{			
		if(entries == null)
			entries = new ArrayList<>();
				
		entries.add(d);			
		this.updateEntry(d);
		
	}
	
	public void updateEntry(Droplet d)
	{
		Set<Integer> indexSet = new HashSet<>();
		
		for(int chunk_num : d.chunkNums)
		{		
			if(chunks[chunk_num] != null)
			{
				d.data = Util.xorBytes(d.data, chunks[chunk_num]);
				indexSet.add(chunk_num);
			}
		}
		
		for(Integer index : indexSet)
			d.chunkNums.remove(index);
		
		if(d.chunkNums.size() == 1)
		{
			chunks[d.chunkNums.get(0)] = d.data;
			
			int temp = d.chunkNums.get(0);
			d.chunkNums.remove(0);
			
			for(int i = 0; i < this.entries.size(); i++)
			{
				if(entries.get(i).chunkNums.contains(temp))				
					this.updateEntry(entries.get(i));
			}
		}
		
		if(d.chunkNums.size() == 0)
			this.entries.remove(d);	
	}
	
	public boolean isDone()
	{
		for(byte[] b : Glass.chunks)
			if(b == null)
				return false;
			
		return true;
	}
	
	public int chunksDone()
	{
		int count = 0;
		
		for(byte[] b : Glass.chunks)
			if(b == null)
				count++;
		
		return count;
		
	}
	
	public byte[] getDecodedData()
	{
		byte[] decodedData = new byte[Glass.chunks.length * Glass.chunks[0].length];
		Arrays.fill(decodedData, (byte) 0x00);
		if(this.isDone())
		{
			int lenTillNow = 0;
			for(int i = 0 ; i < Glass.chunks.length; i++)
			{
				System.arraycopy(Glass.chunks[i], 0, decodedData, lenTillNow, Glass.chunks[i].length);
				lenTillNow += Glass.chunks[i].length;
			}
		}
		
		return decodedData;	
	}
	
}
