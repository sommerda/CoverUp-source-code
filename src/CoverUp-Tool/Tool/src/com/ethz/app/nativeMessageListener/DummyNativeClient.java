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
package com.ethz.app.nativeMessageListener;

import java.io.File;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.json.JSONObject;

import com.ethz.app.env.ENV;

/**
 * @author Aritra
 *
 */
public class DummyNativeClient {

	public static void connectToBrowser(String data) throws Exception
	{
		Socket clientSocket = null;
		try
		{		
			clientSocket = new Socket("localhost", ENV.NATIVE_MESSAGE_LISTER_SERVER_PORT);
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
		
		String data = new String(Files.readAllBytes(new File("data.txt").toPath()), StandardCharsets.UTF_8);
		
		JSONObject job = new JSONObject();
		job.put("key", "BQVZ-tildem");
		job.put("origin", "coverup");
		job.put("value", data);
		
		connectToBrowser(job.toString());
		
		
		data = new String(Files.readAllBytes(new File("datat.txt").toPath()), StandardCharsets.UTF_8);
		
		job = new JSONObject();
		job.put("key", "BQVZ-tildem-table");
		job.put("origin", "coverup");
		job.put("value", data);
		
		connectToBrowser(job.toString());
	}
}
