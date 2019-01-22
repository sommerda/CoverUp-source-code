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
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

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
	
	public byte[] getDecodedData() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException
	{
		if(!this.isDone())
			return null;

		byte[] decodedData = new byte[Glass.chunks.length * Glass.chunks[0].length];
		Arrays.fill(decodedData, (byte) 0x00);

		int lenTillNow = 0;
		
		byte[] lastBlock = Glass.chunks[Glass.chunks.length - 1];
		
		byte[] xorBytesFromLastBlock = new byte[Util.KEY_LENGTH];
		System.arraycopy(lastBlock, 0, xorBytesFromLastBlock, 0, xorBytesFromLastBlock.length);
		
		byte[] ivBytesFromLastBlock = new byte[Util.IV_LENGTH];
		System.arraycopy(lastBlock, xorBytesFromLastBlock.length, ivBytesFromLastBlock, 0, ivBytesFromLastBlock.length);
		
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		
		byte[] xorBytes = new byte[Util.KEY_LENGTH];
		Arrays.fill(xorBytes, (byte) 0x00);
		
		for(int i = 0 ; i < Glass.chunks.length - 1; i++)
		{		
			byte[] _hash = digest.digest(Glass.chunks[i]);
			byte[] hash = Arrays.copyOfRange(_hash, 0, Util.KEY_LENGTH);
			
			xorBytes = Util.xorBytes(hash, xorBytes);
		}
		
		byte[][] decryptedData = new byte[Glass.chunks.length - 1][];
		byte[] keyBytes = Util.xorBytes(xorBytesFromLastBlock, xorBytes);
		
		IvParameterSpec iv = new IvParameterSpec(ivBytesFromLastBlock);
		SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
		Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
		cipher.init(Cipher.DECRYPT_MODE, keySpec, iv);
		
		for(int i = 0 ; i < Glass.chunks.length - 1; i++)
		{
			decryptedData[i] = cipher.doFinal(Glass.chunks[i]);	
			byte[] counterBytes = ByteBuffer.allocate(Integer.BYTES).putInt(i).array();
			decryptedData[i] = Util.xorBytes(decryptedData[i], counterBytes);
		}
	
		for(int i = 0 ; i < decryptedData.length; i++)
		{
			System.arraycopy(decryptedData[i], 0, decodedData, lenTillNow, decryptedData[i].length);
			lenTillNow += decryptedData[i].length;
		}

		return decodedData;	
	}
	
}
