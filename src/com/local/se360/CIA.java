package com.local.se360;

import java.math.BigInteger;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

//Contains the methods used by both the client and the server for CIA purposes
public class CIA {
	
	public static String encrypt(String key, String initVector, String value) {
		
		try {
			IvParameterSpec iv     = new IvParameterSpec(
				Arrays.copyOfRange(initVector.getBytes("UTF-8"), 0, 16));
			SecretKeySpec skeySpec = new SecretKeySpec(
				Arrays.copyOfRange(key.getBytes("UTF-8"), 0, 32), "AES");
			
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
	
	public static String decrypt(String key, String initVector, String encrypted) {
		try {
			IvParameterSpec iv     = new IvParameterSpec(
				Arrays.copyOfRange(initVector.getBytes("UTF-8"), 0, 16));
			SecretKeySpec skeySpec = new SecretKeySpec(
				Arrays.copyOfRange(key.getBytes("UTF-8"), 0, 32), "AES");
			
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
	public static BigInteger generatePrime() {
		
		SecureRandom rand = new SecureRandom();
		byte bytes[] = new byte[20];
		rand.nextBytes(bytes);
		
		BigInteger primeNonce = new BigInteger(1024, 100, rand);
		
		return primeNonce;
	}
	
	
	public static BigInteger generateNonce() {
		
		SecureRandom rand = new SecureRandom();
		byte bytes[] = new byte[20];
		rand.nextBytes(bytes);
		
		BigInteger nonce = new BigInteger(1024, rand);
		
		return nonce;
	}
	
	
	public static BigInteger compute(BigInteger p, BigInteger g, BigInteger s) {
		return g.modPow(s, p);
	}
	
	//GenerateKeyPair
	public static KeyPair generateKeyPair() {
		String publicKey = new String();
		String privateKey = new String();
		
		// TODO
		// Generate public and private keys
		
		KeyPairGenerator kpg;
		try {
			kpg = KeyPairGenerator.getInstance("RSA");
			kpg.initialize(2048);
			java.security.KeyPair kp = kpg.genKeyPair();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
				
		//publicKey = kp.getPublic().getEncoded();
		//privateKey = kp.getPrivate().toString();
		
		KeyPair keys = new KeyPair(publicKey, privateKey);
		//System.out.println("public key: " + publicKey + "\n" + "private key: " + privateKey);
		return keys;
	}
	
	//Sign (returns the encrypted hash of a message)
	public static String sign(String privateKey, String initVector, String message) {
		// TODO
		//hash(message);
		String signature = CIA.encrypt(privateKey, initVector, message);
		return signature;
	}
	
	//CheckSignature (takes an encrypted message, and a public key, and the original message, returns a boolean)
	public static boolean checkSignature(String publicKey, String initVector, String signature, String message) {
		String myHash = new String();
		String theirHash = new String();
		
		// TODO
		//myHash = hash(message);
		
		theirHash = CIA.decrypt(publicKey, initVector, signature);
		
		return (myHash.equals(theirHash)) ? true : false;
	}

}
