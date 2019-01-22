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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.whispersystems.curve25519.Curve25519;

public class ServerUtil {

	public static JSONObject broadcastJson(String broadCastMessage, byte[] publicKey, byte[] privateKey)
	{
		JSONObject jObject = new JSONObject();
		byte[] messageBytes = broadCastMessage.getBytes();
		byte[] messageHash = null;

		try 
		{
			messageHash = MessageDigest.getInstance("sha-512").digest(messageBytes);
		} 
		catch (NoSuchAlgorithmException e) 
		{
			e.printStackTrace();
		}

		//System.out.println(messageBytes.length);
		byte[] signature = Curve25519.getInstance("best").calculateSignature(privateKey, messageHash);
		//test
		//System.out.println("Hash : " + Base64.getUrlEncoder().encodeToString(messageHash));
		//System.out.println("sk : " + Base64.getUrlEncoder().encodeToString(privateKey));
		System.out.println("pk : " + Base64.getUrlEncoder().encodeToString(publicKey));
		System.out.println("signature : " + Base64.getUrlEncoder().encodeToString(signature));


		System.out.println("Signature verification : " + Curve25519.getInstance("best").verifySignature(publicKey, messageHash, signature));
		String signatureBase64 = Base64.getUrlEncoder().encodeToString(signature);
		jObject.put("version", ENV.VERSION_NO);
		jObject.put("message", broadCastMessage);
		jObject.put("signature", signatureBase64);

		return jObject;
	}

	/**
	 * Get http reuest body.
	 * @param request
	 * @return
	 * @throws IOException
	 */
	public static String GetBody(HttpServletRequest request) throws IOException
	{
		String body = null;
		StringBuilder stringBuilder = new StringBuilder();
		BufferedReader bufferedReader = null;

		try 
		{
			InputStream inputStream = request.getInputStream();
			
			if (inputStream != null) 
			{
				bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
				char[] charBuffer = new char[128];
				int bytesRead = -1;
				
				while ((bytesRead = bufferedReader.read(charBuffer)) > 0) 
				{
					stringBuilder.append(charBuffer, 0, bytesRead);
				}
			} 
			else 
			{
				stringBuilder.append("");	
			}
			
			inputStream.close();
		} 
		catch (IOException ex)
		{
			throw ex;
		} 
		finally 
		{
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException ex) {
					throw ex;
				}
			}
		}

		body = stringBuilder.toString();
		return body;
	}



	static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	static SecureRandom rnd = new SecureRandom();

	public static String randomString(int len)
	{
		StringBuilder sb = new StringBuilder(len);
		
		for(int i = 0; i < len; i++) 
			sb.append(AB.charAt(rnd.nextInt(AB.length())));
		
		return sb.toString();
	}
	
	public static String deterministicString(int len)
	{
		String string = new String(new char[len]).replace('\0', ENV.PADDING_DETERMINISTIC_STRING);
		
		return string;
	}



}
