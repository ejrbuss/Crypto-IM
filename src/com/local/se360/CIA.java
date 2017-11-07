package com.local.se360;

import java.math.BigInteger;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

//Contains the methods used by both the client and the server for CIA purposes
public class CIA {
	
	public static String encrypt(String Key, String initVector, String value) {
		try {
			IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
			SecretKeySpec skeySpec = new SecretKeySpec(Key.getBytes("UTF-8"), "AES");
			
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
			
			byte[] encrypted = cipher.doFinal(value.getBytes());
			//System.out.println("Encrypted String: " + Base64.getEncoder().encodeToString(encrypted));
			
			return Base64.getEncoder().encodeToString(encrypted);
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return null;
	}
	
	
	public static String decrypt(String Key, String initVector, String encrypted) {
		try {
			IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
			SecretKeySpec skeySpec = new SecretKeySpec(Key.getBytes("UTF-8"), "AES");
			
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
			
			byte[] original = cipher.doFinal(Base64.getDecoder().decode(encrypted));
			
			return new String(original);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return null;
	}
	
	//Confidentiality
		//Uses lots and lots of crap from javax.crypto
			//Key Generator
			//Key Agreement
			//Cipher
			//SecretKey (maybe?)
	
	
	//My code here
	
	
	//Integrity
		//Uses Mac from the javax.crypto package
	
	//Authentication
		//Deals with passwords for server/client
	
	

	public static void main(String[] args) {
		
		//Test for Confidentiality
		new CIA();
		String A = "Eric is a butt";
		String B = CIA.encrypt("Bar12345Bar12345", "RandomInitVector", A);
		String C = CIA.decrypt("Bar12345Bar12345", "RandomInitVector", B);
		
		System.out.println("String A : " + A);
		System.out.println("String B : " + B);
		System.out.println("String C : " + C);
		
		//Test for Integrity
		
		//Test for Authentication

	}


	public static String sign(String privateKey, String message) {
		// TODO Auto-generated method stub
		return null;
	}


	public static BigInteger generatePrime() {
		// TODO Auto-generated method stub
		return null;
	}


	public static BigInteger generateNonce() {
		// TODO Auto-generated method stub
		return null;
	}


	public static BigInteger computeIntermediate() {
		// TODO Auto-generated method stub
		return null;
	}


	public static KeyPair generateKeyPair() {
		// TODO Auto-generated method stub
		return null;
	}

	public static BigInteger computeFinal(BigInteger intermediate, BigInteger intermediate2) {
		// TODO Auto-generated method stub
		return null;
	}


	public static boolean checkSignature(String publicKey, String signature, String payload) {
		// TODO Auto-generated method stub
		return false;
	}


	public static BigInteger compute(BigInteger prime, BigInteger intermediate, BigInteger intermediate2) {
		// TODO Auto-generated method stub
		return null;
	}

	// Confidentiality


}
