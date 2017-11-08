package com.local.se360;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public final class CIA {
	
	private CIA() {} // Static class, should not be instantiated
	
	private static final SecureRandom rng = new SecureRandom();
	
	/**
	 * Encrypt text using AES encryption and CBC mode.
	 * 
	 * @param key        Key to use for encryption
	 * @param initVector Initialization vector for CBC
	 * @param plainText  Text to encrypt
	 * @return			 The cipherText
	 */
	public static String encrypt(
		final String key, 
		final String initVector, 
		final String plainText
	) {
		try {
			
			final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(
				Cipher.ENCRYPT_MODE, 
				new SecretKeySpec(Arrays.copyOfRange(key.getBytes("UTF-8"), 0, 32), "AES"),
				new IvParameterSpec(Arrays.copyOfRange(initVector.getBytes("UTF-8"), 0, 16))
			);
			return Base64
				.getEncoder()
				.encodeToString(cipher.doFinal(plainText.getBytes()));
			
		} catch (Exception e) {
			Config.panic("Encryption failed", e);
		}
		throw new RuntimeException("Unreachable");
	}
	
	/**
	 * Decrypt text using AES encryption and CBC mode.
	 * 
	 * @param key        Key to use for decryption 
	 * @param initVector Initialization vector for CBC
	 * @param cipherText Text to decrypt
	 * @return           The plaintText
	 */
	public static String decrypt(
		final String key, 
		final String initVector, 
		final String cipherText
	) {
		try {
			
			final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(
				Cipher.DECRYPT_MODE, 
				new SecretKeySpec(Arrays.copyOfRange(key.getBytes("UTF-8"), 0, 32), "AES"),
				new IvParameterSpec(Arrays.copyOfRange(initVector.getBytes("UTF-8"), 0, 16))
			);
			return new String(cipher.doFinal(Base64.getDecoder().decode(cipherText)));
			
		} catch (Exception e) {
			Config.panic("Decryption failed", e);
		}
		throw new RuntimeException("Unreachable");
	}
	
	/**
	 * @return A large securely random prime number.
	 */
	public static BigInteger generatePrime() {
		return new BigInteger(1024, 100, rng);
	}
	
	/**
	 * @return A large securely random number.
	 */
	public static BigInteger generateNonce() {
		return new BigInteger(1024, rng);
	}
	
	/**
	 * Computation used for diffie hellman key exchange.
	 * 
	 * @param prime Prime to modulo
	 * @param a     First nonce      
	 * @param b     Second nonce
	 * @return      a^b mod p
	 */
	public static BigInteger compute( 
		final BigInteger prime, 
		final BigInteger a, 
		final BigInteger b
	) {
		return a.modPow(b, prime);
	}
	
	/**
	 * @return A public/private key pair
	 */
	public static KeyPair generateKeyPair() {
		try {
			
			final KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
			gen.initialize(2048);
			return gen.genKeyPair();
		
		} catch (NoSuchAlgorithmException e) {
			Config.panic("RSA is not an algorithm", e);
		}
		throw new RuntimeException("Unreachable");
	}
	
	/**
	 * Sign a text with a provided private key.
	 * 
	 * @param key  Private key to sign with
	 * @param text The text to sign
	 * @return     The signature
	 */
	public static String sign(final PrivateKey key, final String text) {		
		try {
			
			final Signature signature = Signature.getInstance("MD5withRSA");
			signature.initSign(key);
			signature.update(Base64.getEncoder().encode(text.getBytes()));
			return new String(Base64.getEncoder().encode(signature.sign()));
			
		} catch (Exception e) {
			Config.panic("Failed to sign", e);
		}
		throw new RuntimeException("Unreachable");
	}
	
	/**
	 * Check to see if a provided signature matches a text and public key.
	 * 
	 * @param key       The corresponding public key
	 * @param signature The provided signature
	 * @param text      The text
	 * @return			True if signature is correct
	 */
	public static boolean checkSignature(
		final PublicKey key, 
		final String signature, 
		final String text
	) {		
		try {
			
			final Signature verifier = Signature.getInstance("MD5withRSA");
			verifier.initVerify(key);
			verifier.update(Base64.getEncoder().encode(text.getBytes()));
			return verifier.verify(Base64.getDecoder().decode(signature.getBytes()));
			
		} catch (Exception e) {
			Config.panic("Failed to check Signature", e);
		}
		throw new RuntimeException("Unreachable");
	}

}
