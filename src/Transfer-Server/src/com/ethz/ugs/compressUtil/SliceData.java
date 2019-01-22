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

package com.ethz.ugs.compressUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class SliceData {
	
	byte[] data;
	int chunk_size;
	int originalLength;
	
	List<byte[]> slicedData;
	public int sliceCount;
	
	public SliceData(byte[] data, int chunk_size)
	{
		this.data = data;
		//first 4 bytes are index value, next 4 byes num_chunks
		this.chunk_size = chunk_size - Integer.BYTES - Integer.BYTES;
		this.originalLength = data.length;
		this.slice();
	}
	
	private void slice()
	{
		int num_chunks = (this.data.length % chunk_size == 0) ? data.length / chunk_size : data.length / chunk_size + 1;
		this.slicedData = new ArrayList<>();
		
		byte[] num_chunksBytes = ByteBuffer.allocate(Integer.BYTES).putInt(num_chunks).array();
				
		for(int i = 0; i < num_chunks; i++)
		{
			byte[] tempChunk = new byte[this.chunk_size];
			
			if(i < num_chunks - 1 || data.length % chunk_size == 0)
				System.arraycopy(data, i * this.chunk_size, tempChunk, 0, this.chunk_size);
			else
			{
				int offSet = data.length % chunk_size;
				int toPad = chunk_size - offSet;
				byte[] pad = new byte[toPad];
				System.arraycopy(data, i * this.chunk_size, tempChunk, 0, offSet);
				System.arraycopy(pad, 0, tempChunk, offSet, pad.length);			
			}
			//first 4 bytes are index value
			byte[] sliceIndexBytes = ByteBuffer.allocate(Integer.BYTES).putInt(i).array();

			byte[] data = new byte[sliceIndexBytes.length + num_chunksBytes.length + tempChunk.length];
			System.arraycopy(sliceIndexBytes, 0, data, 0, sliceIndexBytes.length);
			System.arraycopy(num_chunksBytes, 0, data, sliceIndexBytes.length, num_chunksBytes.length);
			System.arraycopy(tempChunk, 0, data, sliceIndexBytes.length + num_chunksBytes.length, tempChunk.length);
			this.slicedData.add(data);
		}
		
		this.sliceCount = this.slicedData.size();
	}
	
	public List<byte[]> getAllSlices()
	{
		return this.slicedData;
	}
	
	public byte[] getSlice(int index)
	{
		if(index < 0)
			throw new IllegalArgumentException("Index < 0");
		if(index > this.slicedData.size())
			throw new IllegalArgumentException("Index higer than maximum");
		
		return this.slicedData.get(index);
	}
	
	public static void main(String[] args) throws IOException 
	{
		/*byte[] data = new byte[1000000000];
		new Random().nextBytes(data);
		
		SliceData sd = new SliceData(data, 128);
		sd.getSlice(1);*/
		
		BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\Aritra\\Desktop\\0.slice"));
		StringBuffer stb = new StringBuffer("");
		String st = null;
		
		while((st = br.readLine()) != null)
			stb.append(st);
		
		String x = new String(Base64.getDecoder().decode(stb.toString()), "UTF-8");
		
		System.out.println(x);
		br.close();
		
	}

}
