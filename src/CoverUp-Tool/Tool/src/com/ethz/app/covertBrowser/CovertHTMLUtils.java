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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONObject;

/**
 * @author Aritra
 *
 */
public class CovertHTMLUtils {

	
	public static Map<String, Long> sliceMap = new HashMap<>();
	public static Map<Long, String> sliceMapRev = new HashMap<>();
	
	
	public static void covertHTMLStartPageGenerator(String loc, String sliceFileLoc, int port) throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader(sliceFileLoc));
		StringBuffer stb = new StringBuffer();
		String st = null;
		
		while((st = br.readLine())!=null)
			stb.append(st);
		br.close();
		
		FileWriter fw = new FileWriter(loc);
		
		fw.append("<html>");
		fw.append("<body>");
		
		fw.append("<h1>Slice Index</h1>");
		
		fw.append("<br>");
		JSONObject jObject = new JSONObject(stb.toString());
		Iterator<String> keys = jObject.keys();
		while(keys.hasNext())
		{
			String key = keys.next();
			long value = jObject.getLong(key);
			sliceMap.put(key, value);
			sliceMapRev.put(value, key);
			fw.append(key + "			" + "<a href=\"http://127.0.0.1:" + port + "/id=" + value + "\">" + value + "</a>");
			fw.append("<br>");
			//System.out.println(key);
			//System.out.println(value);
		}
			
		fw.append("</body>");
		fw.append("</html>");
		
		fw.flush();
		fw.close();
		
	}
	
}
