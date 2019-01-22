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
package com.ethz.ugs.server;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tukaani.xz.simple.PowerPC;

import com.ethz.ugs.dataStructures.ClientState;
import com.ethz.ugs.dataStructures.SliceManager;
import com.ethz.ugs.test.InitialGen;

/**
 * @author Aritra
 *
 */
public class ResponseUtilBinConstantTimeBrowsing {

	public static SecureRandom rand = new SecureRandom();
	public static Random guRand = new Random();

	/**
	 * Broadcast
	 * Fixed time execution
	 * Always calculate the execution time before writing the bytes on the line
	 * @param request
	 * @param response
	 * @param privateKey
	 * @param key
	 * @param iv
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public static void dropletPleaseBin(HttpServletRequest request, HttpServletResponse response, byte[] privateKey) 
			throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, 
			IllegalBlockSizeException, BadPaddingException
	{
		long start = System.nanoTime(), end = 0;

		long additionalDelay = ENV.SIMULATE_NW_NOISE ? (long) ((Math.abs(Math.round(rand.nextGaussian() * 3 + 12))) * Math.pow(10, 6)) : 0;
				
		OutputStream out = response.getOutputStream();
		//garbage
		if( Math.random() <= ENV.PROB_THRESHOLD )
		{
			String sslId = (String) request.getAttribute("javax.servlet.request.ssl_session_id");

			//if ssl id exists, that indicates the client registered for interactive request -> underground client
			if(MainServer.clientState.containSSLId(sslId))
			{
				byte[] postBody = null;
				byte[] toSend = getEncSlice(request, postBody, privateKey);

				if(toSend == null)	
				{
					byte[] garbageReturn = new byte[ENV.FIXED_PACKET_SIZE_BIN];
					rand.nextBytes(garbageReturn);
					
					//additional delay start
					long offset = additionalDelay + ENV.FIXED_REQUEST_PROCESSING_TIME_NANO - (System.nanoTime() - start);
					try {
						TimeUnit.NANOSECONDS.sleep(offset);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					end = System.nanoTime();
					//additional delay end
					
					out.write(garbageReturn);
				}
				//Interactive data :D
				else	
				{
					//additional delay start
					long offset = additionalDelay + ENV.FIXED_REQUEST_PROCESSING_TIME_NANO - (System.nanoTime() - start);
					try {
						TimeUnit.NANOSECONDS.sleep(offset);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					end = System.nanoTime();
					//additional delay end
					
					out.write(toSend);
				}
				
			}
			//No ssl id found. Looks like a normal client.
			else
			{
				byte[] garbageReturn = new byte[ENV.FIXED_PACKET_SIZE_BIN];
				rand.nextBytes(garbageReturn);	
				
				//additional delay start
				long offset = additionalDelay + ENV.FIXED_REQUEST_PROCESSING_TIME_NANO - (System.nanoTime() - start);
				try {
					TimeUnit.NANOSECONDS.sleep(offset);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				end = System.nanoTime();
				//additional delay end
				
				out.write(garbageReturn);
			}
			
			//MainServer.logger.info("Droplet noInt garbage : " + (end - start)  + " ns");
			out.flush();
			out.close();
			response.flushBuffer();
		}

		//This is same for both normal and underground client. Send the droplet
		else
		{
			byte[] packetToSend = ResponseUtilBin.dropletPleaseBinNew(request, privateKey, null);
				
			//additional delay start
			long offset = additionalDelay + ENV.FIXED_REQUEST_PROCESSING_TIME_NANO - (System.nanoTime() - start);
			try {
				TimeUnit.NANOSECONDS.sleep(offset);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			out.write(packetToSend);
			//end = System.nanoTime();
			//additional delay end
			//System.out.println(Base64.getEncoder().encodeToString(packetToSend));
		//	MainServer.logger.info("Droplet noInt packet : " + (end - start)  + " ns");
			out.flush();
			out.close();
			response.flushBuffer();
		}
	}


	/**
	 * Probabilistic interactive droplet request. May server droplets, slices or pure garbage.
	 * Fixed time execution. No droplet served.
	 * @param request
	 * @param response
	 * @param privateKey
	 * @param key
	 * @param iv
	 * @param postBody
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public static void dropletPleaseIntrBin(HttpServletRequest request, HttpServletResponse response, byte[] privateKey, byte[] postBody) 
			throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, 
			IllegalBlockSizeException, BadPaddingException
	{
		long start = System.nanoTime(), end = 0;
		long additionalDelay = ENV.SIMULATE_NW_NOISE ? (long) ((Math.abs(Math.round(rand.nextGaussian() * 3 + 12))) * Math.pow(10, 6)) : 0;

		OutputStream out = response.getOutputStream();

		//interactive, process first
		byte[] toSend = getEncSlice(request, postBody, privateKey);

		//garbage as some how the interactive data failed
		if(toSend == null)
		{
			byte[] garbageReturn = new byte[ENV.FIXED_PACKET_SIZE_BIN];
			rand.nextBytes(garbageReturn);
			
			//additional delay start
			long offset = additionalDelay + ENV.FIXED_REQUEST_PROCESSING_TIME_NANO - (System.nanoTime() - start);
			try {
				TimeUnit.NANOSECONDS.sleep(offset);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//end = System.nanoTime();
			
			//additional delay end
			
			out.write(garbageReturn);
		}	

		//enc slice
		else	
		{
			//additional delay start
			long offset = additionalDelay + ENV.FIXED_REQUEST_PROCESSING_TIME_NANO - (System.nanoTime() - start);
			try {
				TimeUnit.NANOSECONDS.sleep(offset);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//end = System.nanoTime();
			//additional delay end
			
			out.write(toSend);
		}

		//MainServer.logger.info("Droplet Int packet : " + (end - start)  + " ns");
		out.flush();
		out.close();
		response.flushBuffer();
	}

	/**
	 * Get encrypted slice data with signature :D
	 * make stuff with padding all all bells and whistles or whatever
	 * @param request
	 * @param postBody
	 * @return
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 */
	public static byte[] getEncSlice(HttpServletRequest request, byte[] postBody, byte[] privateKey) throws InvalidKeyException, InvalidAlgorithmParameterException, 
	IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException
	{
		boolean flag = false;
	
		int sliceIndex = -1;
		byte[] aesKeyByte = null;

		String sslId = (String) request.getAttribute("javax.servlet.request.ssl_session_id");

		if(postBody == null && MainServer.clientState.containSSLId(sslId))
		{		
			aesKeyByte = MainServer.clientState.getkey(sslId);
			flag = true;
		}
		else if(postBody != null)
		{
			//0x00/0x01 (1) | reserved (3) | key (16) | num_slices (4) | slice id (8 * n)| 
			aesKeyByte = new byte[ENV.AES_KEY_SIZE];
			System.arraycopy(postBody, 4, aesKeyByte, 0, ENV.AES_KEY_SIZE);
			byte[] lenBytes = new byte[4];
			System.arraycopy(postBody, ENV.AES_KEY_SIZE + 4, lenBytes, 0, 4);
			int numSliceId = ByteBuffer.wrap(lenBytes).getInt();
			//int numSliceId = len ;

			List<Long> sliceIds = new ArrayList<>();
			for(int i = 0; i < numSliceId; i++)
			{
				byte[] sliceIdBytes = new byte[8];
				System.arraycopy(postBody, ENV.AES_KEY_SIZE + 8 + i * 8, sliceIdBytes, 0, 8);
				long sliceId = ByteBuffer.wrap(sliceIdBytes).getLong();
				sliceIds.add(sliceId);
				//System.out.println(sliceId);
			}
			MainServer.clientState.addState(sslId, sliceIds, aesKeyByte);
		}

		String sliceData = null;

		long sliceId = 0x00;
		try
		{
			sliceId = MainServer.clientState.getASliceId(sslId);
		}
		catch(RuntimeException ex)
		{
			//in this case send the garbage
			if(ex.getMessage() != null && ex.getMessage().equalsIgnoreCase(ENV.EXCEPTION_MESSAGE_EMPTY_STATE_TABLE))
			{
				return null;
			}
		}
		sliceIndex = MainServer.clientState.getState(sslId, sliceId);
		sliceData = InitialGen.sdm.getSlice(sliceId, sliceIndex);

		byte[] sliceDataBytes = null;

		//in case the slice index is oveflown, remove it from the client state table
		if(sliceData.equals(SliceManager.INVALID_INDEX_OVERFLOW) || sliceData.equals(SliceManager.INVALID_SLICE_URL) || sliceData.equals(SliceManager.INVALID_SLICE_ERROR))
		{
			MainServer.clientState.removeState(sslId, sliceId);		
			return sliceDataBytes;
		}

		else
			sliceDataBytes = Base64.getDecoder().decode(sliceData);

		byte[] iv = new byte[ENV.AES_IV_SIZE];	  
		//generate a secure IV
		//rand.nextBytes(iv);
		//bad idea but for now
		Arrays.fill(iv, (byte) 0x00);
		SecretKeySpec aesKey = new SecretKeySpec(aesKeyByte, "AES");
		IvParameterSpec ivSpec = new IvParameterSpec(iv);

		byte[] sliceIndexBytes = ByteBuffer.allocate(Integer.BYTES).putInt(sliceIndex).array();
		byte[] sliceIdBytes = ByteBuffer.allocate(Long.BYTES).putLong(sliceId).array();
		byte[] sliceDatalenBytes = ByteBuffer.allocate(Integer.BYTES).putInt(sliceDataBytes.length).array();

		//IV (16) | packet len (4) | seedlen (4) ->0 | Magic (8) | Data | Padding
		//Data -> slice id (8) | slice index (4) | slice_data_len (4) | slice data (n) | signature (64)

		byte[] packetlenBytes = ByteBuffer.allocate(Integer.BYTES).putInt(ENV.FIXED_PACKET_SIZE_BIN).array();
		byte[] seedLenBytes = ByteBuffer.allocate(Integer.BYTES).putInt(0x00).array();


		//TODO old structure. Needs to incorporate signature and IV
		byte[] toSendWOpadding = new byte[24 + ENV.INTR_MAGIC_BYTES_LEN + sliceDataBytes.length];

		byte[] magicBytes = new byte[ENV.INTR_MAGIC_BYTES_LEN];
		Arrays.fill(magicBytes, ENV.INTR_MAGIC_BYTES);
		int tillNow = 0;
		System.arraycopy(packetlenBytes, 0, toSendWOpadding, tillNow, 4);
		tillNow += 4;
		System.arraycopy(seedLenBytes, 0, toSendWOpadding, tillNow, 4);
		tillNow += 4;
		System.arraycopy(magicBytes, 0, toSendWOpadding, tillNow, magicBytes.length);
		tillNow += magicBytes.length;
		System.arraycopy(sliceIdBytes, 0, toSendWOpadding, tillNow, 8);
		tillNow += 8;
		System.arraycopy(sliceIndexBytes, 0, toSendWOpadding, tillNow, 4);
		tillNow += 4;
		System.arraycopy(sliceDatalenBytes, 0, toSendWOpadding, tillNow, 4);
		tillNow += 4;
		System.arraycopy(sliceDataBytes, 0, toSendWOpadding, tillNow, sliceDataBytes.length);

		byte[] padding = new byte[ENV.FIXED_PACKET_SIZE_BIN - toSendWOpadding.length];
		if(ENV.RANDOM_PADDING)
			rand.nextBytes(padding);
		else
			Arrays.fill(padding, ENV.PADDING_DETERMINISTIC_BYTE);
		byte[] toSend = new byte[ENV.FIXED_PACKET_SIZE_BIN];
		System.arraycopy(toSendWOpadding, 0, toSend, 0, toSendWOpadding.length);
		System.arraycopy(padding, 0, toSend, toSendWOpadding.length, padding.length);


		Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
		cipher.init(Cipher.ENCRYPT_MODE, aesKey, ivSpec);
		byte[] encryptedSlicePacket = cipher.doFinal(toSend);      

		//increase state by 1
		if(flag)
		{
			try
			{
				MainServer.clientState.incrementState(sslId, sliceId);
			}
			catch(RuntimeException ex)
			{
				if(ex.getMessage().equalsIgnoreCase(ENV.EXCEPTION_MESSAGE_SSL_ID_MISSING) 
						|| ex.getMessage().equalsIgnoreCase(ENV.EXCEPTION_MESSAGE_SSL_ID_MISSING))

					return null;
			}
		}
		flag = false;
		return encryptedSlicePacket;
	}	
}