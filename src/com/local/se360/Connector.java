package com.local.se360;

import java.math.BigInteger;
import java.util.function.Consumer;

public abstract class Connector {
	
	// Socket
	protected SocketController socket;
	
	// Configuration
	protected boolean requireConfidentiality;
	protected boolean requireIntegrity;
	protected boolean requireAuthentication;
	
	// Status
	protected boolean connected     = false;
	protected boolean authenticated = false;
	
	// Confidentiality
	protected BigInteger prime;
	protected BigInteger publicNonce;
	protected BigInteger privateNonce;
	protected BigInteger intermediate;
	protected BigInteger sessionKey;
	protected String initVector;
	
	// Integrity
	protected KeyPair keyPair;
	protected String publicKey;
	
	protected Consumer<Message> receiver;
	
	public final String name;
	
	public Connector() {
		name = this.getClass().getSimpleName();
	}
	
	// State methods
	public void connect(
		final boolean requireConfidentiality, 
		final boolean requireIntegrity, 
		final boolean requireAuthentication,
		final Consumer<Status> accepter
	) {
		this.requireConfidentiality = requireConfidentiality;
		this.requireIntegrity       = requireIntegrity;
		this.requireAuthentication  = requireAuthentication;
		connect(accepter);
	}
	
	protected abstract void connect(final Consumer<Status> accepter);
	
	public Status authenticate(final String username, final String password) {
		// TODO
		authenticated = true;
		return new Status(false, "Not implemented.");
	}
	
	public Status disconnect() {
		authenticated = connected = false;
		return new Status(connected, "Disconnected");
	}
	
	public Status status() {
		if(!connected) {
			return new Status(false, "Not connected.");
		}
		if(requireAuthentication && !authenticated) {
			return new Status(false, "Not authenticated.");
		}
		return new Status(true, "Connected.");
	}
	
	public Status send(final Message message) {	

		assert(connected);
		assert(!requireAuthentication || authenticated);

		final Packet packet = new Packet();
		packet.type         = Packet.Type.MESSAGE;
		packet.payload      = requireConfidentiality
			? CIA.encrypt(sessionKey.toString(), initVector, message.message)
			: message.message;
		packet.signature    = requireIntegrity
			? CIA.sign(keyPair.privateKey, initVector, packet.serializeSansSig())
			: null;
		
		socket.send(packet);
		return new Status(true, "Sent.");
	}
	
	public void listen(final Consumer<Message> receiver) {
		this.receiver = receiver;
	}
	
}
