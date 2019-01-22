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

package com.ethz.ugs.test;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZInputStream;
import org.tukaani.xz.XZOutputStream;

public class LZMATest {


	public static void compress() throws Exception
	{
		FileInputStream inFile = new FileInputStream("C:\\Users\\Aritra\\Desktop\\1510679703\\0.json");
		FileOutputStream outfile = new FileOutputStream("C:\\Users\\Aritra\\Desktop\\1510679703\\0.xz");

		LZMA2Options options = new LZMA2Options();

		options.setPreset(1); // play with this number: 6 is default but 7 works better for mid sized archives ( > 8mb)

		XZOutputStream out = new XZOutputStream(outfile, options);

		byte[] buf = new byte[8192];
		int size;
		while ((size = inFile.read(buf)) != -1)
		   out.write(buf, 0, size);

		out.finish();
		
		
		inFile.close();
		out.close();
	}
	
	
	public static void decompress() throws Exception
	{
		FileInputStream inFile = new FileInputStream("C:\\Users\\Aritra\\Desktop\\1510679703\\0.xz");
		FileOutputStream outfile = new FileOutputStream("C:\\Users\\Aritra\\Desktop\\1510679703\\0_dec.json");

	
		XZInputStream in = new XZInputStream(inFile);
		
		
		byte[] buf = new byte[8192];
		int size;
		while ((size = in.read(buf)) != -1)
			outfile.write(buf, 0, size);

		in.close();
		inFile.close();
		outfile.close();
	}
	
	public static void main(String[] args) throws Exception 
	{		
		compress();
		decompress();
		
		System.out.println("Done");
	}
}