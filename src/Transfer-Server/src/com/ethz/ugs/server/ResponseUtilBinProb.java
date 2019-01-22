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

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.management.RuntimeErrorException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ethz.ugs.dataStructures.ClientState;
import com.ethz.ugs.dataStructures.SliceManager;
import com.ethz.ugs.test.InitialGen;

/**
 * @author Aritra
 *
 */
public class ResponseUtilBinProb {

	public static SecureRandom rand = new SecureRandom();


	/**
	 * Broadcast
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
	public static void dropletPleaseBin(HttpServletRequest request, HttpServletResponse response, byte[] privateKey, byte[] key, byte[] iv) 
			throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, 
			IllegalBlockSizeException, BadPaddingException
	{
		long start = System.nanoTime();

		SecretKeySpec aesKey = new SecretKeySpec(key, "AES");
		IvParameterSpec ivSpec = new IvParameterSpec(iv);

		byte[] randMessage = new byte[ENV.FIXED_PACKET_SIZE_BIN];
		Arrays.fill(randMessage, (byte) 0x00);
		Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
		cipher.init(Cipher.ENCRYPT_MODE, aesKey, ivSpec);
		byte[] cipherText = cipher.doFinal(randMessage);

		OutputStream out = response.getOutputStream();
		//dummy operation
		byte[] toSendDummy = getEncSliceDummy(request, privateKey);
		//garbage
		if( Math.random() <= ENV.PROB_THRESHOLD )
		{
			String sslId = (String) request.getAttribute("javax.servlet.request.ssl_session_id");

			if(MainServer.clientState.containSSLId(sslId))
			{
				byte[] postBody = null;
				byte[] toSend = getEncSlice(request, postBody, privateKey);

				if(toSend == null)		
					out.write(cipherText);
				else
					out.write(toSend);
			}
			else
			{

				out.write(cipherText);
			}
			long end = System.nanoTime();
			//MainServer.logger.info("Droplet noInt garbage : " + (end - start)  + " ns");
			out.flush();
			out.close();
			response.flushBuffer();
		}

		//droplet
		else
		{
			//more dummy
			byte[] sk = new byte[64];
			rand.nextBytes(sk);
			byte[] toSendDummy_1 = getEncSliceDummy(request, sk);
			rand.nextBytes(sk);
			byte[] toSendDummy_2 = getEncSliceDummy(request, sk);
			//rand.nextBytes(sk);
			//byte[] toSendDummy_3 = getEncSliceDummy(request, sk);
			//rand.nextBytes(sk);
			//byte[] toSendDummy_4 = getEncSliceDummy(request, sk);
			//rand.nextBytes(sk);
			//byte[] toSendDummy_5 = getEncSliceDummy(request, sk);

			byte[] dummy = new byte[toSendDummy.length + toSendDummy_1.length + toSendDummy_2.length];
			// + toSendDummy_3.length + toSendDummy_4.length + toSendDummy_5.length];

			int tillNow = 0;
			System.arraycopy(toSendDummy, 0, dummy, tillNow, toSendDummy.length);
			tillNow +=toSendDummy.length;
			System.arraycopy(toSendDummy_1, 0, dummy, tillNow, toSendDummy_1.length);
			tillNow +=toSendDummy_1.length;
			System.arraycopy(toSendDummy_2, 0, dummy, tillNow, toSendDummy_2.length);
			//tillNow +=toSendDummy_2.length;
			//System.arraycopy(toSendDummy_3, 0, dummy, tillNow, toSendDummy_3.length);
			//tillNow +=toSendDummy_3.length;
			//System.arraycopy(toSendDummy_4, 0, dummy, tillNow, toSendDummy_4.length);
			//tillNow +=toSendDummy_4.length;
			//System.arraycopy(toSendDummy_5, 0, dummy, tillNow, toSendDummy_5.length);


			//
			byte[] packetToSend = ResponseUtilBin.dropletPleaseBinNew(request, privateKey, dummy);

			long end = System.nanoTime();
			out.write(packetToSend);
			//MainServer.logger.info("Droplet noInt packet : " + (end - start)  + " ns");
			out.flush();
			out.close();
			response.flushBuffer();
		}


	}


	/**
	 * Probabilistic interactive droplet request. May server droplets, slices or pure garbage.
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
	public static void dropletPleaseIntrBin(HttpServletRequest request, HttpServletResponse response, byte[] privateKey, byte[] key, byte[] iv, byte[] postBody) 
			throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, 
			IllegalBlockSizeException, BadPaddingException
	{
		long start = System.nanoTime();

		OutputStream out = response.getOutputStream();

		SecretKeySpec aesKey = new SecretKeySpec(key, "AES");
		IvParameterSpec ivSpec = new IvParameterSpec(iv);
		byte[] randMessage = new byte[ENV.FIXED_PACKET_SIZE_BIN];
		rand.nextBytes(randMessage);
		Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
		cipher.init(Cipher.ENCRYPT_MODE, aesKey, ivSpec);
		byte[] cipherText = cipher.doFinal(randMessage);


		//interactive, process first
		byte[] toSend = getEncSlice(request, postBody, privateKey);

		//garbage
		if(toSend == null)
		{
			//ResponseUtilBin.dropletPleaseBinNew(request, privateKey, null);
			out.write(cipherText);
		}	

		//enc slice
		else	
			out.write(toSend);


		long end = System.nanoTime();
		//MainServer.logger.info("Droplet Int packet : " + (end - start)  + " ns");
		out.flush();
		out.close();
		response.flushBuffer();
	}

	/**
	 * Get encrypted slice data with signature :D
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
		//0/1,slice_index, slice_id, key:padding

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
			//0x00/0x01 (1) | reserved (3) | key (16) | len (4) | slice id (8 * n)| 
			aesKeyByte = new byte[ENV.AES_KEY_SIZE];
			System.arraycopy(postBody, 4, aesKeyByte, 0, ENV.AES_KEY_SIZE);
			byte[] lenBytes = new byte[4];
			System.arraycopy(postBody, ENV.AES_KEY_SIZE + 4, lenBytes, 0, 4);
			int len = ByteBuffer.wrap(lenBytes).getInt();
			int numSliceId = len / 8;

			List<Long> sliceIds = new ArrayList<>();
			for(int i = 0; i < numSliceId; i++)
			{
				byte[] sliceIdBytes = new byte[8];
				System.arraycopy(postBody, ENV.AES_KEY_SIZE + 8 + i * 8, sliceIdBytes, 0, 8);
				long sliceId = ByteBuffer.wrap(sliceIdBytes).getLong();
				sliceIds.add(sliceId);
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
			catch(RuntimeErrorException ex)
			{
				if(ex.getMessage().equalsIgnoreCase(ENV.EXCEPTION_MESSAGE_SSL_ID_MISSING) 
						|| ex.getMessage().equalsIgnoreCase(ENV.EXCEPTION_MESSAGE_SSL_ID_MISSING))

					return null;
			}
		}
		flag = false;
		return encryptedSlicePacket;
	}


	/**
	 * Normal droplet dummy operation
	 * @param request
	 * @param privateKey
	 * @return
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 */
	public static byte[] getEncSliceDummy(HttpServletRequest request, byte[] privateKey) throws InvalidKeyException, InvalidAlgorithmParameterException, 
	IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException
	{
		boolean flag = false;
		//0/1,slice_index, slice_id, key:padding

		int sliceIndex = -1;
		byte[] aesKeyByte = null;

		String sslId = ClientState.dummySSLId;

		//dummy insert
		//0x00/0x01 (1) | reserved (3) | key (16) | len (4) | slice id (8 * n)| 
		aesKeyByte = new byte[ENV.AES_KEY_SIZE];	
		byte[] postBody = new byte[4 + 16 + 4 + 8 * 3];
		postBody[0] = 0x01;
		rand.nextBytes(aesKeyByte);
		System.arraycopy(aesKeyByte, 0, postBody, 4, ENV.AES_KEY_SIZE);
		byte[] lenB = ByteBuffer.allocate(4).putInt(24).array();
		System.arraycopy(lenB, 0, postBody, 4 + ENV.AES_KEY_SIZE, 4);

		Iterator<Long> sIds = SliceManager.SLICE_MAP.values().iterator();
		for(int i = 0; i < 3; i++)
		{		
			byte[] lbytes = ByteBuffer.allocate(8).putLong(sIds.next()).array();
			System.arraycopy(lbytes, 0, postBody, 8 + ENV.AES_KEY_SIZE + i * 8, 8);
		}
		//dummy insert ends

		System.arraycopy(postBody, 4, aesKeyByte, 0, ENV.AES_KEY_SIZE);
		byte[] lenBytes = new byte[4];
		System.arraycopy(postBody, ENV.AES_KEY_SIZE + 4, lenBytes, 0, 4);
		int len = ByteBuffer.wrap(lenBytes).getInt();
		int numSliceId = len / 8;

		List<Long> sliceIds = new ArrayList<>();
		for(int i = 0; i < numSliceId; i++)
		{
			byte[] sliceIdBytes = new byte[8];
			System.arraycopy(postBody, ENV.AES_KEY_SIZE + 8 + i * 8, sliceIdBytes, 0, 8);
			long sliceId = ByteBuffer.wrap(sliceIdBytes).getLong();
			sliceIds.add(sliceId);
		}

		MainServer.clientState.addState(sslId, sliceIds, aesKeyByte);

		flag = true;

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
		if(sliceIndex > 0)
			sliceIndex = 0;
		//get the first slice from 0the slice index
		sliceData = InitialGen.sdm.getSlice();

		byte[] sliceDataBytes = null;

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
				MainServer.clientState.incrementStateDummy(sslId, sliceId);
			}
			catch(RuntimeException ex)
			{
				if(ex.getMessage().equalsIgnoreCase(ENV.EXCEPTION_MESSAGE_SSL_ID_MISSING) 
						|| ex.getMessage().equalsIgnoreCase(ENV.EXCEPTION_MESSAGE_SSL_ID_MISSING))

					return null;
			}
		}
		flag = false;
		byte[] ret = new byte[encryptedSlicePacket.length + privateKey.length];
		System.arraycopy(encryptedSlicePacket, 0, ret, 0, encryptedSlicePacket.length);
		System.arraycopy(privateKey, 0, ret, encryptedSlicePacket.length, privateKey.length);
		return ret;
	}





	@SuppressWarnings("unused")
	private static byte[] getEncSliceNew(HttpServletRequest request, byte[] postBody, byte[] privateKey, boolean dummy) throws InvalidKeyException, InvalidAlgorithmParameterException, 
	IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException
	{
		if(!dummy)
		{
			boolean flag = false;
			//0/1,slice_index, slice_id, key:padding

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
				//0x00/0x01 (1) | reserved (3) | key (16) | len (4) | slice id (8 * n)| 
				aesKeyByte = new byte[ENV.AES_KEY_SIZE];
				System.arraycopy(postBody, 4, aesKeyByte, 0, ENV.AES_KEY_SIZE);
				byte[] lenBytes = new byte[4];
				System.arraycopy(postBody, ENV.AES_KEY_SIZE + 4, lenBytes, 0, 4);
				int len = ByteBuffer.wrap(lenBytes).getInt();
				int numSliceId = len / 8;

				List<Long> sliceIds = new ArrayList<>();
				for(int i = 0; i < numSliceId; i++)
				{
					byte[] sliceIdBytes = new byte[8];
					System.arraycopy(postBody, ENV.AES_KEY_SIZE + 8 + i * 8, sliceIdBytes, 0, 8);
					long sliceId = ByteBuffer.wrap(sliceIdBytes).getLong();
					sliceIds.add(sliceId);
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
		else
		{
			boolean flag = false;
			//0/1,slice_index, slice_id, key:padding

			int sliceIndex = -1;
			byte[] aesKeyByte = null;

			String sslId = ClientState.dummySSLId;

			//dummy insert
			//0x00/0x01 (1) | reserved (3) | key (16) | len (4) | slice id (8 * n)| 
			aesKeyByte = new byte[ENV.AES_KEY_SIZE];	
			byte[] postBody1 = new byte[4 + 16 + 4 + 8 * 3];
			postBody1[0] = 0x01;
			rand.nextBytes(aesKeyByte);
			System.arraycopy(aesKeyByte, 0, postBody1, 4, ENV.AES_KEY_SIZE);
			byte[] lenB = ByteBuffer.allocate(4).putInt(24).array();
			System.arraycopy(lenB, 0, postBody1, 4 + ENV.AES_KEY_SIZE, 4);

			Iterator<Long> sIds = SliceManager.SLICE_MAP.values().iterator();
			for(int i = 0; i < 3; i++)
			{		
				byte[] lbytes = ByteBuffer.allocate(8).putLong(sIds.next()).array();
				System.arraycopy(lbytes, 0, postBody1, 8 + ENV.AES_KEY_SIZE + i * 8, 8);
			}
			//dummy insert ends

			System.arraycopy(postBody1, 4, aesKeyByte, 0, ENV.AES_KEY_SIZE);
			byte[] lenBytes = new byte[4];
			System.arraycopy(postBody1, ENV.AES_KEY_SIZE + 4, lenBytes, 0, 4);
			int len = ByteBuffer.wrap(lenBytes).getInt();
			int numSliceId = len / 8;

			List<Long> sliceIds = new ArrayList<>();
			for(int i = 0; i < numSliceId; i++)
			{
				byte[] sliceIdBytes = new byte[8];
				System.arraycopy(postBody1, ENV.AES_KEY_SIZE + 8 + i * 8, sliceIdBytes, 0, 8);
				long sliceId = ByteBuffer.wrap(sliceIdBytes).getLong();
				sliceIds.add(sliceId);
			}

			MainServer.clientState.addState(sslId, sliceIds, aesKeyByte);

			flag = true;

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
			if(sliceIndex > 0)
				sliceIndex = 0;
			//get the first slice from 0the slice index
			sliceData = InitialGen.sdm.getSlice();

			byte[] sliceDataBytes = null;

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
					MainServer.clientState.incrementStateDummy(sslId, sliceId);
				}
				catch(RuntimeException ex)
				{
					if(ex.getMessage().equalsIgnoreCase(ENV.EXCEPTION_MESSAGE_SSL_ID_MISSING) 
							|| ex.getMessage().equalsIgnoreCase(ENV.EXCEPTION_MESSAGE_SSL_ID_MISSING))

						return null;
				}
			}
			flag = false;
			byte[] ret = new byte[encryptedSlicePacket.length + privateKey.length];
			System.arraycopy(encryptedSlicePacket, 0, ret, 0, encryptedSlicePacket.length);
			System.arraycopy(privateKey, 0, ret, encryptedSlicePacket.length, privateKey.length);
			return ret;

		}
	}

}