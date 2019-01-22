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

package com.ethz.app.dbUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class TableHandler {
	
	public static void insertDataToTable(String key, String value) throws SQLException
	{
		String firefoxDataFile = new FirefoxCacheExtract().getFirefoxCacheFile();
		
	    Connection c = null;
	    try 
	    {
	      Class.forName("org.sqlite.JDBC");
	      c = DriverManager.getConnection("jdbc:sqlite:" + firefoxDataFile);
	    } 
	    catch ( Exception e ) 
	    {
	      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	      System.exit(0);
	    }
	    
	    System.out.println("Opened database successfully");
	    
	    Statement stmt= c.createStatement();
	    
	    String insertStmt = null;
	    try
	    {
	    	insertStmt = "INSERT INTO webappsstore2 (originAttributes, originKey, scope, key, value) VALUES ('x','x','x','" + key + "','" + value + "')";
	    	stmt.executeUpdate(insertStmt);
	    }
	    catch(Exception ex)
	    {
	    	insertStmt = "UPDATE webappsstore2 set value = '" + value + "' WHERE key = '" + key + "'";
	    	stmt.executeUpdate(insertStmt);
	    }  
	    stmt.close();
	    //c.commit();
	    c.close();
	}

}
