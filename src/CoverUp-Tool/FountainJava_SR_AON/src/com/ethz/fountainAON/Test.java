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

package com.ethz.fountainAON;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.json.JSONException;
import org.json.JSONObject;

public class Test {


	public static double total = 0;

	public static void main(String[] args) throws IOException, InterruptedException, JSONException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {

		FileWriter logWriter = new FileWriter("ResultsTest.log", true);
		File file = new File("C:\\Droplets");

		System.out.println("Deleting droplets..");
		if(file.exists())
			for(File f : file.listFiles())
				f.delete();

		//byte[] seed = new byte[10];
		//Arrays.fill(seed, (byte) 0x00);

		/*byte[] b1 = new byte[100];
		byte[] b2 = new byte[100];

		new SecureRandom(seed).nextBytes(b1);
		new SecureRandom(seed).nextBytes(b2);


		System.err.println(Arrays.equals(b1, b2));*/

		//System.exit(1);
		//System.out.println("deleted");


		/*	int[][] testvector = {
				{10, 10000},
				{100, 10000},
				{500, 10000},
				{1000, 10000},
				{5000, 10000},

				{10, 100000},
				{100, 100000},
				{1000, 100000},
				{5000, 100000},
				{10000, 100000},

				{100, 1000000},
				{1000, 1000000},
				{5000, 1000000},
				{10000, 1000000},
				{50000, 1000000},
				{100000, 1000000},


		};*/

		int[][] testvector = { 
				{1280, 100000}
		};

		for(int i1 = 0; i1 < 10; i1 ++)
		{
			for(int i = 0; i < testvector.length; i++)
			{
				test(testvector[i][0], testvector[i][1], true, logWriter);

				System.out.println("Deleting droplets..");
				if(file.exists())
					for(File f : file.listFiles())
						f.delete();
				System.out.println("deleted");

			}	
			//test(1000, 1000000, true, logWriter);
		}

		//System.out.println(total/1000);
		logWriter.close();

	}

	public static void test(int chunk_size, int data_size, boolean seq, FileWriter logWriter) throws IOException, InterruptedException, JSONException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {


		byte[] data = new byte[data_size];
		Random rand = new Random();

		//File f =new File("C:\\4k wallpapers\\Space\\1.txt");
		//byte[] data = Files.readAllBytes(f.toPath());

		rand.nextBytes(data);

		byte[] seed = new byte[32]; 
		rand.nextBytes(seed);

		Fountain fountain = new Fountain(data, chunk_size, seed);

		int fa = (data.length / chunk_size);

		//System.out.println("Creating droplets...");	

		for(int i = 0; i < fa * 5; i++)
		{
			if(i % 1000 == 0)
				System.out.println(i);

			FileWriter fw = new FileWriter("C:\\Droplets\\" + i + ".json");
			Droplet d = fountain.droplet();
			fw.write(d.toString());
			fw.close();
		}


		//System.out.println("Droplets created...");

		Glass glass = new Glass(fountain.num_chunks);

		byte[] decodedData = new byte[data.length];

		int req_s = 0;


		Droplet d;
		while(true)
		{
			BufferedReader br = null;

			try
			{
				if(!seq)
					br = new BufferedReader(new FileReader("C:\\Droplets\\" + rand.nextInt(fa * 5) + ".json"));
				else
					br = new BufferedReader(new FileReader("C:\\Droplets\\" + req_s + ".json"));

			}
			catch(FileNotFoundException ex)
			{
				System.err.println("Not enough to decode");
				break;
			}

			StringBuffer stb = new StringBuffer();
			String st = "";

			while((st = br.readLine()) != null)
				stb.append(st);

			br.close();

			req_s++;
			JSONObject jObject = new JSONObject(stb.toString());

			d = new Droplet(Base64.getUrlDecoder().decode(jObject.get("data").toString()), Base64.getUrlDecoder().decode(jObject.get("seed").toString()), jObject.getInt("num_chunks"));
			glass.addDroplet(d);

			if(glass.isDone())
			{
				//for(int i = 0; i < Glass.chunks.length; i++)
				//	System.arraycopy(Glass.chunks[i], 0, decodedData, i * chunk_size, chunk_size);

				decodedData = glass.getDecodedData();

				decodedData = Arrays.copyOfRange(decodedData, 0, fountain.dataLenBeforPadding);

				if(Arrays.equals(data, decodedData))
					System.err.println("Decoding sucess");
				else
					System.err.println("Decoding ERROR");

				break;
			}
		}

		logWriter.append("\nchunk size : " + chunk_size  + ", data_size : " + data_size + ", chunks : " + fa + "\n-------------------------------------------\n");
		logWriter.append("Ratio : " + (double)req_s/fa + "\n\n");

		//System.out.print("chunk size : " + chunk_size  + ", data_size : " + data_size + ", chunks : " + fa + "\n-------------------------------------------\n");
		System.out.println("Ratio : " + (double)req_s/fa);

		total += (double)req_s/fa;

		logWriter.flush();

		/*System.out.print("Counter : " + req_s);
		System.out.println((double)req_s/fa);	
		 */
	}

}
