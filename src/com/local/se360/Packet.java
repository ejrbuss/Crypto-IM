package com.local.se360;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.util.Base64;

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
	
	public Packet(final String raw) {
		Config.log("Deserializing packet...");
		try {
			ByteArrayInputStream target = new ByteArrayInputStream(Base64.getDecoder().decode(raw.getBytes()));
			ObjectInputStream stream    = new ObjectInputStream(target);
			type                   	 = (Type) stream.readObject();
			requireConfidentiality 	 = stream.readBoolean();
			requireIntegrity       	 = stream.readBoolean();
			requireAuthentication  	 = stream.readBoolean();
			initVector             	 = (String) stream.readObject();
			prime                  	 = (BigInteger) stream.readObject();
			nonce                  	 = (BigInteger) stream.readObject();
			intermediate           	 = (BigInteger) stream.readObject();
			signature					 = (String) stream.readObject();
			publicKey              	 = (String) stream.readObject();
			payload                	 = (String) stream.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Config.log(toString());
}
	
	public String serialize() {
		Config.log("Serializing packet...");
		Config.log(toString());
		try {
			ByteArrayOutputStream target = new ByteArrayOutputStream();
	        ObjectOutputStream stream    = new ObjectOutputStream(target);
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
	        return new String(Base64.getEncoder().encode(target.toByteArray()));        
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public String toString() {
		return "Packet contents:"
			+ "\n\ttype: "    + type.name()
			+ "\n\tC: "       + requireConfidentiality
			+ "\n\tI: "       + requireIntegrity
			+ "\n\tA: "       + requireAuthentication
			+ "\n\tIV: "      + initVector
			+ "\n\tprime: "   + prime
			+ "\n\tnonce: "   + nonce
			+ "\n\tinter: "   + intermediate
			+ "\n\tsig: "     + signature
			+ "\n\tPK: "      + publicKey
			+ "\n\tpayload: " + payload;  
	}
	
	public String serializeSansSig() {
		final String hold = signature;
		signature = "";
		final String serial = serialize();
		signature = hold;
		return serial;
	}
	
	// Testing Serialization
	public static void main(String[] args) {
		Packet send = new Packet();
		send.type = Type.PING;
		send.payload = "Hello World";
		Packet recieve = new Packet(send.serialize());
		System.out.println(recieve.payload);
		System.out.println(recieve.type.name());
	}
}
