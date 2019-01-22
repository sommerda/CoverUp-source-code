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
package com.ethz.app.covertBrowser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Base64;

import com.ethz.app.env.ENV;

/**
 * @author Aritra
 *
 */
public class CovertBrowserUtility {

	public static String[] getLocalSliceIds()
	{
		File sliceFileLoc = new File(ENV.APP_STORAGE_LOC + ENV.DELIM + ENV.APP_STORAGE_INTERACTIVE_DATA);
		File[] sliceFiles = sliceFileLoc.listFiles();
		String[] ret = new String[sliceFiles.length];
		int i = 0;
		for(File sliceDoc : sliceFiles)
			ret[i++] = sliceDoc.getName();
		return ret;
		
	}
	
	public static byte[] assembleSlices(long sliceId) throws IOException
	{
		String sliceDirLocation = ENV.APP_STORAGE_LOC + ENV.DELIM + ENV.APP_STORAGE_INTERACTIVE_DATA + ENV.DELIM + sliceId;
		
		if(!new File(sliceDirLocation).exists())
			return null;
		
		File[] files = new File(sliceDirLocation).listFiles();
		
		byte[] ret = null;
		int i = 0;
		for(File file: files)
		{
			String fileName = file.getName().split("\\.")[0];
			BufferedReader br = new BufferedReader(new FileReader(file));
			StringBuffer stb = new StringBuffer();
			String str = null;
			while((str = br.readLine())!=null)
				stb.append(str);
			br.close();
			
			String sliceDataInString = stb.toString();
			byte[] decodedSliceData = Base64.getDecoder().decode(sliceDataInString);
			//Initialize the big byte array at the first look 
			if(i == 0)
				ret = new byte[decodedSliceData.length * files.length];
			int startIndex = Integer.parseInt(fileName);
			System.arraycopy(decodedSliceData, 0, ret, startIndex * decodedSliceData.length, decodedSliceData.length);
			i++;
		}
		
		return ret;
	}
	
	public static boolean checkSliceFolder(long sliceId)
	{
		String sliceDirLocation = ENV.APP_STORAGE_LOC + ENV.DELIM + ENV.APP_STORAGE_INTERACTIVE_DATA + ENV.DELIM + sliceId;
		
		return new File(sliceDirLocation).exists();
	}
	public static String getSliceTable() throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader(ENV.APP_STORAGE_LOC + ENV.DELIM + ENV.APP_STORAGE_SLICE_TABLE_LOC + ENV.DELIM + ENV.APP_STORAGE_SLICE_TABLE));
		StringBuffer stb = new StringBuffer();
		String str = null;
		
		while((str = br.readLine()) != null)
			stb.append(str);
		br.close();
		
		return stb.toString();
	}
	
}
