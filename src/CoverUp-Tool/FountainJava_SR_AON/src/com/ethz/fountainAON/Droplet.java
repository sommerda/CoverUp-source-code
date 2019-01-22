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

import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

import org.json.JSONObject;

public class Droplet {

	byte[] data;
	byte[] seed;
	int num_chunks;
	public SecureRandom rand;
	
	public boolean generated; 
	List<Integer> chunkNums;
	
	public Droplet(byte[] data, byte[] seed, int num_chunks) throws NoSuchAlgorithmException, NoSuchProviderException
	{
		this.data = data;
		this.seed = seed;
		this.num_chunks = num_chunks;
		this.rand = SecureRandom.getInstance("SHA1PRNG", "SUN");
		chunkNums();
	}
	
	private void chunkNums() throws NoSuchAlgorithmException, NoSuchProviderException
	{
		this.rand = SecureRandom.getInstance("SHA1PRNG", "SUN");
		this.rand.setSeed(this.seed);
		//rand = new SecureRandom(this.seed);	
		this.chunkNums = Util.randChunkNums(rand, this.num_chunks);	
	}
	
	public byte[] toByteArray() 
	{
		
		byte[] seedLenBytes = ByteBuffer.allocate(Integer.BYTES).putInt(this.seed.length).array();
		byte[] num_chunksBytes = ByteBuffer.allocate(Integer.BYTES).putInt(this.num_chunks).array();
		byte[] dataLenBytes = ByteBuffer.allocate(Integer.BYTES).putInt(this.data.length).array();
		                          
		byte[] dropletByte = new byte[seedLenBytes.length + this.seed.length + num_chunksBytes.length + dataLenBytes.length + this.data.length];

		System.arraycopy(seedLenBytes, 0, dropletByte, 0, seedLenBytes.length);
		System.arraycopy(this.seed, 0, dropletByte, seedLenBytes.length, this.seed.length);
		System.arraycopy(num_chunksBytes, 0, dropletByte, seedLenBytes.length + this.seed.length, num_chunksBytes.length);
		System.arraycopy(dataLenBytes, 0, dropletByte, seedLenBytes.length + this.seed.length + num_chunksBytes.length, dataLenBytes.length);
		System.arraycopy(this.data, 0, dropletByte, seedLenBytes.length + this.seed.length + num_chunksBytes.length + dataLenBytes.length, this.data.length);

		return dropletByte;
	}
	
	@Override
	public String toString() {
		
		JSONObject jObject = new JSONObject();
		jObject.put("seed", Base64.getUrlEncoder().encodeToString(this.seed));
		jObject.put("num_chunks", this.num_chunks);
		jObject.put("data", Base64.getUrlEncoder().encodeToString(this.data));
		
		
		return jObject.toString(2);
	}
	
	@Override
	public boolean equals(Object obj) 
	{
		return this.seed == ((Droplet) obj).seed;
	}
}
