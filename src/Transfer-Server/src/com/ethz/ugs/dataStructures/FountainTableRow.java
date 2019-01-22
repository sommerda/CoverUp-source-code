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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.json.JSONObject;

import com.ethz.fountain.Fountain;
import com.ethz.ugs.server.ENV;

/**
 * Defines single row of the fountain table. This is used to create new fountain table row or load it from disk.
 * Contains all required field for a fountain to operate.
 * @author Aritra
 *
 */
public class FountainTableRow 
{
	//dropletloc->url mapping
	public static Map<Long, String> dropletLocUrlMap = new HashMap<>();
	//reverse mapping of the above one
	public static Map<String, Long> dropletLocUrlMapRev = new HashMap<>();
	
	String url;
	int num_chunks, chunk_size, datalenBeforPadding, droplet_count;
	byte[] data;
	Fountain fountain;
	String dropletLoc;
	byte[] seed;
	byte[] unchangedSeed;
	
	/**
	 * Create a new row in the fountain table
	 * @param url Url of the input data
	 * @param chunk_size chunk size of the fountain
	 * @param droplet_count number of droplets to generate
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 */
	public FountainTableRow(String url, int chunk_size, int droplet_count) throws IOException, NoSuchAlgorithmException, NoSuchProviderException 
	{
		this.url = url;
		this.chunk_size = chunk_size;
		this.droplet_count = droplet_count;
		File file = new File(this.url);
		this.data = Files.readAllBytes(file.toPath());

		//System.out.println("Size : " + data.length);
		
		this.seed = new byte[32];
		new Random().nextBytes(seed);
		
		this.unchangedSeed = new byte[32];
		System.arraycopy(this.seed, 0, this.unchangedSeed, 0, this.unchangedSeed.length);

		this.fountain = new Fountain(this.data, this.chunk_size, this.seed);
		this.num_chunks = fountain.num_chunks;
		this.datalenBeforPadding = fountain.dataLenBeforPadding;

		long l = new SecureRandom().nextLong();
		if(l < 0) l *= -1;
		this.dropletLoc = new Long(l).toString();
		//add this part to put those droplets inside a folder
		//this.dropletLoc = ENV.BROADCAST_DROPLET_LOC + ENV.DELIM + this.dropletLoc;
		File f = new File(dropletLoc);
		f.mkdir();
		
		dropletLocUrlMap.put(Long.parseLong(this.dropletLoc), this.url);
		dropletLocUrlMapRev.put(this.url, Long.parseLong(this.dropletLoc));
	}
	
	/**
	 * Reate a row of fountain table from already existing table file from the disk.
	 * @param jsonString Table in JSON format recovered from the disk
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 */
	public FountainTableRow(String jsonString) throws IOException, NoSuchAlgorithmException, NoSuchProviderException
	{
		JSONObject jObject = new JSONObject(jsonString);
		
		this.url = jObject.getString("url");
		this.num_chunks = jObject.getInt("num_chunks");
		this.chunk_size = jObject.getInt("chunk_size");
		this.seed = Base64.getUrlDecoder().decode(jObject.getString("seed"));
		this.unchangedSeed = Base64.getUrlDecoder().decode(jObject.getString("seed"));
		this.datalenBeforPadding = jObject.getInt("len");
		this.dropletLoc = jObject.getString("dropletLoc");
		
		File file = new File(this.url);
		this.data = Files.readAllBytes(file.toPath());
		this.fountain = new Fountain(this.data, this.chunk_size, this.seed);
	
		dropletLocUrlMap.put(Long.parseLong(this.dropletLoc), this.url);
		dropletLocUrlMapRev.put(this.url, Long.parseLong(this.dropletLoc));
	}
	
	/**
	 * Create droplets
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 */
	public void makeDroplets() throws IOException, NoSuchAlgorithmException, NoSuchProviderException
	{
		//String delm = (System.getProperty("os.name").contains("Windows")) ? "\\" : "/";

		for(int i = 0; i < this.droplet_count; i++)
		{
			FileWriter fw = new FileWriter(this.dropletLoc + ENV.DELIM + i  + ".json");
			fw.write(this.fountain.droplet().toString());
			fw.close();
		}
	}
	
	
	@Override
	public String toString() 
	{
		JSONObject jObject = new JSONObject();
		jObject.put("url", this.url);
		jObject.put("num_chunks", this.num_chunks);
		jObject.put("chunk_size", this.chunk_size);
		jObject.put("seed", Base64.getUrlEncoder().encodeToString(this.unchangedSeed));
		jObject.put("len", this.datalenBeforPadding);
		jObject.put("dropletLoc", this.dropletLoc);
		jObject.put("dropletCount", this.droplet_count);
		
		return jObject.toString(2);
	}
}
