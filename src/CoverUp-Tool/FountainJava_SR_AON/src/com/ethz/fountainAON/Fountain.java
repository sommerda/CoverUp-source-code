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
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONObject;

/**
 * 
 * @author Aritra
 *
 */
public class Fountain {
	
	public byte[] data;
	public int chunk_size;
	public byte[] seed;
	public int num_chunks;
	public SecureRandom rand;
	public int dataLenBeforPadding;
	
	public byte[] keyBytes, ivBytes;
 
	List<byte[]> chunks;
	
	/**
	 * This is the constructor for the AON fountain. Uses AES 128 in CBC mode no padding according to the original specification.
	 * Future extension: XOR with a counter value to make sure identical blocks do not produce identical cipher block.
	 * @param data Input data to be encoded
	 * @param chunk_size Size of the fountain chunks
	 * @param seed Initial seed to generate the droplets
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws InvalidAlgorithmParameterException
	 */
	public Fountain(byte[] data, int chunk_size, byte[] seed) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException
	{
		if(chunk_size % 16 != 0)
			throw new IllegalArgumentException("chunk_size is not multiple of 16 bytes");
		
		this.keyBytes = new byte[Util.KEY_LENGTH];
		this.ivBytes = new byte[Util.IV_LENGTH];
		
		SecureRandom rand_cipher = new SecureRandom();
		rand_cipher.nextBytes(this.ivBytes);
		rand_cipher.nextBytes(this.keyBytes);
		
		//System.out.println("Random key : " + Base64.getUrlEncoder().encodeToString(this.keyBytes));
		//System.out.println("Random iv : " + Base64.getUrlEncoder().encodeToString(this.ivBytes));
		
		
		IvParameterSpec iv = new IvParameterSpec(this.ivBytes);
		SecretKeySpec keySpec = new SecretKeySpec(this.keyBytes, "AES");
		Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
		cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv);
		
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
			//xor with the chunk number to make sure 2 identical chunks produce different ciphertext
			byte[] counterBytes = ByteBuffer.allocate(Integer.BYTES).putInt(i).array();
			byte[] counterXoredChuck = Util.xorBytes(tempChunk, counterBytes);
			byte[] cipherText = cipher.doFinal(counterXoredChuck);
			
			chunks.add(cipherText);	
		}
		
		//prepare last block
		//calculate hash
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		byte[] xorBytes = new byte[Util.KEY_LENGTH];
		Arrays.fill(xorBytes, (byte)0x00);
		
		for(int i = 0; i < this.num_chunks; i++)
		{
			byte[] _hash = digest.digest(chunks.get(i));
			byte[] hash = Arrays.copyOfRange(_hash, 0, keyBytes.length);
			xorBytes = Util.xorBytes(hash, xorBytes);
		}
		
		xorBytes = Util.xorBytes(keyBytes, xorBytes);
		byte[] pad = new byte[this.chunk_size - xorBytes.length - ivBytes.length];
		//last block -> xor of keys and hash | iv | pad with 0's
		//xor = Util.KEY_LENGTH bytes, iv = Util.IV_LENGTH bytes
		Arrays.fill(pad, (byte) 0x00);
		byte[] lastBlock = new byte[this.chunk_size];
		
		System.arraycopy(xorBytes, 0, lastBlock, 0, xorBytes.length);
		System.arraycopy(ivBytes, 0, lastBlock, xorBytes.length, ivBytes.length);
		System.arraycopy(pad, 0, lastBlock, xorBytes.length + ivBytes.length, pad.length);
		chunks.add(lastBlock);
		this.num_chunks += 1;
		
		//System.out.println("last block : " + Base64.getUrlEncoder().encodeToString(lastBlock));
		
		//System.out.println("new " + this.num_chunks);
		
		this.rand = SecureRandom.getInstance("SHA1PRNG", "SUN");
		this.seed = seed;
		this.rand.setSeed(this.seed);
		//this.rand = new SecureRandom(this.seed);		
	}
	
	/**
	 * This method produces droplets.
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 */
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
	
	
	/**
	 * Update the seed
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 */
	void updateSeed() throws NoSuchAlgorithmException, NoSuchProviderException
	{
		this.rand.nextBytes(this.seed);
		this.rand = SecureRandom.getInstance("SHA1PRNG", "SUN");
		//only set seed works with SecureRandom object
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
