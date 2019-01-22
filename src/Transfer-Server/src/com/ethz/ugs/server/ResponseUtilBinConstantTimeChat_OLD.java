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
public class ResponseUtilBinConstantTimeChat_OLD {

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
		//long start = System.nanoTime(), end = 0;
		long start_ms = System.currentTimeMillis(), end_ms = 0;

		long additionalDelay = ENV.SIMULATE_NW_NOISE ? (long) ((Math.abs(Math.round(rand.nextGaussian() * 3 + 12))) * Math.pow(10, 6)) : 0;

		OutputStream out = response.getOutputStream();
		//garbage
		if( Math.random() <= ENV.PROB_THRESHOLD )
		{
			String sslId = (ENV.IFRAME_IF_ENABLED) ? request.getParameter("iframe_id") : (String) request.getAttribute("javax.servlet.request.ssl_session_id");

			if(MainServer.chatManager.containSSLId(sslId))
			{
				byte[] toSend = MainServer.chatManager.getChat(sslId);

				if(toSend == null)	
				{
					//send garbage in this case
					byte[] garbageReturn = new byte[ENV.FIXED_PACKET_SIZE_BIN];
					rand.nextBytes(garbageReturn);

					//additional delay start
					long offset = additionalDelay + ENV.FIXED_REQUEST_PROCESSING_TIME_MILI - (System.currentTimeMillis() - start_ms);
					try {
						TimeUnit.MILLISECONDS.sleep(offset);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					//end = System.nanoTime();
					end_ms = System.currentTimeMillis();
					//additional delay end
					MainServer.logger.info("garbage : " + (end_ms - start_ms)  + " ms");
					//MainServer.logger.info("Droplet noInt garbage : " + (end - start)  + " ns");
					out.write(garbageReturn);
				}
				//chat data :D
				else	
				{			
					//additional delay start
					long offset = additionalDelay + ENV.FIXED_REQUEST_PROCESSING_TIME_MILI - (System.currentTimeMillis() - start_ms);
					try {
						TimeUnit.MILLISECONDS.sleep(offset);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					//end = System.nanoTime();
					end_ms = System.currentTimeMillis();
					//additional delay end
					MainServer.logger.info("chat : " + (end_ms - start_ms)  + " ms");
					//MainServer.logger.info("Droplet noInt garbage : " + (end - start)  + " ns");
					out.write(toSend);
				}

			}
			else
			{
				byte[] garbageReturn = new byte[ENV.FIXED_PACKET_SIZE_BIN];
				rand.nextBytes(garbageReturn);	

				//additional delay start
				long offset = additionalDelay + ENV.FIXED_REQUEST_PROCESSING_TIME_MILI - (System.currentTimeMillis() - start_ms);
				try {
					TimeUnit.MILLISECONDS.sleep(offset);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				//end = System.nanoTime();
				end_ms = System.currentTimeMillis();
				//additional delay end
				MainServer.logger.info(" garbage : " + (end_ms - start_ms)  + " ms");
				//MainServer.logger.info("Droplet noInt garbage : " + (end - start)  + " ns");

				out.write(garbageReturn);
			}

			//MainServer.logger.info("Droplet noInt garbage : " + (end - start)  + " ns");
			out.flush();
			out.close();
			response.flushBuffer();
		}

		//droplet
		else
		{
			byte[] packetToSend = ResponseUtilBin.dropletPleaseBinNew(request, privateKey, null);

			//additional delay start
			long offset = additionalDelay + ENV.FIXED_REQUEST_PROCESSING_TIME_MILI - (System.currentTimeMillis() - start_ms);
			try {
				TimeUnit.MILLISECONDS.sleep(offset);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//end = System.nanoTime();
			end_ms = System.currentTimeMillis();
			//additional delay end
			MainServer.logger.info("droplet : " + (end_ms - start_ms)  + " ms");
			//MainServer.logger.info("Droplet noInt garbage : " + (end - start)  + " ns");
			
			out.write(packetToSend);
			//MainServer.logger.info("Droplet noInt packet : " + (end - start)  + " ns");
			out.flush();
			out.close();
			response.flushBuffer();
		}
	}

	/**
	 * Serves chat data
	 * @param request
	 * @param response
	 * @param postBody
	 * @throws IOException 
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 * @throws InvalidAlgorithmParameterException 
	 * @throws NoSuchPaddingException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 */
	public static void dropletPleaseChatBin(HttpServletRequest request, HttpServletResponse response, byte[] postBody) throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException 
	{
		//long start = System.nanoTime(), end = 0;
		long start_ms = System.currentTimeMillis(), end_ms = 0;
		long additionalDelay = ENV.SIMULATE_NW_NOISE ? (long) ((Math.abs(Math.round(rand.nextGaussian() * 3 + 12))) * Math.pow(10, 6)) : 0;
		String sslId = (ENV.IFRAME_IF_ENABLED) ? request.getParameter("iframe_id") : (String) request.getAttribute("javax.servlet.request.ssl_session_id");

		OutputStream out = response.getOutputStream();
		//0x00/0x01 (1) | reserved (3) | packetEncKey(16) | len  | data | signature
		//<p_i = sr//R_adder(8) | S_addr(8) | iv(16) | len(4) | enc_Data(n) | sig(64) (on 0|1|2|3|4)

		int pointer = 4;

		byte[] packetEnckey = new byte[16];
		//while(true)
		//{
		//reached to the end
		//if(pointer >= postBody.length)
		//	break;
		try
		{
			System.arraycopy(postBody, pointer, packetEnckey, 0, packetEnckey.length);

			pointer += packetEnckey.length; // add the offset for packet enc key

			byte[] datalenBytes = new byte[4]; //data len
			System.arraycopy(postBody, pointer, datalenBytes, 0, 4);
			int dataLen = ByteBuffer.wrap(datalenBytes).getInt();
			pointer += 4; //add offset for datalen

			//get the target address for storing in the chat manager class
			byte[] targetAddressBytes = new byte[8];
			System.arraycopy(postBody, pointer, targetAddressBytes, 0, 8);

			byte[] originAddress = new byte[8];
			System.arraycopy(postBody, pointer + 8, originAddress, 0, 8);

			//System.out.println(Base64.getEncoder().encodeToString(targetAddressBytes));

			//do not increment the pointer as we need the whole data
			byte[] dataChunk = new byte[dataLen]; //datalen  this also include 64 bytes for signature
			System.arraycopy(postBody, pointer, dataChunk, 0, dataChunk.length);

			MainServer.chatManager.addChat(sslId, Base64.getUrlEncoder().encodeToString(originAddress), 
					Base64.getUrlEncoder().encodeToString(targetAddressBytes), dataChunk);		
		}
		catch(Exception ex)
		{
			//mostly a wrongly formatted post body. Simply exit
			ex.printStackTrace(System.out);
		}
		//}

		//send chat data here

		byte[] toSendWOPadding = MainServer.chatManager.getChat(sslId);
		byte[] toSend = makeChatPacket(toSendWOPadding, packetEnckey);

		if(toSend == null)	
		{
			//send garbage in this case
			byte[] garbageReturn = new byte[ENV.FIXED_PACKET_SIZE_BIN];
			rand.nextBytes(garbageReturn);

			//additional delay start
			long offset = additionalDelay + ENV.FIXED_REQUEST_PROCESSING_TIME_MILI - (System.currentTimeMillis() - start_ms);
			try {
				TimeUnit.MILLISECONDS.sleep(offset);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//end = System.nanoTime();
			end_ms = System.currentTimeMillis();
			//additional delay end
			MainServer.logger.info("garbage : " + (end_ms - start_ms)  + " ms");
			//MainServer.logger.info("Droplet noInt garbage : " + (end - start)  + " ns");

			out.write(garbageReturn);
			out.flush();
			response.flushBuffer();
			return;
		}
		//chat data :D
		else	
		{			
			//additional delay start
			long offset = additionalDelay + ENV.FIXED_REQUEST_PROCESSING_TIME_MILI - (System.currentTimeMillis() - start_ms);
			try {
				TimeUnit.MILLISECONDS.sleep(offset);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//end = System.nanoTime();
			end_ms = System.currentTimeMillis();
			//additional delay end
			MainServer.logger.info("chat : " + (end_ms - start_ms)  + " ms");
			//MainServer.logger.info("Droplet noInt garbage : " + (end - start)  + " ns");
			
			
			out.write(toSend);
			out.flush();
			response.flushBuffer();
			return;
		}
		/*
		//additional delay start
		long offset = additionalDelay + ENV.FIXED_REQUEST_PROCESSING_TIME_NANO - (System.nanoTime() - start);
		try {
			TimeUnit.NANOSECONDS.sleep(offset);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		end = System.nanoTime();*/

		//MainServer.logger.info("Chat packet : " + (end - start)  + " ns");
		//additional delay end

	}


	/**
	 * Make the chat data packet
	 * packet len (4) | seedlen (4) ->0 | Magic (8) | Data | Padding
	 * @param chatData
	 * @return
	 * @throws NoSuchPaddingException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidAlgorithmParameterException 
	 * @throws InvalidKeyException 
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 */
	public static byte[] makeChatPacket(byte[] chatData, byte[] packetEnckey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException
	{
		if(chatData == null || packetEnckey == null)
			return null;

		//packet len (4) | seedlen (4) ->0 | Magic (8) | Data | Padding
		byte[] packetlenBytes = ByteBuffer.allocate(Integer.BYTES).putInt(ENV.FIXED_PACKET_SIZE_BIN).array();
		byte[] seedLenBytes = ByteBuffer.allocate(Integer.BYTES).putInt(0x00).array();

		byte[] toSendWOpadding = new byte[8 + ENV.CHAT_MAGIC_BYTES_LEN + chatData.length];

		byte[] magicBytes = new byte[ENV.CHAT_MAGIC_BYTES_LEN];
		Arrays.fill(magicBytes, ENV.CHAT_MAGIC_BYTES);
		int tillNow = 0;
		System.arraycopy(packetlenBytes, 0, toSendWOpadding, tillNow, 4);
		tillNow += 4;
		System.arraycopy(seedLenBytes, 0, toSendWOpadding, tillNow, 4);
		tillNow += 4;
		System.arraycopy(magicBytes, 0, toSendWOpadding, tillNow, magicBytes.length);
		tillNow += magicBytes.length;
		System.arraycopy(chatData, 0, toSendWOpadding, tillNow, chatData.length);
		tillNow += chatData.length;

		byte[] padding = new byte[ENV.FIXED_PACKET_SIZE_BIN - toSendWOpadding.length];

		if(ENV.RANDOM_PADDING)
			rand.nextBytes(padding);
		else
			Arrays.fill(padding, ENV.PADDING_DETERMINISTIC_BYTE);

		byte[] toSend = new byte[ENV.FIXED_PACKET_SIZE_BIN];
		System.arraycopy(toSendWOpadding, 0, toSend, 0, toSendWOpadding.length);
		System.arraycopy(padding, 0, toSend, toSendWOpadding.length, padding.length);

		//encryption entire packet
		byte[] iv = new byte[ENV.AES_IV_SIZE];	  
		//generate a secure IV
		//rand.nextBytes(iv);
		//bad idea but for now
		Arrays.fill(iv, (byte) 0x00);
		SecretKeySpec aesKey = new SecretKeySpec(packetEnckey, "AES");
		IvParameterSpec ivSpec = new IvParameterSpec(iv);

		Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
		cipher.init(Cipher.ENCRYPT_MODE, aesKey, ivSpec);
		byte[] encryptedChatPacket = cipher.doFinal(toSend);      

		//System.out.println(encryptedChatPacket);
		return encryptedChatPacket;
	}
}