package com.local.se360;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;

public final class Packet {
	
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
	
	public Packet(final String rawBuffer) {
		 try {
		     ObjectInputStream stream = new ObjectInputStream(new ByteArrayInputStream(rawBuffer.getBytes()));
		     type                   = (Type) stream.readObject();
		     requireConfidentiality = stream.readBoolean();
		     requireIntegrity       = stream.readBoolean();
		     requireAuthentication  = stream.readBoolean();
		     initVector             = (String) stream.readObject();
		     prime                  = (BigInteger) stream.readObject();
		     nonce                  = (BigInteger) stream.readObject();
		     intermediate           = (BigInteger) stream.readObject();
		     publicKey              = (String) stream.readObject();
		     payload                = (String) stream.readObject();
		     stream.close();
		 } catch (Exception e) {
		     e.printStackTrace();
		 }
	}
	
	public String serializeSansSig() {
		try {
			ObjectOutputStream stream = new ObjectOutputStream(new ByteArrayOutputStream());
			stream.writeObject(type);
			stream.writeBoolean(requireConfidentiality);
			stream.writeBoolean(requireIntegrity);
			stream.writeBoolean(requireAuthentication);
			stream.writeObject(initVector);
			stream.writeObject(prime);
			stream.writeObject(nonce);
			stream.writeObject(intermediate);
			stream.writeObject(publicKey);
			stream.writeObject(payload);
			stream.flush();
			stream.close();
			return stream.toString();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public String serialize() {
		try {
			ObjectOutputStream stream = new ObjectOutputStream(new ByteArrayOutputStream());
			stream.writeObject(type);
			stream.writeBoolean(requireConfidentiality);
			stream.writeBoolean(requireIntegrity);
			stream.writeBoolean(requireAuthentication);
			stream.writeObject(initVector);
			stream.writeObject(prime);
			stream.writeObject(nonce);
			stream.writeObject(intermediate);
			stream.writeObject(signature);
			stream.writeObject(publicKey);
			stream.writeObject(payload);
			stream.flush();
			return stream.toString();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
