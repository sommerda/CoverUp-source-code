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

package com.ethz.app;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Base64;

import org.json.JSONObject;
import org.whispersystems.curve25519.Curve25519;

import com.ethz.app.dbUtils.FirefoxCacheExtract;

public class MessageUtil {

	public String fileName;
	public byte[] ServerpublicKey;

	String version, message, signatureString;
	boolean verifyResult;

	String cacheLocation;
	
	/*public App(String fileName)
	{
		this.verifyResult = false;
		this.fileName = fileName;
		this.ServerpublicKey = Base64.getUrlDecoder().decode("jMpEEfoSc2iOgRXeLQnLN1YKtjcyN824yso3psylEU0=");
	}*/
	public MessageUtil()
	{
		this.verifyResult = false;
		//this.ServerpublicKey = Base64.getUrlDecoder().decode("jMpEEfoSc2iOgRXeLQnLN1YKtjcyN824yso3psylEU0=");
	}
	public MessageUtil(String cacheLocation)
	{
		this.verifyResult = false;
		this.cacheLocation = cacheLocation;
	}

	public void setPK(String pk)
	{
		this.ServerpublicKey = Base64.getUrlDecoder().decode(pk);

	}

	public void loadMessage() throws SQLException
	{
		FirefoxCacheExtract fc = new FirefoxCacheExtract();
		fc.connectDatabase(this.cacheLocation, false);
		
		String jsonData = fc.jsonData;

		JSONObject jObject = new JSONObject(jsonData);
		this.message = jObject.getString("message").toString();
	}
	
	public void loadSignature() throws SQLException
	{
		FirefoxCacheExtract fc = new FirefoxCacheExtract();
		fc.connectDatabase(this.cacheLocation, false);
		String jsonData = fc.jsonData;

		JSONObject jObject = new JSONObject(jsonData);
		this.signatureString = jObject.getString("signature");
	}
	
	
	public void extractMessageFireFox() throws SQLException, NoSuchAlgorithmException
	{
		FirefoxCacheExtract fc = new FirefoxCacheExtract();
		fc.connectDatabase(this.cacheLocation, false);
		String jsonData = fc.jsonData;

		JSONObject jObject = new JSONObject(jsonData);

		this.signatureString = jObject.getString("signature");
		this.message = jObject.getString("message").toString();
		this.version = jObject.getString("version").toString();

		byte[] messageBytes = this.message.getBytes();
		byte[] messageHash = MessageDigest.getInstance("sha-512").digest(messageBytes);

		byte[] signatureBytes = Base64.getUrlDecoder().decode(this.signatureString);

		if(!Curve25519.getInstance("best").verifySignature(ServerpublicKey, messageHash, signatureBytes))
		{
			throw new RuntimeException("SIgnature is not verified");
		}
		else
		{
			this.verifyResult = true;
			System.err.println("Success!");
		}

		//System.out.println(this.message);
	}
	
	public void verifyMessage() throws Exception
	{
		byte[] messageBytes = this.message.getBytes();
		byte[] messageHash = MessageDigest.getInstance("sha-512").digest(messageBytes);
		byte[] signatureBytes = Base64.getUrlDecoder().decode(this.signatureString);

		if(!Curve25519.getInstance("best").verifySignature(ServerpublicKey, messageHash, signatureBytes))
		{
			throw new RuntimeException("SIgnature is not verified");
		}
		else
		{
			this.verifyResult = true;
			System.err.println("Success!");
		}
	}

	public void extractMessage() throws IOException, NoSuchAlgorithmException
	{
		BufferedReader br = new BufferedReader(new FileReader(this.fileName));

		StringBuffer sb = new StringBuffer();
		String st = null;

		while((st = br.readLine()) != null)
		{
			sb.append(st);
		}
		br.close();

		JSONObject jObject = new JSONObject(sb.toString());

		this.signatureString = jObject.getString("signature");
		this.message = jObject.getString("message").toString();
		this.version = jObject.getString("version").toString();

		byte[] messageBytes = this.message.getBytes();
		byte[] messageHash = MessageDigest.getInstance("sha-512").digest(messageBytes);

		byte[] signatureBytes = Base64.getUrlDecoder().decode(this.signatureString);

		if(!Curve25519.getInstance("best").verifySignature(ServerpublicKey, messageHash, signatureBytes))
		{
			throw new RuntimeException("SIgnature is not verified");
		}
		else
		{
			this.verifyResult = true;
			System.out.println("Success!");
		}
	}

	/*
	public static void main(String[] args) throws NoSuchAlgorithmException, IOException, SQLException {

		App app = new App();
		//app.extractMessage();
		app.extractMessageFireFox();
	}
	 */
}
