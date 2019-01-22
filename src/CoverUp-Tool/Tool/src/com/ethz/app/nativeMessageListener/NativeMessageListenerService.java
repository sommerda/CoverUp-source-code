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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import com.ethz.app.env.ENV;

/**
 * @author Aritra
 *
 */
public class NativeMessageListenerService extends Thread {

	public static ServerSocket serverSocket;
	
	@Override
	public void run(){
		String clientSentence = null;
		try {
			serverSocket = new ServerSocket(ENV.NATIVE_MESSAGE_LISTER_SERVER_PORT);
		} 
		catch (IOException e) {
			System.err.println("---- error at creating message listener service ----\n");
			e.printStackTrace();
			return;
		}

		System.err.println("--------------------------------------");
		System.err.println("       Listner service started        ");
		System.err.println("--------------------------------------");
		while(true)
		{
			if(Thread.currentThread().isInterrupted())
				return;
			try
			{
				Socket connectionSocket = serverSocket.accept();
				BufferedReader messageFromClient =
						new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
				clientSentence = messageFromClient.readLine();
				JSONObject jObject = new JSONObject(clientSentence);
				String key = jObject.getString("key");
				System.err.println(">> Listener service : Received key: " +  key + " | Received data len : " + clientSentence.length());
				//System.out.println(">> Listener service : " + clientSentence);
				
				messageFromClient.close();
				NativeMessageDataHandler dataHandler = new NativeMessageDataHandler(jObject);
				dataHandler.insertToDB();
				
			}
			catch(JSONException jsonEx)
			{
				System.err.println(">> Listener service : Malformed json object; parse error");
				System.out.println(">> Listener service : " + clientSentence);
			}
			catch(Exception ex)
			{
				continue;
			}
		}
	}

	
	public static void stopServer()
	{
		try {
			serverSocket.close();
		} 
		catch (IOException e) {
			System.err.println(">> Listener service : Problem in closing the message listner service");
		}
	}
	
	//Test main
	public static void main(String[] args) throws IOException {
		NativeMessageListenerService service = new NativeMessageListenerService();
		service.run();
	}
}
