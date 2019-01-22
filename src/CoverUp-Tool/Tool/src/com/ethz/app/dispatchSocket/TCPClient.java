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
package com.ethz.app.dispatchSocket;

import java.io.DataOutputStream;
import java.io.File;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.ethz.app.env.ENV;

/**
 * @author Aritra
 *
 */
public class TCPClient {

	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	
	public static String bytesToHex(byte[] bytes) 
	{
	    char[] hexChars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) 
	    {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	
	/**
	 * Sends data as byte array
	 * @param data data to be send in byte stream format
	 * @throws Exception
	 */
	public static void connectToBrowser(byte[] data) throws Exception
	{
		Socket clientSocket = null;
		try
		{		
			clientSocket = new Socket("localhost", ENV.NATIVE_MESSAGE_PORT);
			clientSocket.setSoTimeout(5000);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			throw new RuntimeException(ENV.EXCEPTION_BROWSER_EXTENSION_MISSING);
		}
		DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream()); 
		//outToServer.writeBytes(bytesToHex(data) + "\n");
		outToServer.write(data);
		outToServer.flush();
		System.out.println(System.currentTimeMillis());
		//System.out.println(Base64.getEncoder().encodeToString(data));
		outToServer.close();
		clientSocket.close();
	}
	/**
	 * Sends data as string
	 * @param data data to be send in String format
	 * @throws Exception
	 */
	public static void connectToBrowser(String data) throws Exception
	{
		Socket clientSocket = null;
		try
		{		
			clientSocket = new Socket("localhost", ENV.NATIVE_MESSAGE_PORT);
			clientSocket.setSoTimeout(5000);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			throw new RuntimeException(ENV.EXCEPTION_BROWSER_EXTENSION_MISSING);
		}
		OutputStreamWriter outToServer = new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"); 
		outToServer.write(data, 0, data.length());
		outToServer.flush();
		outToServer.close();
		clientSocket.close();
	}

	public static void main(String[] args) throws Exception {

		//covert browsing data
	/*	byte[] data = Files.readAllBytes(new File(ENV.APP_STORAGE_LOC + ENV.DELIM + 
				ENV.APP_STORAGE_SLICE_ID_FILES_LOC + ENV.DELIM + ENV.APP_STORAGE_SLICE_ID_FILE).toPath());*/
		
		//covert chat data
		byte[] data = Files.readAllBytes(new File("C:\\Users\\Aritra\\workspace_Mars_new\\UndergroundApp\\APP_DATA\\Chat\\Dispatch\\R_KHQIumdX8=\\CHAT_ENC.bin").toPath());
		
		
		//data = new byte[32];
		//testing for normal droplets
		//Arrays.fill(data, (byte)0x01);
		
		ScheduledExecutorService execService
		= Executors.newScheduledThreadPool(50);


		execService.scheduleAtFixedRate(()->{
			try {
				connectToBrowser(data);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}, 0, 500L, TimeUnit.MILLISECONDS);
	}
}
