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
package com.ethz.app.chatApp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Base64;

import com.ethz.app.env.ENV;

/**
 * @author Aritra
 *
 */
public class IncomingChatPoll {


	public static void pollChat()
	{
		Connection c = null;
		try 
		{				
			Class.forName(ENV.JDBC_DRIVER);
			c = DriverManager.getConnection(ENV.JDBC_CONNECTION_STRING + ENV.APP_STORAGE_INCOMING_CHAT_DATABASE_FILE);
		} 
		catch ( Exception e ) 
		{
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			return;
		}
		Statement stmt;
		try {
			stmt = c.createStatement();
			
			ResultSet rs = stmt.executeQuery("SELECT * FROM incoming_chat;" );
			//read all the rows from the database and store them in the proper chat location
			while(rs.next())
			{
				String senderAddress = rs.getString("sender");
				System.out.println(senderAddress);
				String _data = rs.getString("data");
				String data = new String(Base64.getMimeDecoder().decode(_data), StandardCharsets.UTF_8);
				String saveLocStr = ENV.APP_STORAGE_LOC + ENV.DELIM + ENV.APP_STORAGE_CHAT_LOC + ENV.DELIM + 
						ENV.APP_STORAGE_CHAT_LOG_LOC + ENV.DELIM + senderAddress;
				File saveLoc = new File(saveLocStr);
				if(!saveLoc.exists())
					saveLoc.mkdir();
				
				//if(!new File(saveLocStr + ENV.DELIM + ENV.APP_STORAGE_CHAT_REPO_FILE).exists())
				//	new File(saveLocStr + ENV.DELIM + ENV.APP_STORAGE_CHAT_REPO_FILE).createNewFile();
				
				//check for duplicate messages
				String messageToInsert = "---- Received start ----\n[" + senderAddress + "] : " + data + "\n ---- Received end ----\n";
				BufferedReader br = new BufferedReader(new FileReader(saveLocStr + ENV.DELIM + ENV.APP_STORAGE_CHAT_REPO_FILE));
				StringBuffer stb = new StringBuffer();
				String str = null;
				while((str = br.readLine()) != null)
					stb.append(str).append("\n");
				// + 3 IS FOR THE 3 NEW LINE
				String lastEnteredMessage = stb.substring(stb.length() - messageToInsert.length(), stb.length());
				br.close();
				
				System.out.println(lastEnteredMessage.length());
				System.out.println(messageToInsert.length());
				if(lastEnteredMessage.equals(messageToInsert))					
					System.out.println("Duplicate message dectected");
				
				else
				{
					FileWriter fw = new FileWriter(saveLocStr + ENV.DELIM + ENV.APP_STORAGE_CHAT_REPO_FILE, true);
					fw.append("---- Received start ----\n[" + senderAddress + "] : " + data + "\n ---- Received end ----\n");
					fw.close();
				}
			}
			rs.close();
			//delete all the rows from the datebaseee
			stmt.executeUpdate("DELETE FROM incoming_chat;");
			stmt.close();
			c.close();
		} catch (SQLException | IOException e) {
			e.printStackTrace();
		}
		

	}
}
