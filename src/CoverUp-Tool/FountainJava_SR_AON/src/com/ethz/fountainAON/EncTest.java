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

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncTest {
	
	public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException 
	{
		SecureRandom rand_cipher = new SecureRandom();
		
		byte[] ivBytes = new byte[16];
		rand_cipher.nextBytes(ivBytes);
		
		byte[] keyBytes = new byte[16];
		rand_cipher.nextBytes(keyBytes);
		
		byte[] message = new byte[128];
		rand_cipher.nextBytes(message);
		
		IvParameterSpec iv = new IvParameterSpec(ivBytes);
		SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
		
		Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
		cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv);
		
		byte[] cipherText = cipher.doFinal(message);
		
		System.out.println(cipherText.length);
	}

}
