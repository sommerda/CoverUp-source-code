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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;

import com.ethz.app.env.ENV;

/**
 * @author Aritra
 *
 */
public class TCPServer {

	public static void startServer() throws IOException
	{
		String clientSentence;
		String capitalizedSentence;
		ServerSocket welcomeSocket = new ServerSocket(6789);

		while(true)
		{
			try
			{
				Socket connectionSocket = welcomeSocket.accept();
				BufferedReader inFromClient =
						new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
				DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
				clientSentence = inFromClient.readLine();
				System.out.println("Received: " + clientSentence);
				capitalizedSentence = clientSentence.toUpperCase() + '\n';
				
				
				String intrLocation = ENV.APP_STORAGE_LOC + ENV.DELIM + ENV.APP_STORAGE_SLICE_ID_FILES_LOC + ENV.DELIM + ENV.APP_STORAGE_SLICE_ID_FILE;
				byte[] bytesToSend = {-1};
				if(new File(intrLocation).exists())
					bytesToSend = Files.readAllBytes(new File(intrLocation).toPath());
				
				outToClient.write(bytesToSend);
				outToClient.flush();
				outToClient.close();
			}
			catch(Exception ex)
			{
				continue;
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		
		startServer();
	}

}
