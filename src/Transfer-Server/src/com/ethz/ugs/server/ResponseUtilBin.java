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

/**
 * Response processing class. Provide resposne in byte stream.
 * @author Aritra
 *
 */
public class ResponseUtilBin {

	public static SecureRandom rand = new SecureRandom();

	/**
	 * P = fixed packet size
	 * <p>
	 * table-> P (4) | table_len (4) | table (n) | signature (64) | slice_table_len(4) | slice_table(n) | signature(64) | padding (P - 72 - n) |</p><p>
	 * signature is on table
	 * @param request HttpServletRequest
	 * @param response HttpServletResponse
	 * @param privateKey Server's Curve 25519 private key 
	 * @throws IOException
	 */

	public static void tablePleaseBin(HttpServletRequest request, HttpServletResponse response, byte[] privateKey) throws IOException
	{

		long start = System.nanoTime();
		response.addHeader("x-flag", "0");

		//get the fountain table
		String fountainTable = SiteMap.getTable();
		//get the slice tabel
		
		byte[] fixedPacketSizeBytes = ByteBuffer.allocate(Integer.BYTES).putInt(ENV.FIXED_PACKET_BASE_SIZE).array();
		byte[] theTableBytes = fountainTable.getBytes(StandardCharsets.UTF_8);
		//System.out.println("HT : " + Base64.getUrlEncoder().encodeToString(theTableBytes));
		byte[] signatureBytes = null;

		try 
		{
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] hashtableBytes = md.digest(theTableBytes);
			signatureBytes = Curve25519.getInstance("best").calculateSignature(privateKey, hashtableBytes);
		} 

		catch (NoSuchAlgorithmException e) 
		{
			e.printStackTrace();
			response.getWriter().append("Exception happed in crypto part!!");
			response.flushBuffer();
		}

		//P = fixed packet size
		//table-> table_len | table | signature | slice_table_len | slice_table | signature | padding |
		//			4			x		64		  		4       		y		 	  64     P-(136+x+y)
		byte[] packetToSend = new byte[ENV.FIXED_PACKET_SIZE_BIN];
		byte[] tableLenBytes = ByteBuffer.allocate(Integer.BYTES).putInt(theTableBytes.length).array();
		
		String sliceTable = InitialGen.sdm.getSliceTableAsJsonTree();
		byte[] sliceTableBytes = sliceTable.getBytes(StandardCharsets.UTF_8);
		byte[] sliceTableLenBytes = ByteBuffer.allocate(Integer.BYTES).putInt(sliceTableBytes.length).array();
		
		byte[] sliceSignatureBytes = null;
		try 
		{
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] hashtableBytes = md.digest(sliceTableBytes);
			sliceSignatureBytes = Curve25519.getInstance("best").calculateSignature(privateKey, hashtableBytes);
		} 

		catch (NoSuchAlgorithmException e) 
		{
			e.printStackTrace();
			response.getWriter().append("Exception happed in crypto part 2 !!");
			response.flushBuffer();
		}
		
		byte[] padding = new byte[ENV.FIXED_PACKET_SIZE_BIN - fixedPacketSizeBytes.length - 
		                          tableLenBytes.length - theTableBytes.length - signatureBytes.length -
		                          sliceTableLenBytes.length - sliceTableBytes.length - sliceSignatureBytes.length];

		if(ENV.RANDOM_PADDING)
			rand.nextBytes(padding);
		else
			Arrays.fill(padding, ENV.PADDING_DETERMINISTIC_BYTE);

		//System.out.println("table len " + theTableBytes.length);

		System.arraycopy(fixedPacketSizeBytes, 0, packetToSend, 0, fixedPacketSizeBytes.length);
		System.arraycopy(tableLenBytes, 0, packetToSend, fixedPacketSizeBytes.length, tableLenBytes.length);
		System.arraycopy(theTableBytes, 0, packetToSend, fixedPacketSizeBytes.length + tableLenBytes.length, theTableBytes.length);
		System.arraycopy(signatureBytes, 0, packetToSend, fixedPacketSizeBytes.length + tableLenBytes.length + theTableBytes.length, signatureBytes.length);
		int tillNow = fixedPacketSizeBytes.length + tableLenBytes.length + theTableBytes.length + signatureBytes.length;
		System.arraycopy(sliceTableLenBytes, 0, packetToSend, tillNow, sliceTableLenBytes.length);
		tillNow += sliceTableLenBytes.length;
		System.arraycopy(sliceTableBytes, 0, packetToSend, tillNow, sliceTableBytes.length);
		tillNow += sliceTableBytes.length;
		System.arraycopy(sliceSignatureBytes, 0, packetToSend, tillNow, sliceSignatureBytes.length);
		tillNow += sliceSignatureBytes.length;
		System.arraycopy(padding, 0, packetToSend, tillNow, padding.length);


		OutputStream out = response.getOutputStream();
		out.write(packetToSend);
		out.flush();
		out.close();
		//response.addHeader("x-flag", "0");

		/*
		 * FileWriter fw = new FileWriter("binResp.txt");
		fw.append(Base64.getEncoder().encodeToString(packetToSend));
		fw.flush();
		fw.close();
		 */
		//System.out.println("len (byte on line) :: " + packetToSend.length);
		//System.out.println(response.getHeader("x-flag"));

		long end = System.nanoTime();
		//MainServer.logger.info("Table : " + (end - start) + " ns");

		response.flushBuffer();
	}

	/**
	 * P = fixed packet size
	 * <br>
	 * table-> packet_len (4) | droplet (n) | url_len (4) | url (n_1) | f_id (8) | signature (64) | padding (p - 72 - n - n_1) |</br>
	 * 
	 * Signature is on  packet_len | droplet | url_len | url | f_id 
	 * <br>
	 * droplet -> seedlen (4) | seed(n) | num_chunk (4) | datalen (4) | data (n)
	 * <br>
	 * @param request HttpServletRequest
	 * @param response HttpServletResponse
	 * @param privateKey Server's Curve 25519 private key 
	 * @param fake for testing purpose
	 * @throws IOException
	 */

	public static void dropletPleaseBin(HttpServletRequest request, HttpServletResponse response, byte[] privateKey, boolean fake) throws IOException
	{
		long start = System.nanoTime();

		if(fake)
			response.addHeader("x-flag", "1");
		else
			response.addHeader("x-flag", "0");

		String url = request.getParameter("url");

		String[] dropletStr = new String[2];
		if(url == null)
		{
			//response.getWriter().append("Request contains no url id");
			//response.flushBuffer();
			//return;
			dropletStr = SiteMap.getRandomDroplet(null);
			url = dropletStr[1];
		}	
		else
		{
			System.err.println("Request droplet url : " + url);

			try
			{
				int urlId = Integer.parseInt(url);
				url = FountainTableRow.dropletLocUrlMap.get(urlId);

				if(url == null)
				{
					response.getWriter().append("Invalid fountain id");
					response.flushBuffer();

					return;
				}
			}
			catch(NullPointerException ex)
			{
				response.getWriter().append("Invalid fountain id");
				response.flushBuffer();

				return;
			}
			catch(Exception ex)
			{

			}
			try
			{
				dropletStr = SiteMap.getRandomDroplet(url);
			}
			catch(Exception ex)
			{
				response.getWriter().append(ex.getMessage());
				response.flushBuffer();

				return;
			}
			//System.out.println(dropletStr[0]);
		}

		//System.err.println("Fountain served : " + url);


		JSONObject jObject2 = new JSONObject(dropletStr[0]);
		byte[] seedBytes = Base64.getUrlDecoder().decode(jObject2.getString("seed"));
		byte[] seedLenBytes = ByteBuffer.allocate(Integer.BYTES).putInt(seedBytes.length).array();
		byte[] num_chunksBytes = ByteBuffer.allocate(Integer.BYTES).putInt(jObject2.getInt("num_chunks")).array();
		byte[] data = Base64.getUrlDecoder().decode(jObject2.getString("data"));
		byte[] dataLenBytes = ByteBuffer.allocate(Integer.BYTES).putInt(data.length).array();
		//System.out.println(dataLenBytes[3] + "," + dataLenBytes[2] + "," + dataLenBytes[1] + "," + dataLenBytes[0]);


		byte[] dropletByte = new byte[seedLenBytes.length + seedBytes.length + num_chunksBytes.length + dataLenBytes.length + data.length];

		System.arraycopy(seedLenBytes, 0, dropletByte, 0, seedLenBytes.length);
		System.arraycopy(seedBytes, 0, dropletByte, seedLenBytes.length, seedBytes.length);
		System.arraycopy(num_chunksBytes, 0, dropletByte, seedLenBytes.length + seedBytes.length, num_chunksBytes.length);
		System.arraycopy(dataLenBytes, 0, dropletByte, seedLenBytes.length + seedBytes.length + num_chunksBytes.length, dataLenBytes.length);
		System.arraycopy(data, 0, dropletByte, seedLenBytes.length + seedBytes.length + num_chunksBytes.length + dataLenBytes.length, data.length);

		byte[] fixedPacketLenBytes = ByteBuffer.allocate(Integer.BYTES).putInt(ENV.FIXED_PACKET_SIZE_BIN).array();

		byte[] urlBytes = url.getBytes(StandardCharsets.UTF_8);
		byte[] urlLenBytes = ByteBuffer.allocate(Integer.BYTES).putInt(urlBytes.length).array();
		byte[] f_idBytes = ByteBuffer.allocate(Long.BYTES).putLong(FountainTableRow.dropletLocUrlMapRev.get(url)).array();

		byte[] dataToSign = new byte[fixedPacketLenBytes.length + dropletByte.length + urlLenBytes.length + urlBytes.length + f_idBytes.length];

		System.arraycopy(fixedPacketLenBytes, 0, dataToSign, 0, fixedPacketLenBytes.length);
		System.arraycopy(dropletByte, 0, dataToSign, fixedPacketLenBytes.length, dropletByte.length);
		System.arraycopy(urlLenBytes, 0, dataToSign, fixedPacketLenBytes.length + dropletByte.length, urlLenBytes.length);
		System.arraycopy(urlBytes, 0, dataToSign, fixedPacketLenBytes.length + dropletByte.length + urlLenBytes.length, urlBytes.length);
		System.arraycopy(f_idBytes, 0, dataToSign, fixedPacketLenBytes.length + dropletByte.length + urlLenBytes.length + urlBytes.length, f_idBytes.length);

		byte[] signatureBytes = null;
		try 
		{

			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] hashtableBytes = md.digest(dataToSign);
			//System.out.println("hash : " + Base64.getUrlEncoder().encodeToString(hashtableBytes));
			signatureBytes = Curve25519.getInstance("best").calculateSignature(privateKey, hashtableBytes);
		} 

		catch (NoSuchAlgorithmException e) 
		{
			e.printStackTrace();
			response.getWriter().append("Exception in signature calculation!");
			response.flushBuffer();
		}


		byte[] padding = new byte[ENV.FIXED_PACKET_SIZE_BIN - dataToSign.length - signatureBytes.length];
		if(ENV.RANDOM_PADDING)
			rand.nextBytes(padding);
		else
			Arrays.fill(padding, ENV.PADDING_DETERMINISTIC_BYTE);

		byte[] packetToSend = new byte[ENV.FIXED_PACKET_SIZE_BIN];

		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//Dummy operation for same access pattern as the intr droplet serve
		String sliceData = InitialGen.sdm.getSlice();
		byte[] sliceDataBytes = null;
		
		if(sliceData.equals(SliceManager.INVALID_INDEX_OVERFLOW) || sliceData.equals(SliceManager.INVALID_SLICE_URL) || sliceData.equals(SliceManager.INVALID_SLICE_ERROR))
		{
			sliceDataBytes = new byte[ENV.FOUNTAIN_CHUNK_SIZE];
			Arrays.fill(sliceDataBytes, ENV.PADDING_DETERMINISTIC_BYTE);
		}
		
		else
			sliceDataBytes = Base64.getDecoder().decode(sliceData);
		System.arraycopy(sliceDataBytes, 0, packetToSend, 0, sliceDataBytes.length);
		//Dummy operation end
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		//dataToSign | signature | padding
		System.arraycopy(dataToSign, 0, packetToSend, 0, dataToSign.length);
		System.arraycopy(signatureBytes, 0, packetToSend, dataToSign.length, signatureBytes.length);
		System.arraycopy(padding, 0, packetToSend, dataToSign.length + signatureBytes.length, padding.length);

		OutputStream out = response.getOutputStream();
		out.write(packetToSend);
		out.flush();
		out.close();

		//test
		/*FileWriter fw = new FileWriter("binResp.txt");
		fw.append(Base64.getEncoder().encodeToString(packetToSend));
		fw.flush();
		fw.close();*/


		//System.out.println("len (bytes on line) :: " + packetToSend.length);
		//System.out.println("x-flag value : " + response.getHeader("x-flag"));
		long end = System.nanoTime();
		//MainServer.logger.info("Droplet Bin : " + (end - start)  + " ns");
		response.flushBuffer();
	}


	/**
	 * Intr droplet request
	 * 
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
		for(int i = 3; i < fountains.length; i++)
		{
			try
			{
				long fountainId = Long.parseLong(fountains[i]);
				String url = FountainTableRow.dropletLocUrlMap.get(fountainId);

				if(url == null)
				{
					response.getWriter().append("Invalid fountain id " + fountainId);
					response.flushBuffer();

					return;
				}
				//System.out.println("added" + url);
				fountainSet.add(url);
			}
			catch(Exception ex)
			{
				//System.out.println("added" + fountains[i]);
				if(!SiteMap.TABLE_MAP.containsKey(fountains[i]))
				{
					response.getWriter().append("Invalid fountain url " + fountains[i]);
					response.flushBuffer();

					return;
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
			response.getWriter().append("Exception in signature calculation!");
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


		//replace droplet data with teh slice data

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

				byte[] sliceLenBytes = ByteBuffer.allocate(Integer.BYTES).putInt(sliceDataBytes.length).array();

				byte[] sliceByte = new byte[seedLenBytes.length + seedBytes.length + num_chunksBytes.length + sliceLenBytes.length + sliceDataBytes.length];

				//make slice pack
				System.arraycopy(seedLenBytes, 0, sliceByte, 0, seedLenBytes.length);
				System.arraycopy(seedBytes, 0, sliceByte, seedLenBytes.length, seedBytes.length);
				System.arraycopy(num_chunksBytes, 0, sliceByte, seedLenBytes.length + seedBytes.length, num_chunksBytes.length);
				System.arraycopy(sliceLenBytes, 0, sliceByte, seedLenBytes.length + seedBytes.length + num_chunksBytes.length, sliceLenBytes.length);
				System.arraycopy(sliceDataBytes, 0, sliceByte, seedLenBytes.length + seedBytes.length + num_chunksBytes.length + sliceLenBytes.length, sliceDataBytes.length);

				byte[] sliceToSign = new byte[fixedPacketLenBytes.length + sliceByte.length + urlLenBytes.length + urlBytes.length + f_idBytes.length];

				System.arraycopy(fixedPacketLenBytes, 0, sliceToSign, 0, fixedPacketLenBytes.length);
				System.arraycopy(sliceByte, 0, sliceToSign, fixedPacketLenBytes.length, sliceByte.length);
				System.arraycopy(urlLenBytes, 0, sliceToSign, fixedPacketLenBytes.length + sliceByte.length, urlLenBytes.length);
				System.arraycopy(urlBytes, 0, sliceToSign, fixedPacketLenBytes.length + sliceByte.length + urlLenBytes.length, urlBytes.length);
				System.arraycopy(f_idBytes, 0, sliceToSign, fixedPacketLenBytes.length + sliceByte.length + urlLenBytes.length + urlBytes.length, f_idBytes.length);

				padding = new byte[ENV.FIXED_PACKET_SIZE_BIN - sliceToSign.length - signatureBytes.length];
				if(ENV.RANDOM_PADDING)
					rand.nextBytes(padding);
				else
					Arrays.fill(padding, ENV.PADDING_DETERMINISTIC_BYTE);

				packetToSend = new byte[ENV.FIXED_PACKET_SIZE_BIN];

				//dataToSign | signature | padding
				System.arraycopy(sliceToSign, 0, packetToSend, 0, sliceToSign.length);
				System.arraycopy(signatureBytes, 0, packetToSend, sliceToSign.length, signatureBytes.length);
				System.arraycopy(padding, 0, packetToSend, sliceToSign.length + signatureBytes.length, padding.length);
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
			//MainServer.logger.info("Droplet bin intr : " + (end - start) + " ns");

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
		long end = System.nanoTime();
		//MainServer.logger.info("Droplet Bin : " + (end - start)  + " ns");
		//System.out.println("len (bytes on line) :: " + packetToSend.length);	
		response.flushBuffer();		
	}

	
	/**This is for the probabilistic droplet serve call. x-flag and logging is removed
	 * P = fixed packet size
	 * <br>
	 * table-> packet_len (4) | droplet (n) | url_len (4) | url (n_1) | f_id (8) | signature (64) | padding (p - 72 - n - n_1) |</br>
	 * 
	 * Signature is on  packet_len | droplet | url_len | url | f_id 
	 * <br>
	 * droplet -> seedlen (4) | seed(n) | num_chunk (4) | datalen (4) | data (n)
	 * <br>
	 * @param request HttpServletRequest
	 * @param privateKey Server's Curve 25519 private key 
	 * @throws IOException
	 */

	public static byte[] dropletPleaseBinNew(HttpServletRequest request, byte[] privateKey, byte[] garbage) throws IOException
	{
		String url = request.getParameter("url");

		String[] dropletStr = new String[2];
		if(url == null)
		{
			dropletStr = SiteMap.getRandomDroplet(null);
			url = dropletStr[1];
		}	
		else
		{
			System.err.println("Request droplet url : " + url);

			try
			{
				int urlId = Integer.parseInt(url);
				url = FountainTableRow.dropletLocUrlMap.get(urlId);

				if(url == null)
					return null;
			}
			catch(Exception ex)
			{
				return null;
			}
			try
			{
				dropletStr = SiteMap.getRandomDroplet(url);
			}
			catch(Exception ex)
			{
				return null;
			}
			//System.out.println(dropletStr[0]);
		}

		//System.err.println("Fountain served : " + url);


		JSONObject jObject2 = new JSONObject(dropletStr[0]);
		byte[] seedBytes = Base64.getUrlDecoder().decode(jObject2.getString("seed"));
		byte[] seedLenBytes = ByteBuffer.allocate(Integer.BYTES).putInt(seedBytes.length).array();
		byte[] num_chunksBytes = ByteBuffer.allocate(Integer.BYTES).putInt(jObject2.getInt("num_chunks")).array();
		byte[] data = Base64.getUrlDecoder().decode(jObject2.getString("data"));
		byte[] dataLenBytes = ByteBuffer.allocate(Integer.BYTES).putInt(data.length).array();
		//System.out.println(dataLenBytes[3] + "," + dataLenBytes[2] + "," + dataLenBytes[1] + "," + dataLenBytes[0]);


		byte[] dropletByte = new byte[seedLenBytes.length + seedBytes.length + num_chunksBytes.length + dataLenBytes.length + data.length];

		System.arraycopy(seedLenBytes, 0, dropletByte, 0, seedLenBytes.length);
		System.arraycopy(seedBytes, 0, dropletByte, seedLenBytes.length, seedBytes.length);
		System.arraycopy(num_chunksBytes, 0, dropletByte, seedLenBytes.length + seedBytes.length, num_chunksBytes.length);
		System.arraycopy(dataLenBytes, 0, dropletByte, seedLenBytes.length + seedBytes.length + num_chunksBytes.length, dataLenBytes.length);
		System.arraycopy(data, 0, dropletByte, seedLenBytes.length + seedBytes.length + num_chunksBytes.length + dataLenBytes.length, data.length);

		byte[] fixedPacketLenBytes = ByteBuffer.allocate(Integer.BYTES).putInt(ENV.FIXED_PACKET_SIZE_BIN).array();

		byte[] urlBytes = url.getBytes(StandardCharsets.UTF_8);
		byte[] urlLenBytes = ByteBuffer.allocate(Integer.BYTES).putInt(urlBytes.length).array();
		byte[] f_idBytes = ByteBuffer.allocate(Long.BYTES).putLong(FountainTableRow.dropletLocUrlMapRev.get(url)).array();

		byte[] dataToSign = new byte[fixedPacketLenBytes.length + dropletByte.length + urlLenBytes.length + urlBytes.length + f_idBytes.length];

		System.arraycopy(fixedPacketLenBytes, 0, dataToSign, 0, fixedPacketLenBytes.length);
		System.arraycopy(dropletByte, 0, dataToSign, fixedPacketLenBytes.length, dropletByte.length);
		System.arraycopy(urlLenBytes, 0, dataToSign, fixedPacketLenBytes.length + dropletByte.length, urlLenBytes.length);
		System.arraycopy(urlBytes, 0, dataToSign, fixedPacketLenBytes.length + dropletByte.length + urlLenBytes.length, urlBytes.length);
		System.arraycopy(f_idBytes, 0, dataToSign, fixedPacketLenBytes.length + dropletByte.length + urlLenBytes.length + urlBytes.length, f_idBytes.length);

		byte[] signatureBytes = null;
		try 
		{

			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] hashtableBytes = md.digest(dataToSign);
			signatureBytes = Curve25519.getInstance("best").calculateSignature(privateKey, hashtableBytes);
		} 

		catch (NoSuchAlgorithmException e) 
		{
			e.printStackTrace();
			return garbage;
		}

		byte[] padding = new byte[ENV.FIXED_PACKET_SIZE_BIN - dataToSign.length - signatureBytes.length];
		if(ENV.RANDOM_PADDING)
			rand.nextBytes(padding);
		else
			Arrays.fill(padding, ENV.PADDING_DETERMINISTIC_BYTE);

		byte[] packetToSend = new byte[ENV.FIXED_PACKET_SIZE_BIN];

		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//Dummy operation for same access pattern as the intr droplet serve
		String sliceData = InitialGen.sdm.getSlice();
		byte[] sliceDataBytes = null;
		
		if(sliceData.equals(SliceManager.INVALID_INDEX_OVERFLOW) || sliceData.equals(SliceManager.INVALID_SLICE_URL) || sliceData.equals(SliceManager.INVALID_SLICE_ERROR))
		{
			sliceDataBytes = new byte[ENV.FOUNTAIN_CHUNK_SIZE];
			Arrays.fill(sliceDataBytes, ENV.PADDING_DETERMINISTIC_BYTE);
		}
		
		else
			sliceDataBytes = Base64.getDecoder().decode(sliceData);
		System.arraycopy(sliceDataBytes, 0, packetToSend, 0, sliceDataBytes.length);
		//Dummy operation end
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		//dataToSign | signature | padding
		System.arraycopy(dataToSign, 0, packetToSend, 0, dataToSign.length);
		System.arraycopy(signatureBytes, 0, packetToSend, dataToSign.length, signatureBytes.length);
		System.arraycopy(padding, 0, packetToSend, dataToSign.length + signatureBytes.length, padding.length);

		return packetToSend;
	}

}
