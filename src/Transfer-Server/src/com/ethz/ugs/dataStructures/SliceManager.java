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
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONObject;

import com.ethz.tree.Tree;
import com.ethz.ugs.compressUtil.SliceData;
import com.ethz.ugs.server.ENV;

/**
 * This class handles the slicing of the data for interactive browing
 * @author Aritra
 *
 */
public class SliceManager 
{
	//slice url -> fragment location (id)
	public static Map<String, Long> SLICE_MAP = new HashMap<>();
	
	public static final String INVALID_SLICE_URL = "invalid slice url";
	public static final String INVALID_INDEX_OVERFLOW = "slice index overflow";
	public static final String INVALID_SLICE_ERROR = "unknown error related to I/O";
	public long firstSliceId;
	/**
	 * Initiate {@code SliceManager} with {@code chunk_size}
	 * @param chunk_size In bytes. Should be same as the droplet chunk size. This also checks for existing slice table.
	 * Needs more optimization later on.
	 * @throws IOException
	 */
	public SliceManager(int chunk_size) throws IOException 
	{
		if(loadSliceTable())
		{
			System.out.println("Slice dir loaded from table");
			return;
		}
		
		File files = new File(ENV.INTR_SOURCE_DOCUMENT_LOC);
		
		
		if(!new File(ENV.INTR_SLICE_OUTPUT_LOC).exists())
		{
			new File(ENV.INTR_SLICE_OUTPUT_LOC).mkdir();
		}
		if(new File(ENV.INTR_SLICE_OUTPUT_LOC).listFiles().length > 0)
		{
			for(File sliceDir : new File(ENV.INTR_SLICE_OUTPUT_LOC).listFiles())
				sliceDir.delete();
		}
		
		SecureRandom rand = new SecureRandom();
		
		for(File file: files.listFiles())
		{
			long id = rand.nextLong();
			
			if(id < 0)
				id *= -1;
			
			File sliceDir = new File(ENV.INTR_SLICE_OUTPUT_LOC + ENV.DELIM + id);
			
			if(!sliceDir.exists())
				sliceDir.mkdir();
			
			byte[] data = Files.readAllBytes(file.toPath());
			//chunk size should be same as the data size
			SliceData sd = new SliceData(data, chunk_size);
		
			int i = 0;
			
			System.out.println("slices : " + sd.getAllSlices().size());
			
			for(byte[] slice : sd.getAllSlices())
			{
				FileWriter fw_slice = new FileWriter(sliceDir + ENV.DELIM + i + ".slice");
				fw_slice.append(Base64.getEncoder().encodeToString(slice));
				fw_slice.close();

				i++;
			}
			
			System.out.println("Slice added : " + file.getName());
			SLICE_MAP.put(file.getName(), id);
		}
		this.saveSliceTable();
		this.firstSliceId = SLICE_MAP.entrySet().iterator().next().getValue();
	}
	
	/**
	 * Fetch a slice specific to a url and slice index.
	 * Index starts from 0
	 * @param url Slice url
	 * @param index Index of the Specific slice
	 * @return
	 */
	public String getSlice(String url, int index)
	{
		Long sliceId = SLICE_MAP.get(url);
		
		//System.out.println("slice url");
		/*for(String st : SLICE_MAP.keySet())
		{
			System.out.println("SLICE url in tab : " + st);
		}*/
		
		if(sliceId == null)
			return INVALID_SLICE_URL;
		
		System.out.println("Slice with " + url + " found");
		//File sliceDir = new File(ENV.INTR_SLICE_OUTPUT_LOC + ENV.DELIM + sliceId.toString());
		File sliceFile = new File(ENV.INTR_SLICE_OUTPUT_LOC + ENV.DELIM + sliceId.toString() + ENV.DELIM + index + ".slice");
		
		//System.out.println("Slice file loc : " + sliceFile);
		
		if(!sliceFile.exists())
			return INVALID_INDEX_OVERFLOW;
		
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(sliceFile));
			String st = new String();
			StringBuffer stb = new StringBuffer("");

			while((st = br.readLine()) != null)
				stb.append(st);

			br.close();

			return stb.toString();
		}
		catch(IOException ex)
		{
			return INVALID_SLICE_ERROR;
		}
	}
	/**
	 * Fetch a slice specific to a slice id and slice index.
	 * Index starts from 0
	 * @param sliceId Slice id (one to one corresponds with slice url)
	 * @param index Index of the Specific slice
	 * @return
	 */
	public String getSlice(long sliceId, int index)
	{
		if(!new File(ENV.INTR_SLICE_OUTPUT_LOC + ENV.DELIM + sliceId).exists())
			return INVALID_SLICE_URL;
		File sliceFile = new File(ENV.INTR_SLICE_OUTPUT_LOC + ENV.DELIM + sliceId + ENV.DELIM + index + ".slice");
		if(!sliceFile.exists())
			return INVALID_INDEX_OVERFLOW;
		
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(sliceFile));
			String st = new String();
			StringBuffer stb = new StringBuffer("");

			while((st = br.readLine()) != null)
				stb.append(st);

			br.close();

			return stb.toString();
		}
		catch(IOException ex)
		{
			return INVALID_SLICE_ERROR;
		}
	}
	
	public int getSliceCount(String url)
	{
		Long sliceId = SLICE_MAP.get(url);
		if(sliceId == null)
			return -1;
		else
			return new File(ENV.INTR_SLICE_OUTPUT_LOC + ENV.DELIM + sliceId.toString()).listFiles().length;
		
	}
	
	/**
	 * Give a slice from the first available slice. This is to increase the normal droplet response.
	 * @return
	 */
	public String getSlice()
	{
		Long sliceId = SLICE_MAP.entrySet().iterator().next().getValue();
		File sliceFile = new File(ENV.INTR_SLICE_OUTPUT_LOC + ENV.DELIM + sliceId + ENV.DELIM + 0 + ".slice");
		
		if(!sliceFile.exists())
			return INVALID_INDEX_OVERFLOW;
		
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(sliceFile));
			String st = new String();
			StringBuffer stb = new StringBuffer("");

			while((st = br.readLine()) != null)
				stb.append(st);

			br.close();

			return stb.toString();
		}
		catch(IOException ex)
		{
			return INVALID_SLICE_ERROR;
		}
	}
	
	/**
	 * Save the in memory slice table in disk
	 * @throws IOException
	 */
	private void saveSliceTable() throws IOException
	{
		JSONObject jObject = new JSONObject(SLICE_MAP);
		FileWriter fw_slice = new FileWriter(ENV.SLICS_TABLE_LOC);
		fw_slice.append(jObject.toString());
		fw_slice.close();
	}
	
	/**
	 * Load slice table from disk to memory
	 * @return true/false in case it can able to load or not. In case load fails, it will reslice the data and remake the slice table.
	 * @throws IOException
	 */
	private boolean loadSliceTable() throws IOException
	{
		if(!new File(ENV.SLICS_TABLE_LOC).exists())
			return false;
		
		BufferedReader br = new BufferedReader(new FileReader(ENV.SLICS_TABLE_LOC));
		StringBuffer stb = new StringBuffer();
		String str = null;
		
		while((str = br.readLine()) != null)
			stb.append(str);
		
		br.close();
		
		JSONObject jObject = new JSONObject(stb.toString());

		Iterator<String> itKey = jObject.keys();
		
		System.out.println(jObject.keySet().size());
		
		while(itKey.hasNext())
		{
			String sliceKey = itKey.next();
			long sliceId = jObject.getLong(sliceKey);
			
			if(!new File(ENV.INTR_SLICE_OUTPUT_LOC + ENV.DELIM + sliceId).exists())
			{
				return false;
			}
			
			System.out.println("Slice : " + sliceKey + " with id : " + sliceId + " found...");
			SLICE_MAP.put(sliceKey, sliceId);
		}
		
		return true;
	}
	
	/**
	 * Fetch the slice table to be sent in the table please response
	 * @return Slice table in JSOn key value pair
	 * @throws IOException
	 */
	public String getSliceTableAsJson() throws IOException
	{
		/*
		 * if(!new File(ENV.SLICS_TABLE_LOC).exists())
			return null;
		
		BufferedReader br = new BufferedReader(new FileReader(ENV.SLICS_TABLE_LOC));
		StringBuffer stb = new StringBuffer();
		String str = null;
		
		while((str = br.readLine()) != null)
			stb.append(str);
		
		br.close();
		*/
		
		JSONObject jObject = new JSONObject(SliceManager.SLICE_MAP);
		
		return jObject.toString();
	}
	
	/**
	 * Fetch the slice table to be sent in the table please response
	 * in a tree structure. This is to be later parsed in the client
	 * end in the covert browsing window.
	 * @return Slice table in JSOn key value pair
	 * @throws IOException
	 */
	public String getSliceTableAsJsonTree() throws IOException
	{
		Tree tree = new Tree();
		for(String slice : SliceManager.SLICE_MAP.keySet())
			tree.insert("ROOT", slice, SliceManager.SLICE_MAP.get(slice));
		
		return tree.treeToJSON();
			
		/*if(!new File(ENV.SLICS_TABLE_LOC).exists())
			return null;
		
		BufferedReader br = new BufferedReader(new FileReader(ENV.SLICS_TABLE_LOC));
		StringBuffer stb = new StringBuffer();
		String str = null;
		
		while((str = br.readLine()) != null)
			stb.append(str);
		
		br.close();
		
		return stb.toString();*/
	}
}
