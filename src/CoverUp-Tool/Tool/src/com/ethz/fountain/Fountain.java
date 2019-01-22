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

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;

public class Fountain {
	
	public byte[] data;
	public int chunk_size;
	public byte[] seed;
	public int num_chunks;
	public SecureRandom rand;
	public int dataLenBeforPadding;
	
	List<byte[]> chunks;
	
	public Fountain(byte[] data, int chunk_size, byte[] seed) throws NoSuchAlgorithmException, NoSuchProviderException
	{
		this.data = data;
		this.dataLenBeforPadding = data.length;
		this.chunk_size = chunk_size;
		this.num_chunks = (data.length % chunk_size == 0) ? data.length/chunk_size : data.length/chunk_size + 1;
		chunks = new ArrayList<>();
		
		for(int i = 0; i < this.num_chunks; i++)
		{
			byte[] tempChunk = new byte[this.chunk_size];
			
			if(i < this.num_chunks - 1 || data.length % chunk_size == 0)
				System.arraycopy(data, i * this.chunk_size, tempChunk, 0, this.chunk_size);
			else
			{
				int offSet = data.length % chunk_size;
				int toPad = chunk_size - offSet;
				byte[] pad = new byte[toPad];
				System.arraycopy(data, i * this.chunk_size, tempChunk, 0, offSet);
				System.arraycopy(pad, 0, tempChunk, offSet, pad.length);			
			}
			chunks.add(tempChunk);
		}
		this.rand = SecureRandom.getInstance("SHA1PRNG", "SUN");
		this.seed = seed;
		this.rand.setSeed(this.seed);
		//this.rand = new SecureRandom(this.seed);		
	}
	
	public Droplet droplet() throws NoSuchAlgorithmException, NoSuchProviderException
	{
		this.updateSeed();
		List<Integer> chunk_nums = Util.randChunkNums(this.rand, this.num_chunks);
		byte[] dataI = new byte[this.chunk_size];
		Arrays.fill(dataI, (byte) 0);
		
		for(int num : chunk_nums)	
			dataI = Util.xorBytes(dataI, this.chunks.get(num));
		
		return new Droplet(dataI, this.seed, this.num_chunks);
		
	}
	
	@Override
	public String toString() 
	{
		JSONObject jObject = new JSONObject();
		jObject.put("unpadded data size", dataLenBeforPadding);
		jObject.put("num_chunks", this.num_chunks);
		jObject.put("chunk size", this.chunk_size);
		jObject.put("seed", this.seed);
		
		return jObject.toString(2);
		
	}
	
	
	
	void updateSeed() throws NoSuchAlgorithmException, NoSuchProviderException
	{
		this.rand.nextBytes(this.seed);
		this.rand = SecureRandom.getInstance("SHA1PRNG", "SUN");
		this.rand.setSeed(this.seed);
		//this.rand = new SecureRandom(this.seed);
	}
	
	public byte[] chunk(int num)
	{
		int start = this.chunk_size * num;
		int end = Math.min(this.chunk_size * (num + 1), this.data.length);
		return Arrays.copyOfRange(this.data, start, end);
	}
}
