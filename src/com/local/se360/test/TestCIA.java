package com.local.se360.test;

import java.math.BigInteger;
import java.security.KeyPair;

import com.local.se360.CIA;

public final class TestCIA {

	public static void main(String[] args) {
		TestCIA.start();
	}
	
	public static void start() {
		
		//Test encrypt and decrypt
		String A = "Hello, World!";
		String B = CIA.encrypt("Bar12345Bar12345", "RandomInitVector", A);
		String C = CIA.decrypt("Bar12345Bar12345", "RandomInitVector", B);
		
		System.out.println("String A : " + A);
		System.out.println("String B : " + B);
		System.out.println("String C : " + C);
		
		// Test key exchange
		BigInteger p = CIA.generatePrime();
		System.out.println("Prime Nonce : " + p);
		
		BigInteger g = CIA.generateNonce();
		System.out.println("Non-Prime Nonce : " + g);
		
		BigInteger s1 = CIA.generateNonce();
		System.out.println("Alice secret : " + s1);
		
		BigInteger s2 = CIA.generateNonce();
		System.out.println("Bob secret : " + s2);
		
		BigInteger intermediateAlice = CIA.compute(p, g, s1);
		System.out.println("IntermediateAlice : " + intermediateAlice);
		
		BigInteger intermediateBob = CIA.compute(p, g, s2);
		System.out.println("IntermediateBob : " + intermediateBob);
		
		BigInteger finalAlice = CIA.compute(p, intermediateBob, s1);
		System.out.println("Final Alice : " + finalAlice);
		
		BigInteger finalBob = CIA.compute(p, intermediateAlice, s2);
		System.out.println("Final Bob : " + finalBob);
		
		//Test Signing
		KeyPair keys = CIA.generateKeyPair();
		String plaintext = new String("hello Eric");
		String signature = CIA.sign(keys.getPrivate(), plaintext);
		System.out.println("plaintext: " + plaintext);
		System.out.println("signature: " + signature);
		System.out.println("signature matched? " + CIA.checkSignature(keys.getPublic(), signature, plaintext));
	}
	
}
