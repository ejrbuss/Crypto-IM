package com.local.se360;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Base64;

public final class Packet implements Serializable {
	
	private static final long serialVersionUID = -6191564202273410701L;

	public enum Type {
		PING,
		PONG,
		DENY,
		MESSAGE
	}
	
	// Packet type
	public Type type;
	
	// Configuration
	public boolean requireConfidentiality;
	public boolean requireIntegrity;
	public boolean requireAuthentication;
	
	public String initVector;
	
	// Confidentiality
	public BigInteger prime;
	public BigInteger nonce;
	public BigInteger intermediate;
	
	// Integrity
	public String signature;
	public String publicKey;

	public String payload;
	
	public Packet() {}
	
	public static Packet parse(final String raw) {
		try {
			
			ByteArrayInputStream target = new ByteArrayInputStream(Base64.getDecoder().decode(raw.getBytes()));
			ObjectInputStream    stream = new ObjectInputStream(target);
			return (Packet) stream.readObject();
		
		} catch (ClassNotFoundException e) {
			// Failure to cast to Packet!!!
			e.printStackTrace();
			System.exit(-1);
		} catch (IOException e) {
			// Stream error!!!
			e.printStackTrace();
			System.exit(-1);
		}
		return null;
	}
	
	public String serialize() {
		try {
			ByteArrayOutputStream target = new ByteArrayOutputStream();
	        ObjectOutputStream    stream = new ObjectOutputStream(target);
	        stream.writeObject(this);
	        stream.flush();
	        return new String(Base64.getEncoder().encode(target.toByteArray()));        
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public String serializeSansSig() {
		final String hold = signature;
		signature = "";
		final String serial = serialize();
		signature = hold;
		return serial;
	}
}
