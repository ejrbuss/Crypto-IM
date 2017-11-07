package com.local.se360;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Signature;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

//Contains the methods used by both the client and the server for CIA purposes
public class CIA {
	
	// Note: initVector must be 16 bytes long
	
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
	
	// -- Diffie-Hellman key exchange! -- //
	
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
	public static java.security.KeyPair generateKeyPair() {
		try {
			KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
			kpg.initialize(2048);
			java.security.KeyPair kp = kpg.genKeyPair();
			return kp;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		return null;		
	}
	
	//Sign (returns the encrypted hash of a message)
	public static String sign(KeyPair keys, String message) {		
		try {
			Signature sig = Signature.getInstance("MD5withRSA");
			sig.initSign(keys.getPrivate());
			sig.update(Base64.getEncoder().encode(message.getBytes()));
			byte[] realSig = sig.sign();
			String signature = new String(Base64.getEncoder().encode(realSig));
			return signature;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	//CheckSignature (takes an encrypted message, and a public key, and the original message, returns a boolean)
	public static boolean checkSignature(KeyPair keys, String signature, String message) {		
		try {
			Signature sig = Signature.getInstance("MD5withRSA");
			sig.initVerify(keys.getPublic());
			sig.update(Base64.getEncoder().encode(message.getBytes()));
			boolean result = sig.verify(Base64.getDecoder().decode(signature.getBytes()));
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println();
		}
		
		return false;
	}

	public static void main(String[] args) throws NoSuchAlgorithmException {	
		//Test for Encrypt and Decrypt
		new CIA();
		
		java.security.KeyPair keys = CIA.generateKeyPair();
		String plaintext = new String("hello Eric");
		String signature = CIA.sign(keys, plaintext);
		System.out.println("plaintext: " + plaintext);
		System.out.println("signature: " + signature);
		System.out.println("signature matched? " + CIA.checkSignature(keys, signature, plaintext));
	}

}
