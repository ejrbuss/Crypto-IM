package com.local.se360;

import java.math.BigInteger;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
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

	public static void main(String[] args) throws NoSuchAlgorithmException {	
		//Test for Encrypt and Decrypt
		new CIA();
		String A = "I like Butts";
		String B = CIA.encrypt("Bar12345Bar12345", "RandomInitVector", A);
		String C = CIA.decrypt("Bar12345Bar12345", "RandomInitVector", B);
		
		System.out.println("String A : " + A);
		System.out.println("String B : " + B);
		System.out.println("String C : " + C);
		
		
		BigInteger p = generatePrime();
		System.out.println("Prime Nonce : " + p);
		
		BigInteger g = generateNonce();
		System.out.println("Non-Prime Nonce : " + g);
		
		BigInteger s1 = generateNonce();
		System.out.println("Alice secret : " + s1);
		
		BigInteger s2 = generateNonce();
		System.out.println("Bob secret : " + s2);
		
		BigInteger intermediateAlice = compute(p, g, s1);
		System.out.println("IntermediateAlice : " + intermediateAlice);
		
		BigInteger intermediateBob = compute(p, g, s2);
		System.out.println("IntermediateBob : " + intermediateBob);
		
		BigInteger finalAlice = compute(p, intermediateBob, s1);
		System.out.println("Final Alice : " + finalAlice);
		
		BigInteger finalBob = compute(p, intermediateAlice, s2);
		System.out.println("Final Bob : " + finalBob);
	}

}
