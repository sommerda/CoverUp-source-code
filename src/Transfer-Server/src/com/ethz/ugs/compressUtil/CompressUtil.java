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

package com.ethz.ugs.compressUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZInputStream;
import org.tukaani.xz.XZOutputStream;

public class CompressUtil {
	
	/**
	 * 
	 * @param infileString
	 * @param outfileString
	 * @param preSet play with this number: 6 is default but 7 works better for mid sized archives ( > 8mb)
	 * @throws IOException
	 */
	public static void compress(String infileString, String outfileString, int preSet) throws IOException
	{
		FileInputStream inFile = new FileInputStream(infileString);
		FileOutputStream outfile = new FileOutputStream(outfileString);

		LZMA2Options options = new LZMA2Options();

		options.setPreset(preSet);

		XZOutputStream out = new XZOutputStream(outfile, options);

		byte[] buf = new byte[8192];
		int size;
		while ((size = inFile.read(buf)) != -1)
		   out.write(buf, 0, size);

		out.finish();
		
		
		inFile.close();
		out.close();
	}
	
	
	public static byte[] compress(byte[] inbyte, int preSet) throws IOException
	{
		InputStream inStream = new ByteArrayInputStream(inbyte);
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		
		LZMA2Options options = new LZMA2Options();

		options.setPreset(preSet);
		
		XZOutputStream out = new XZOutputStream(outStream, options);
		
		byte[] buf = new byte[8192];
		int size;
		while ((size = inStream.read(buf)) != -1)
		   out.write(buf, 0, size);

		out.finish();
			
		inStream.close();
		out.close();
		
		return outStream.toByteArray();
	}

	public static void deCompress(String infileString, String outfileString) throws IOException
	{
		FileInputStream inFile = new FileInputStream(infileString);
		FileOutputStream outfile = new FileOutputStream(outfileString);

		XZInputStream in = new XZInputStream(inFile);
				
		byte[] buf = new byte[8192];
		int size;
		while ((size = in.read(buf)) != -1)
			outfile.write(buf, 0, size);

		in.close();
		inFile.close();
		outfile.close();
	}
	
	public static byte[] deCompress(byte[] inByte) throws IOException
	{
		InputStream inStrem = new ByteArrayInputStream(inByte);
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		
		XZInputStream in = new XZInputStream(inStrem);
		
		byte[] buf = new byte[8192];
		int size;
		while ((size = in.read(buf)) != -1)
			outStream.write(buf, 0, size);

		in.close();
		inStrem.close();
		outStream.close();
		
		return outStream.toByteArray();
	}
	
	//Test
	public static void main(String[] args) throws IOException 
	{
		byte[] bytes = new byte[1000000];
		Arrays.fill(bytes, (byte) 0x01);
		//new Random().nextBytes(bytes);
		
		System.out.println("Data Size = " + bytes.length);
		
		byte[] compressedBytes = compress(bytes, 7);
		
		System.out.println("Compressed data size = " + compressedBytes.length);
		
		byte[] decompressedBytes = deCompress(compressedBytes);
		
		System.out.println(Arrays.equals(bytes, decompressedBytes));
		
	}
}
