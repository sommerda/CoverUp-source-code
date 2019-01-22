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
package com.ethz.app.chatApp;

import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

import com.ethz.app.env.ENV;

/**
 * @author Aritra
 *
 */
public class ChatUtils {

	public static String publicKeyToAddress(String publicKeyStr) throws NoSuchAlgorithmException
	{
		byte[] pk = Base64.getUrlDecoder().decode(publicKeyStr);
		MessageDigest md = MessageDigest.getInstance(ENV.CRYPTO_HASH_ALGORITHM);
		byte[] hashedPk = md.digest(pk);
		byte[] publicAddressBytes = Arrays.copyOf(hashedPk, ENV.CHAT_PUBLIC_ADDRESS_LEN);
		
		return Base64.getUrlEncoder().encodeToString(publicAddressBytes);
	}
	
	public static void addPublicKeyToLocalStorage(String publicKeyStr) throws IOException
	{
		FileWriter fw = new FileWriter(ENV.APP_STORAGE_PUBLIC_KEY_LIST, true);
		fw.append("\n" + publicKeyStr);
		fw.close();
	}
}
