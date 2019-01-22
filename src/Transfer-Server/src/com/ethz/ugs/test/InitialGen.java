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

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Arrays;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.whispersystems.curve25519.Curve25519;
import org.whispersystems.curve25519.Curve25519KeyPair;

import com.ethz.fountain.Droplet;
import com.ethz.fountain.Fountain;
import com.ethz.ugs.dataStructures.FountainTableRow;
import com.ethz.ugs.dataStructures.SiteMap;
import com.ethz.ugs.dataStructures.SliceManager;
import com.ethz.ugs.server.ENV;

/**
 * Droplet and slice data initialization. For every server deployment this will execute.
 * @author Aritra
 *
 */
public class InitialGen 
{
	
	public static SliceManager sdm = null;
	
	
	public static void init() throws IOException, NoSuchAlgorithmException, NoSuchProviderException
	{
		
		sdm = new SliceManager(ENV.FOUNTAIN_CHUNK_SIZE);
		
		
		SiteMap.loadTable();

		File[] files = new File(ENV.SOURCE_DOCUMENT_LOCATION).listFiles();

		for(File file : files)
		{
			if(SiteMap.TABLE_MAP.containsKey(file.getAbsolutePath()))
			{
				System.out.println(file + "  skipped ..");
				continue;
			}
			
			FountainTableRow row = new FountainTableRow(file.getAbsolutePath(), ENV.FOUNTAIN_CHUNK_SIZE, 50);
			row.makeDroplets();
			SiteMap.insertRowToTable(file.getAbsolutePath(), row);
		}

		SiteMap.saveTable();

		//SiteMap.loadTable();

		System.out.println("---------------------done----------------------");
	}

	
	@SuppressWarnings("unused")
	public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchProviderException, SecurityException, IOException {
		
		FileHandler fh = new FileHandler("data.log", true);
		
		Logger logger = Logger.getLogger(InitialGen.class.getName());

		byte[] b1 = ByteBuffer.allocate(8).putLong(5409821196803746949L).array();
		
		Curve25519KeyPair keypair = Curve25519.getInstance(Curve25519.BEST).generateKeyPair();
		byte[] sk = keypair.getPrivateKey();
		byte[] pk = keypair.getPublicKey();
		
		logger.addHandler(fh);
        SimpleFormatter formatter = new SimpleFormatter();  
        fh.setFormatter(formatter);  
        
        logger.info("ab");
		logger.log(Level.FINE, "msg", pk.length);
		
		System.out.println(sk.length);
		System.out.println(pk.length);
		
		byte[] data = new byte[256];
		Arrays.fill(data, (byte)0x01);
		
		byte[] sig = Curve25519.getInstance(Curve25519.BEST).calculateSignature(sk, data);
		System.out.println(sig.length);
		
		byte[] seed = new byte[10];
		Arrays.fill(seed, (byte) 0x02);
		
		Fountain f1 = new Fountain(data, 10, seed);
		Droplet d1 = f1.droplet();
		
		Fountain f2 = new Fountain(data, 10, seed);
		Droplet d2 = f2.droplet();
	}
	
	
/*	
 * public static void main(String[] args) throws IOException, NoSuchAlgorithmException, NoSuchProviderException {

		SiteMap.loadTable();

		File[] files = new File(ENV.SOURCE_DOCUMENT_LOCATION).listFiles();

		for(File file : files)
		{
			if(SiteMap.SITE_MAP.containsKey(file.getAbsolutePath()))
			{
				System.out.println(file + "  skipped ..");
				continue;
			}
			
			FountainTableRow row = new FountainTableRow(file.getAbsolutePath(), ENV.FOUNTAIN_CHUNK_SIZE, 50);
			row.makeDroplets();
			SiteMap.insertRowToTable(file.getAbsolutePath(), row);
		}

		SiteMap.saveTable();

		//SiteMap.loadTable();

		System.out.println("---------------------done----------------------");
	}	
	*/

}
