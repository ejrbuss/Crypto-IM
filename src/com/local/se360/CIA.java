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
	
	//Diffie-Hellman key exchange!
	
		//GeneratePrimeNonce
	//public static BigInteger GeneratePrimeNonce() {
		
		//return TestValue
	//}
	
		//GenerateNonce
	
		//ComputeIntermediate
	
		//ComputeFinal
	
	//GenerateKeyPair
	
	//Sign (returns the encrypted hash of a message)
	
	//CheckSignature (takes an encrypted message, and a public key, and the original message, returns a boolean)

	public static void main(String[] args) {
		
		//Test for Encrypt and Decrypt
		new CIA();
		String A = "I like Butts";
		String B = CIA.encrypt("Bar12345Bar12345", "RandomInitVector", A);
		String C = CIA.decrypt("Bar12345Bar12345", "RandomInitVector", B);
		
		System.out.println("String A : " + A);
		System.out.println("String B : " + B);
		System.out.println("String C : " + C);

	}

}
