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

package com.ethz.ugs.dataStructures;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;

import com.ethz.ugs.server.ENV;

/**
 * Contains site map implemented on a HashMap
 * @author Aritra
 *
 */
public class SiteMap {

	//url/file location -> row of the table
	public static Map<String, FountainTableRow> TABLE_MAP = new HashMap<>();
	//title -> link
	public static Map<String, String> SITE_MAP = new HashMap<>();
	public static boolean updated = false;
	public static volatile String TABLE_STRING;
	
	public static void insertRowToTable(String url, FountainTableRow tableRow) throws IOException
	{
		if(SiteMap.TABLE_MAP.containsKey(url))
		{
			System.out.println("Row added with url : " + tableRow.url + " already exists");
			return;
		}
		
		
		SiteMap.TABLE_MAP.put(url, tableRow);
		saveTable();
		updated = false;
		
		System.out.println("Row added with url : " + tableRow.url);
	}
	
	/**
	 * 
	 * @return Table from local storage.
	 * @throws IOException
	 */
	public static String getTable() throws IOException
	{
		if(updated)
			return TABLE_STRING;
		else
		{
			saveTable();
			
			BufferedReader br = new BufferedReader(new FileReader(ENV.SITE_TABLE_LOC));
			String st;
			StringBuffer stb = new StringBuffer();
			
			while((st = br.readLine()) != null)
				stb.append(st);
			
			br.close();
			
			TABLE_STRING = stb.toString();
			updated = true;
			
			return TABLE_STRING;
		}
	}
	
	public static void saveTable() throws IOException
	{
		updated = false;
		
		FileWriter fw = new FileWriter(ENV.SITE_TABLE_LOC);
		//System.out.println(new File(ENV.SITE_TABLE_LOC).getAbsolutePath());
		JSONObject jObject = new JSONObject();
		
		JSONArray jArray = new JSONArray();
		
		for(String key : TABLE_MAP.keySet())
		{
			JSONObject inJo = new JSONObject();
			inJo.put("key", key);
			inJo.put("value", TABLE_MAP.get(key).toString());
			jArray.put(inJo);
		}
		jObject.put("table", jArray);
		
		fw.write(jObject.toString(2));
		fw.close();
	}
	
	/**
	 * Validation check is due
	 * @return
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 */
	public static boolean loadTable() throws IOException, NoSuchAlgorithmException, NoSuchProviderException
	{
		updated = false;
		
		TABLE_MAP = new HashMap<>();
		
		File file = new File(ENV.SITE_TABLE_LOC);
		if(!file.exists())
		{
			System.out.println("Site table not found. Regenerating");
			file.createNewFile();
			updated = false;
			return true;
		}
		
		BufferedReader br = new BufferedReader(new FileReader(ENV.SITE_TABLE_LOC));
		String st = null;
		StringBuffer stb = new StringBuffer();
		
		while((st = br.readLine())!= null)
			stb.append(st);
		
		System.out.println(stb);
		
		JSONObject jObject = new JSONObject(stb.toString());
		
		JSONArray jarray = jObject.getJSONArray("table");
		
		for(int i = 0; i < jarray.length(); i++)
		{
			JSONObject inObj = jarray.getJSONObject(i);
			String url = inObj.getString("key");
			String tabRowStr = inObj.getString("value");
			FountainTableRow deserializedTableRow = new FountainTableRow(tabRowStr);
			
			TABLE_MAP.put(url, deserializedTableRow);
		}
		
		br.close();
		
		return false;
	}
	
	/**
	 * Here is your nice random droplet. Enjoy!
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public static String[] getRandomDroplet(String url) throws IOException
	{		
		FountainTableRow row = null;
		if(url == null)
		{
			int index = new Random().nextInt(SiteMap.TABLE_MAP.size());
			int i = 0;
			for(String urlInternal : SiteMap.TABLE_MAP.keySet())
			{
				if(i == index)
				{
					row = SiteMap.TABLE_MAP.get(urlInternal);
					url = urlInternal;
					break;
				}
				i++;
			}
		}
		else
			row = SiteMap.TABLE_MAP.get(url);
		
		if(row == null)
			throw new RuntimeException("url : " + url + " is invalid");
		
		String dropletLocation = row.dropletLoc;
		
		File dropletDir = new File(dropletLocation);

		if(!dropletDir.isDirectory())
			throw new RuntimeException("Error in droplet dir!");
		
		//get a random droplet file
		File randDropletFile = dropletDir.listFiles()[new Random().nextInt(dropletDir.listFiles().length)];
		
		BufferedReader br = new BufferedReader(new FileReader(randDropletFile));
		
		String st = null;
		StringBuffer stb = new StringBuffer("");
		
		while((st = br.readLine()) != null)
			stb.append(st);
		
		br.close();
		
		return new String[]{stb.toString(), url};
	}
	
	//old test code
	/////////////////////////////////////////////////////////
	public static void inserToSiteMap(String title, String link)
	{
		SiteMap.SITE_MAP.put(title, link);
	}
	
	
	//random initialization for testing
	public static void randomInitialization(int enrty)
	{
		byte[] randBytes1 = new byte[32];
		byte[] randBytes2 = new byte[32];
		Random rand = new Random();
		
		for(int i = 0; i < enrty; i++)
		{
			rand.nextBytes(randBytes1);
			rand.nextBytes(randBytes2);
			
			inserToSiteMap(Base64.getUrlEncoder().encodeToString(randBytes1), Base64.getUrlEncoder().encodeToString(randBytes2));
		}
	}
}
