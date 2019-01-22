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

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.sqlite.SQLiteConfig;

import com.ethz.app.env.ENV;

/**
 * @author Aritra
 * Transfers Chrome's cache data base files in a local sqllite file located in APP_DATA. 
 * The local database file has identical schema structure with that of the firefox's cache file.
 */
public class ChromeCacheTransfer {

	String baseDir;

	public ChromeCacheTransfer(String baseDir) {	
		this.baseDir = baseDir;
	}

	public void transfer() throws IOException, ClassNotFoundException, SQLException
	{
		File[] files = new File(baseDir).listFiles();
		//System.out.println(baseDir);
		SQLiteConfig config = new SQLiteConfig();
		config.setReadOnly(true); 

		Connection replicatedFileconn = DriverManager.getConnection("jdbc:sqlite:" + ENV.REPLICATED_CHROME_DB);
		Statement innerStatement = replicatedFileconn.createStatement();

		innerStatement.executeUpdate("DELETE FROM webappsstore2");

		for(File file : files)
		{
			//System.out.println("Here");
			if(file.getCanonicalPath().contains("localstorage-journal") || file.toString().contains("__0.localstorage"))
				continue;		

			Class.forName("org.sqlite.JDBC");
			Connection c = DriverManager.getConnection("jdbc:sqlite:" + file.getCanonicalPath(), config.toProperties());

			Statement stmt = c.createStatement();
			ResultSet rs = null;
			try
			{
				rs = stmt.executeQuery( "SELECT * FROM itemTable WHERE key = \'" + ENV.DATABASE_TABLE_COL + "\';" );
			}
			catch(SQLException ex)
			{
				continue;
			}
			if(rs == null)
				continue;

			while(rs.next())
			{
				String key = rs.getString("key");
				String value = rs.getString("value");

				innerStatement.executeUpdate("INSERT INTO webappsstore2 (originKey, scope, key, value) "
						+ "VALUES ('" + file.getName() + "' , '" + file.getName() + "','" + key + "','" +  value + "');");
				//System.out.println(file);
			}

			try
			{
				rs = stmt.executeQuery( "SELECT * FROM itemTable WHERE key = \'" + ENV.DATABASE_DROPLET_COL + "\';" );
			}
			catch(SQLException ex)
			{
				continue;
			}
			if(rs == null)
				continue;

			while(rs.next())
			{
				String key = rs.getString("key");
				String value = rs.getString("value");

				innerStatement.executeUpdate("INSERT INTO webappsstore2 (originKey, scope, key, value) "
						+ "VALUES ('" + file.getName() + "' , '" + file.getName() + "','" + key + "','" +  value + "');");
			}

		}
	}
	//public static void main(String[] args) throws ClassNotFoundException, IOException, SQLException {

		//new ChromeCacheTransfer("C:\\Users\\Aritra\\AppData\\Local\\Google\\Chrome\\User Data\\Default\\Local Storage").transfer();
	//}
}
