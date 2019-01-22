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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.whispersystems.curve25519.Curve25519;

import com.ethz.ugs.compressUtil.CompressUtil;
import com.ethz.ugs.dataStructures.FountainTableRow;
import com.ethz.ugs.dataStructures.SiteMap;
import com.ethz.ugs.dataStructures.SliceManager;
import com.ethz.ugs.test.InitialGen;

/**
 * Response processing class
 * @author Aritra
 *	
 */
public class ResponseUtil 
{
	/**
	 * Table request
	 * @param request HttpServletRequest
	 * @param response HttpServletResponse
	 * @param privateKey Server curve25519 private key
	 * @throws IOException
	 */
	public static void tablePlease(HttpServletRequest request, HttpServletResponse response, byte[] privateKey) throws IOException
	{
		//System.out.println("Table request from : " + request.getRemoteAddr());
		
		JSONObject jObject = new JSONObject();

		String theTable = SiteMap.getTable();


		byte[] theTableBytes = theTable.getBytes(StandardCharsets.UTF_8);
		byte[] signatureBytes = null;
		String signatureBase64 = null;

		try 
		{

			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] hashtableBytes = md.digest(theTableBytes);
			signatureBytes = Curve25519.getInstance("best").calculateSignature(privateKey, hashtableBytes);
			signatureBase64 = Base64.getUrlEncoder().encodeToString(signatureBytes);
		} 

		catch (NoSuchAlgorithmException e) 
		{
			e.printStackTrace();
			response.getWriter().append("Exception happed in crypto part!!");
			response.flushBuffer();
		}

		String sliceTable = InitialGen.sdm.getSliceTableAsJsonTree();
		byte[] sliceTableBytes = sliceTable.getBytes(StandardCharsets.UTF_8);
		
		byte[] sliceSignatureBytes = null;
		String sliceSignatureBase64 = null;
		try 
		{
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] hashtableBytes = md.digest(sliceTableBytes);
			sliceSignatureBytes = Curve25519.getInstance("best").calculateSignature(privateKey, hashtableBytes);
			sliceSignatureBase64 = Base64.getUrlEncoder().encodeToString(sliceSignatureBytes);
		} 

		catch (NoSuchAlgorithmException e) 
		{
			e.printStackTrace();
			response.getWriter().append("Exception happed in crypto part 2 !!");
			response.flushBuffer();
		}
		

		jObject.put("table", theTable);
		jObject.put("signature", signatureBase64);
		jObject.put("sliceTable", sliceTable);
		jObject.put("sliceTableSignature", sliceSignatureBase64);
		
		String responseString = jObject.toString();

		if(ENV.PADDING_ENABLE)
		{
			int padLen = ENV.FIXED_PACKET_SIZE - responseString.length();
			String stringPadding = null;
			
			if(ENV.RANDOM_PADDING)
				stringPadding = ServerUtil.randomString(padLen);
			else
				stringPadding = ServerUtil.deterministicString(padLen);
			
			jObject.put("pad", stringPadding);
		}

		if(ENV.ENABLE_COMPRESS)
		{
			byte[] bytes = jObject.toString().getBytes();

			OutputStream output = response.getOutputStream();
			output.write(CompressUtil.compress(bytes, ENV.COMPRESSION_PRESET));
			output.flush();
			output.close();
		}

		else
			response.getWriter().append(jObject.toString());

		//response.addHeader("x-flag", "0");
		
		System.out.println("len (String) :: " + jObject.toString().length());
		
		response.flushBuffer();

	}
	
	
	/**
	 * normal droplet request
	 * @param request HttpServletRequest
	 * @param response HttpServletResponse
	 * @param privateKey Server curve25519 private key
	 * @throws IOException
	 */
	public static void dropletPlease(HttpServletRequest request, HttpServletResponse response, byte[] privateKey) throws IOException
	{
			
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
		
		System.err.println("Fountain served : " + url);
		
		JSONObject jObject = new JSONObject();

		//sign droplet|url

		String dropletStrMod = dropletStr[0].concat(url);

		byte[] dropletByte = dropletStrMod.getBytes(StandardCharsets.UTF_8);
		byte[] signatureBytes = null;
		String signatureBase64 = null;


		System.out.println("Droplet " + dropletByte.length);
		try 
		{

			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] hashtableBytes = md.digest(dropletByte);

			System.out.println("hash : " + Base64.getUrlEncoder().encodeToString(hashtableBytes));

			signatureBytes = Curve25519.getInstance("best").calculateSignature(privateKey, hashtableBytes);
			signatureBase64 = Base64.getUrlEncoder().encodeToString(signatureBytes);
		} 

		catch (NoSuchAlgorithmException e) 
		{
			e.printStackTrace();
			response.getWriter().append("Exception in signature calculation!");
			response.flushBuffer();
		}


		jObject.put("url", url);
		jObject.put("f_id", FountainTableRow.dropletLocUrlMapRev.get(url));
		jObject.put("droplet", dropletStr[0]);
		jObject.put("signature", signatureBase64);	

		if(ENV.PADDING_ENABLE)
		{
			String responseString = jObject.toString();
			int padLen = ENV.FIXED_PACKET_SIZE - responseString.length();
			String stringPadding = null;
			
			if(ENV.RANDOM_PADDING)
				stringPadding = ServerUtil.randomString(padLen);
			else
				stringPadding = ServerUtil.deterministicString(padLen);
			
			jObject.put("pad", stringPadding);
		}
		
		
		if(ENV.ENABLE_COMPRESS)
		{
			byte[] bytes = jObject.toString().getBytes();

			OutputStream output = response.getOutputStream();
			output.write(CompressUtil.compress(bytes, ENV.COMPRESSION_PRESET));
			output.flush();
			output.close();
		}
		else
			response.getWriter().append(jObject.toString());
		
		//response.addHeader("x-flag", "0");
		
		System.out.println("len (String) :: " + jObject.toString().length());	
		response.flushBuffer();
	}
	
	/**
	 * Intr droplet request
	 * @param request HttpServletRequest
	 * @param response HttpServletResponse
	 * @param privateKey Server curve25519 private key
	 * @throws IOException
	 */
	public static void dropletPleaseIntr(HttpServletRequest request, HttpServletResponse response, byte[] privateKey, String requestBody) throws IOException
	{
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
		
		JSONObject jObject = new JSONObject();
		String dropletStrMod = dropletStr[0].concat(url);

		byte[] dropletByte = dropletStrMod.getBytes(StandardCharsets.UTF_8);
		byte[] signatureBytes = null;
		String signatureBase64 = null;
		
		try 
		{

			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] hashtableBytes = md.digest(dropletByte);

			System.out.println("hash : " + Base64.getUrlEncoder().encodeToString(hashtableBytes));

			signatureBytes = Curve25519.getInstance("best").calculateSignature(privateKey, hashtableBytes);
			signatureBase64 = Base64.getUrlEncoder().encodeToString(signatureBytes);
		} 

		catch (NoSuchAlgorithmException e) 
		{
			e.printStackTrace();
			response.getWriter().append("Exception in signature calculation!");
			response.flushBuffer();
		}


		jObject.put("url", url);
		jObject.put("f_id", FountainTableRow.dropletLocUrlMapRev.get(url));
		
		//System.out.println("url : " + url);
		if(fountainSet.contains(url))
		{
			JSONObject oldDroplet = new JSONObject(dropletStr[0]);
			
			JSONObject dropletJObject = new JSONObject();
			dropletJObject.put("seed", oldDroplet.getString("seed"));
			
			//this is to be manipulated by intrFountainId
			String sliceData = InitialGen.sdm.getSlice(intrSliceId, sliceIndex);
			
			//some stupid code here. But I will fix this later
			
			if(sliceData.equals(SliceManager.INVALID_INDEX_OVERFLOW))
				response.addHeader("x-flag", "2");
			
			else if(sliceData.equals(SliceManager.INVALID_SLICE_URL))
				response.addHeader("x-flag", "3");
			
			else if(sliceData.equals(SliceManager.INVALID_SLICE_ERROR))
				response.addHeader("x-flag", "4");
			
			else
				response.addHeader("x-flag", "1");
/*			if(sliceData.equals(SliceManager.INVALID_SLICE_URL) ||
					sliceData.equals(SliceManager.INVALID_SLICE_FILE) ||
					sliceData.equals(SliceManager.INVALID_SLICE_URL)
					)
				
				dropletJObject.put("data", sliceData);
*/						
			dropletJObject.put("data", sliceData);
			
			dropletJObject.put("num_chunks", oldDroplet.get("num_chunks"));
			
			jObject.put("droplet", dropletJObject.toString());
			//response.addHeader("x-flag", "1");
			
			System.out.println("here");
		}
		
		else
		{
			jObject.put("droplet", dropletStr[0]);
			response.addHeader("x-flag", "0");
			
			System.out.println("here out");
		}
		
		jObject.put("signature", signatureBase64);	
		//mandetory padding
		if(ENV.PADDING_ENABLE)
		{
			String responseString = jObject.toString();
			int padLen = ENV.FIXED_PACKET_SIZE - responseString.length();
			
			String stringPadding = null;
			if(ENV.RANDOM_PADDING)
				stringPadding = ServerUtil.randomString(padLen);
			else
				stringPadding = ServerUtil.deterministicString(padLen);
			
			jObject.put("pad", stringPadding);
		}
		
		response.getWriter().append(jObject.toString());
		response.flushBuffer();
	}
	
	public static void broadCastjson(HttpServletRequest request, HttpServletResponse response, String broadCastMessage, byte[] publicKey, byte[] privateKey) throws IOException
	{
		Stats.TOTAL_CONNECTIONS++;
		Stats.LIVE_CONNECTIONS++;

		String requestBody = ServerUtil.GetBody(request);

		//System.out.println("Body " + requestBody);

		JSONObject jObject = null;
		if(requestBody.length() == 0)
			jObject = ServerUtil.broadcastJson(broadCastMessage, publicKey, privateKey);


		else if(requestBody.equals("tableRequest"))
			jObject = new JSONObject(SiteMap.SITE_MAP);

		if(ENV.ENABLE_COMPRESS)
		{
			byte[] bytes = jObject.toString(2).getBytes();

			OutputStream output = response.getOutputStream();
			output.write(CompressUtil.compress(bytes, ENV.COMPRESSION_PRESET));
			output.flush();
			output.close();
		}

		else
			response.getWriter().append(jObject.toString(2));

		response.addHeader("x-flag", "0");
		response.flushBuffer();
	}
	public static void broadCast(HttpServletRequest request, HttpServletResponse response, String broadCastMessage) throws IOException
	{
		Stats.TOTAL_CONNECTIONS++;
		Stats.LIVE_CONNECTIONS++;

		response.getWriter().append(broadCastMessage);
		response.flushBuffer();
	}
	
	public static void randB(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		Stats.TOTAL_CONNECTIONS++;
		Stats.LIVE_CONNECTIONS++;

		int responseSize = 256;
		SecureRandom rand = new SecureRandom();
		byte[] toSent = new byte[responseSize];
		rand.nextBytes(toSent);
		String responseStr = Base64.getUrlEncoder().encodeToString(toSent);

		response.getWriter().append(responseStr);
		response.flushBuffer();
	}
	
	/**
	 * Produce a random resopse of specified size.
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	public static void rand(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		Stats.TOTAL_CONNECTIONS++;
		Stats.LIVE_CONNECTIONS++;

		int responseSize = 256;
		SecureRandom rand = new SecureRandom();
		byte[] toSent = new byte[responseSize];
		rand.nextBytes(toSent);

		OutputStream output = response.getOutputStream();
		output.write(toSent);
		output.flush();
		response.flushBuffer();
	}
	
	/**
	 * EC base DHKE.
	 * @param request
	 * @param response
	 * @param publicKey
	 * @param privateKey
	 * @param sharedSecret
	 * @param sharedSecretMap
	 * @throws IOException
	 */
	public static void ke(HttpServletRequest request, HttpServletResponse response, byte[] publicKey, byte[] privateKey, byte[] sharedSecret, Map<String, byte[]> sharedSecretMap) throws IOException
	{
		Stats.TOTAL_CONNECTIONS++;
		Stats.LIVE_CONNECTIONS++;

		String otherPublicKey = request.getParameter("pk");
		String sessionCode = request.getParameter("code");

		sharedSecret = Curve25519.getInstance("best").calculateAgreement(Base64.getUrlDecoder().decode(otherPublicKey), privateKey);
		response.getWriter().append(Base64.getUrlEncoder().encodeToString(publicKey));

		byte[] sharedSecretHash = null;
		try {
			sharedSecretHash = MessageDigest.getInstance("sha-256").digest(sharedSecret);
		}
		catch (NoSuchAlgorithmException e) {

		}

		sharedSecretMap.put(sessionCode, sharedSecretHash);

		System.out.println(Base64.getUrlEncoder().encodeToString(sharedSecretHash));
	}
}
