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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.whispersystems.curve25519.Curve25519;

import com.ethz.ugs.dataStructures.FountainTableRow;
import com.ethz.ugs.dataStructures.SiteMap;
import com.ethz.ugs.dataStructures.SliceManager;
import com.ethz.ugs.test.InitialGen;

public class ResponseUtilBinHP {


	public static SecureRandom rand = new SecureRandom();
	/**
	 * Intr droplet request. High performance version.
	 * <br>
	 * P = fixed packet size
	 * <br>
	 * table-> packet_len (4) | droplet (n) | url_len (4) | url (n_1) | f_id (8) | signature (64) | padding (p - 72 - n - n_1) |</br>
	 * 
	 * Signature is on  packet_len | droplet | url_len | url | f_id 
	 * <br>
	 * droplet -> seedlen (4) | seed(n) | num_chunk (4) | datalen (4) | data (n)
	 * 
	 * @param request HttpServletRequest
	 * @param response HttpServletResponse
	 * @param privateKey Server curve25519 private key
	 * @throws IOException
	 */
	public static void dropletPleaseIntrBin(HttpServletRequest request, HttpServletResponse response, byte[] privateKey, String requestBody) throws IOException
	{
		long start = System.nanoTime();

		//0/1,slice_index,id_x,id_1,...,id_n:padding
		String fountainIdString = requestBody.split(":")[0];
		String[] fountains = fountainIdString.split(",");

		int sliceIndex = Integer.parseInt(fountains[1]);
		//3rd element is the requested id
		String intrSliceId = fountains[2];

		Set<String> fountainSet = new HashSet<>();

		//starts form 3rd element
		/*
		 * Important:
		 * Current implementation will ignore any invalid fountain id/url.
		 * It will simply go to the next fountain id in the request payload.
		 * Example: Server has 3 fountains : a,b,c. If client send a,b,d
		 * it will be treated as a,b. d will be ignored.
		 */
		for(int i = 3; i < fountains.length; i++)
		{
			try
			{
				long fountainId = Long.parseLong(fountains[i]);
				String url = FountainTableRow.dropletLocUrlMap.get(fountainId);

				if(url == null)
				{
					continue;
					
					/*
					response.getWriter().append("Invalid fountain id " + fountainId);
					response.flushBuffer();

					return;*/				
				}
				//System.out.println("added" + url);
				fountainSet.add(url);
			}
			catch(Exception ex)
			{
				//System.out.println("added" + fountains[i]);
				if(!SiteMap.TABLE_MAP.containsKey(fountains[i]))
				{
					continue;
					/*response.getWriter().append("Invalid fountain url " + fountains[i]);
					response.flushBuffer();

					return;*/				
				}
				fountainSet.add(fountains[i]);
			}
		}

		String[] dropletStr = SiteMap.getRandomDroplet(null);
		String url = dropletStr[1];

		JSONObject jObject2 = new JSONObject(dropletStr[0]);
		byte[] seedBytes = Base64.getUrlDecoder().decode(jObject2.getString("seed"));
		byte[] seedLenBytes = ByteBuffer.allocate(Integer.BYTES).putInt(seedBytes.length).array();
		byte[] num_chunksBytes = ByteBuffer.allocate(Integer.BYTES).putInt(jObject2.getInt("num_chunks")).array();
		byte[] data = Base64.getUrlDecoder().decode(jObject2.getString("data"));
		byte[] dataLenBytes = ByteBuffer.allocate(Integer.BYTES).putInt(data.length).array();


		byte[] dropletByte = new byte[seedLenBytes.length + seedBytes.length + num_chunksBytes.length + dataLenBytes.length + data.length];

		System.arraycopy(seedLenBytes, 0, dropletByte, 0, seedLenBytes.length);
		System.arraycopy(seedBytes, 0, dropletByte, seedLenBytes.length, seedBytes.length);
		System.arraycopy(num_chunksBytes, 0, dropletByte, seedLenBytes.length + seedBytes.length, num_chunksBytes.length);
		System.arraycopy(dataLenBytes, 0, dropletByte, seedLenBytes.length + seedBytes.length + num_chunksBytes.length, dataLenBytes.length);
		int dataOffset = seedLenBytes.length + seedBytes.length + num_chunksBytes.length + dataLenBytes.length;
		System.arraycopy(data, 0, dropletByte, seedLenBytes.length + seedBytes.length + num_chunksBytes.length + dataLenBytes.length, data.length);

		byte[] fixedPacketLenBytes = ByteBuffer.allocate(Integer.BYTES).putInt(ENV.FIXED_PACKET_SIZE_BIN).array();

		byte[] urlBytes = url.getBytes(StandardCharsets.UTF_8);
		byte[] urlLenBytes = ByteBuffer.allocate(Integer.BYTES).putInt(urlBytes.length).array();
		byte[] f_idBytes = ByteBuffer.allocate(Long.BYTES).putLong(FountainTableRow.dropletLocUrlMapRev.get(url)).array();

		/*debug
		 * System.out.println("@@");
		for(byte b : f_idBytes)
			System.out.println(b);
		System.out.println("@@");
		 */
		byte[] dataToSign = new byte[fixedPacketLenBytes.length + dropletByte.length + urlLenBytes.length + urlBytes.length + f_idBytes.length];

		System.arraycopy(fixedPacketLenBytes, 0, dataToSign, 0, fixedPacketLenBytes.length);
		//move offset by fixedPacketLenBytes.length bytes as it is prepended in the packet
		dataOffset += fixedPacketLenBytes.length; 
		//System.out.println("offset :" + dataOffset);

		System.arraycopy(dropletByte, 0, dataToSign, fixedPacketLenBytes.length, dropletByte.length);

		System.arraycopy(urlLenBytes, 0, dataToSign, fixedPacketLenBytes.length + dropletByte.length, urlLenBytes.length);
		System.arraycopy(urlBytes, 0, dataToSign, fixedPacketLenBytes.length + dropletByte.length + urlLenBytes.length, urlBytes.length);
		System.arraycopy(f_idBytes, 0, dataToSign, fixedPacketLenBytes.length + dropletByte.length + urlLenBytes.length + urlBytes.length, f_idBytes.length);

		byte[] signatureBytes = null;
		try 
		{

			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] hashDataToSign = md.digest(dataToSign);
			//System.out.println("hash : " + Base64.getUrlEncoder().encodeToString(hashDataToSign));
			signatureBytes = Curve25519.getInstance("best").calculateSignature(privateKey, hashDataToSign);
		} 

		catch (NoSuchAlgorithmException e) 
		{
			e.printStackTrace();
			response.getWriter().append("Server Error.");
			response.flushBuffer();
		}


		byte[] padding = new byte[ENV.FIXED_PACKET_SIZE_BIN - dataToSign.length - signatureBytes.length];
		if(ENV.RANDOM_PADDING)
			rand.nextBytes(padding);
		else
			Arrays.fill(padding, ENV.PADDING_DETERMINISTIC_BYTE);

		byte[] packetToSend = new byte[ENV.FIXED_PACKET_SIZE_BIN];

		//dataToSign | signature | padding
		System.arraycopy(dataToSign, 0, packetToSend, 0, dataToSign.length);
		System.arraycopy(signatureBytes, 0, packetToSend, dataToSign.length, signatureBytes.length);
		System.arraycopy(padding, 0, packetToSend, dataToSign.length + signatureBytes.length, padding.length);


		//replace droplet data with the slice data

		if(fountainSet.contains(url))
		{
			//String sliceData = InitialGen.sdm.getSlice(intrSliceId, sliceIndex);
			String sliceData = null;
			try
			{
				Long sliceID = Long.parseLong(intrSliceId);
				sliceData = InitialGen.sdm.getSlice(sliceID, sliceIndex);
			}
			catch(Exception ex)
			{
				sliceData = InitialGen.sdm.getSlice(intrSliceId, sliceIndex);
			}
			
			byte[] sliceDataBytes = null;

			if(sliceData.equals(SliceManager.INVALID_INDEX_OVERFLOW) || sliceData.equals(SliceManager.INVALID_SLICE_URL) || sliceData.equals(SliceManager.INVALID_SLICE_ERROR))
			{
				sliceDataBytes = new byte[ENV.FOUNTAIN_CHUNK_SIZE];
				Arrays.fill(sliceDataBytes, ENV.PADDING_DETERMINISTIC_BYTE);
			}

			else
				sliceDataBytes = Base64.getDecoder().decode(sliceData);

			//for some stupid reason
			if(sliceData.equals(SliceManager.INVALID_INDEX_OVERFLOW))
				response.addHeader("x-flag", "2");

			else if(sliceData.equals(SliceManager.INVALID_SLICE_URL))
				response.addHeader("x-flag", "3");

			else if(sliceData.equals(SliceManager.INVALID_SLICE_ERROR))
				response.addHeader("x-flag", "4");

			else
			{
				response.addHeader("x-flag", "1");
				System.arraycopy(sliceDataBytes, 0, packetToSend, dataOffset, sliceDataBytes.length);
			}
			//replace the droplet data with slice data. Finger crossed :P


			OutputStream out = response.getOutputStream();
			out.write(packetToSend);	
			out.flush();
			out.close();

			/*FileWriter fw = new FileWriter("binResp.txt");
			fw.append(Base64.getEncoder().encodeToString(packetToSend));
			fw.flush();
			fw.close();
			 */

			//System.out.println("len (bytes on line) :: " + packetToSend.length);	
			long end = System.nanoTime();
			//MainServer.logger.info("Droplet Bin HP : " + (end - start)  + " ns");

			response.flushBuffer();

			return;	
		}

		response.addHeader("x-flag", "0");
		OutputStream out = response.getOutputStream();
		out.write(packetToSend);
		out.flush();
		out.close();

		/*
		FileWriter fw = new FileWriter("binResp.txt");
		fw.append(Base64.getEncoder().encodeToString(packetToSend));
		fw.flush();
		fw.close();
		 */
		//MainServer.logger.info("Droplet Bin : " + (System.nanoTime() - start)  + " ns");
		//System.out.println("len (bytes on line) :: " + packetToSend.length);	
		response.flushBuffer();		
	}


	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	/**
	 * Sends slices
	 * @param request
	 * @param response
	 * @param privateKey
	 * @param requestBody
	 * @throws IOException
	 */
	public static void slicePleaseBin(HttpServletRequest request, HttpServletResponse response, byte[] privateKey, String requestBody) throws IOException
	{
		long start = System.nanoTime();

		String[] dropletStr = SiteMap.getRandomDroplet(null);
		String url = dropletStr[1];

		JSONObject jObject2 = new JSONObject(dropletStr[0]);
		byte[] seedBytes = Base64.getUrlDecoder().decode(jObject2.getString("seed"));
		byte[] seedLenBytes = ByteBuffer.allocate(Integer.BYTES).putInt(seedBytes.length).array();
		byte[] num_chunksBytes = ByteBuffer.allocate(Integer.BYTES).putInt(jObject2.getInt("num_chunks")).array();
		byte[] data = Base64.getUrlDecoder().decode(jObject2.getString("data"));
		byte[] dataLenBytes = ByteBuffer.allocate(Integer.BYTES).putInt(data.length).array();


		byte[] dropletByte = new byte[seedLenBytes.length + seedBytes.length + num_chunksBytes.length + dataLenBytes.length + data.length];

		System.arraycopy(seedLenBytes, 0, dropletByte, 0, seedLenBytes.length);
		System.arraycopy(seedBytes, 0, dropletByte, seedLenBytes.length, seedBytes.length);
		System.arraycopy(num_chunksBytes, 0, dropletByte, seedLenBytes.length + seedBytes.length, num_chunksBytes.length);
		System.arraycopy(dataLenBytes, 0, dropletByte, seedLenBytes.length + seedBytes.length + num_chunksBytes.length, dataLenBytes.length);
		int dataOffset = seedLenBytes.length + seedBytes.length + num_chunksBytes.length + dataLenBytes.length;
		System.arraycopy(data, 0, dropletByte, seedLenBytes.length + seedBytes.length + num_chunksBytes.length + dataLenBytes.length, data.length);

		byte[] fixedPacketLenBytes = ByteBuffer.allocate(Integer.BYTES).putInt(ENV.FIXED_PACKET_SIZE_BIN).array();

		byte[] urlBytes = url.getBytes(StandardCharsets.UTF_8);
		byte[] urlLenBytes = ByteBuffer.allocate(Integer.BYTES).putInt(urlBytes.length).array();
		byte[] f_idBytes = ByteBuffer.allocate(Long.BYTES).putLong(FountainTableRow.dropletLocUrlMapRev.get(url)).array();

		/*debug
		 * System.out.println("@@");
		for(byte b : f_idBytes)
			System.out.println(b);
		System.out.println("@@");
		 */
		byte[] dataToSign = new byte[fixedPacketLenBytes.length + dropletByte.length + urlLenBytes.length + urlBytes.length + f_idBytes.length];

		System.arraycopy(fixedPacketLenBytes, 0, dataToSign, 0, fixedPacketLenBytes.length);
		//move offset by fixedPacketLenBytes.length bytes as it is prepended in the packet
		dataOffset += fixedPacketLenBytes.length; 
		//System.out.println("offset :" + dataOffset);

		System.arraycopy(dropletByte, 0, dataToSign, fixedPacketLenBytes.length, dropletByte.length);

		System.arraycopy(urlLenBytes, 0, dataToSign, fixedPacketLenBytes.length + dropletByte.length, urlLenBytes.length);
		System.arraycopy(urlBytes, 0, dataToSign, fixedPacketLenBytes.length + dropletByte.length + urlLenBytes.length, urlBytes.length);
		System.arraycopy(f_idBytes, 0, dataToSign, fixedPacketLenBytes.length + dropletByte.length + urlLenBytes.length + urlBytes.length, f_idBytes.length);

		byte[] signatureBytes = null;
		try 
		{

			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] hashDataToSign = md.digest(dataToSign);
			//System.out.println("hash : " + Base64.getUrlEncoder().encodeToString(hashDataToSign));
			signatureBytes = Curve25519.getInstance("best").calculateSignature(privateKey, hashDataToSign);
		} 

		catch (NoSuchAlgorithmException e) 
		{
			e.printStackTrace();
			response.getWriter().append("Server Error.");
			response.flushBuffer();
		}


		byte[] padding = new byte[ENV.FIXED_PACKET_SIZE_BIN - dataToSign.length - signatureBytes.length];
		if(ENV.RANDOM_PADDING)
			rand.nextBytes(padding);
		else
			Arrays.fill(padding, ENV.PADDING_DETERMINISTIC_BYTE);

		byte[] packetToSend = new byte[ENV.FIXED_PACKET_SIZE_BIN];

		//dataToSign | signature | padding
		System.arraycopy(dataToSign, 0, packetToSend, 0, dataToSign.length);
		System.arraycopy(signatureBytes, 0, packetToSend, dataToSign.length, signatureBytes.length);
		System.arraycopy(padding, 0, packetToSend, dataToSign.length + signatureBytes.length, padding.length);


		//replace droplet data with the slice data

		String sliceData = InitialGen.sdm.getSlice();
		byte[] sliceDataBytes = null;

		if(sliceData.equals(SliceManager.INVALID_INDEX_OVERFLOW) || sliceData.equals(SliceManager.INVALID_SLICE_URL) || sliceData.equals(SliceManager.INVALID_SLICE_ERROR))
		{
			sliceDataBytes = new byte[ENV.FOUNTAIN_CHUNK_SIZE];
			Arrays.fill(sliceDataBytes, ENV.PADDING_DETERMINISTIC_BYTE);
		}

		else
			sliceDataBytes = Base64.getDecoder().decode(sliceData);

		//for some stupid reason
		if(sliceData.equals(SliceManager.INVALID_INDEX_OVERFLOW))
			response.addHeader("x-flag", "2");

		else if(sliceData.equals(SliceManager.INVALID_SLICE_URL))
			response.addHeader("x-flag", "3");

		else if(sliceData.equals(SliceManager.INVALID_SLICE_ERROR))
			response.addHeader("x-flag", "4");

		else
		{
			response.addHeader("x-flag", "1");
			//replace the droplet data with slice data. Finger crossed :P
			System.arraycopy(sliceDataBytes, 0, packetToSend, dataOffset, sliceDataBytes.length);
		}


		OutputStream out = response.getOutputStream();
		out.write(packetToSend);	
		out.flush();
		out.close();

		/*FileWriter fw = new FileWriter("binResp.txt");
			fw.append(Base64.getEncoder().encodeToString(packetToSend));
			fw.flush();
			fw.close();
		 */

		//System.out.println("len (bytes on line) :: " + packetToSend.length);	
		long end = System.nanoTime();
		//MainServer.logger.info("Droplet Bin HP : " + (end - start)  + " ns");

		response.flushBuffer();

	}

}
