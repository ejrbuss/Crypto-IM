package com.local.se360;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.PublicKey;
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
	public PublicKey publicKey;

	public String payload;
	
	public Packet() {}
	
	public static Packet deserialize(final String raw) {
		try {
			
			final ByteArrayInputStream target = new ByteArrayInputStream(
				Base64.getDecoder().decode(raw.getBytes()));
			final ObjectInputStream    stream = new ObjectInputStream(target);
			return (Packet) stream.readObject();
		
		} catch (Exception e) {
			Config.panic("Failed to deserialize", e);
		}
		throw new RuntimeException("Unreachable");
	}
	
	public String serialize() {
		try {
			
			final ByteArrayOutputStream target = new ByteArrayOutputStream();
	        final ObjectOutputStream    stream = new ObjectOutputStream(target);
	        stream.writeObject(this);
	        stream.flush();
	        return new String(Base64.getEncoder().encode(target.toByteArray())); 
	        
		} catch(Exception e) {
			Config.panic("Failed to serialize", e);
		}
		throw new RuntimeException("Unreachable");
	}
	
	public String serializeSansSig() {
		final String hold = signature;
		signature = null;
		final String serial = serialize();
		signature = hold;
		return serial;
	}
}
