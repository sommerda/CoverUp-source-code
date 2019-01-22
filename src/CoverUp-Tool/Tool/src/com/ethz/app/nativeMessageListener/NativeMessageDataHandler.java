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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.json.JSONException;
import org.json.JSONObject;

import com.ethz.app.env.ENV;

/**
 * @author Aritra
 * This handle the transfer of messages received from the native message listener 
 * service and transfer it to the replicated database identical to that of the firefox 
 * browser cache database
 */
public class NativeMessageDataHandler {

	JSONObject messageJSON;

	public NativeMessageDataHandler(JSONObject messageJSON)
	{
		this.messageJSON = messageJSON;
	}

	public void insertToDB()
	{
		PreparedStatement preparedStatement = null;
		try
		{
			String origin = this.messageJSON.getString("origin");
			String key = this.messageJSON.getString("key");
			String value = this.messageJSON.getString("value");

			Class.forName(ENV.JDBC_DRIVER);
			Connection connection = DriverManager.getConnection(ENV.JDBC_CONNECTION_STRING + ENV.REPLICATED_NATIVE_MESSGAE_DB);

			//check if there exists same key for the same origin
			preparedStatement = connection.prepareStatement("SELECT COUNT(*) from webappsstore2 where originKey = ? AND key = ?;");
			preparedStatement.setString(1, origin);
			preparedStatement.setString(2, key);			
			ResultSet rs = preparedStatement.executeQuery();
			int rowCounter = Integer.parseInt(rs.getString(1));
			
			//Delete
			if(rowCounter > 0)
			{
				preparedStatement = connection.prepareStatement("delete from webappsstore2 where originKey = ? and key = ?;");
				preparedStatement.setString(1, origin);
				preparedStatement.setString(2, key);	
				preparedStatement.executeUpdate();
			}
			
			//then do normal insert
			preparedStatement = connection.prepareStatement("INSERT INTO webappsstore2 (originKey, scope, key, value) VALUES (?, ?, ?, ?);");

			preparedStatement.setString(1, origin);
			preparedStatement.setString(2, origin);
			preparedStatement.setString(3, key);
			preparedStatement.setString(4, value);
			preparedStatement.executeUpdate();
			
			connection.close();
		}
		catch(JSONException ex)
		{
			System.err.println(">> Data handler service : json parsing error");
		}
		catch(Exception es)
		{
			System.err.println(">> Data handler service : Database write error");
			es.printStackTrace();
		}
	}
}
