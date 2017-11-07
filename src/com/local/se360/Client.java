package com.local.se360;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.util.function.Consumer;

public final class Client extends Connector implements Runnable {

	public static void main(String[] args) {
		ChatApp.connect(new Client());
	}
	
	// Sockets
	private PrintWriter writer;
	private BufferedReader reader;
	private String waiting;
	
	// Confidentiality
	private BigInteger prime;
	private BigInteger publicNonce;
	private BigInteger privateNonce;
	private BigInteger intermediate;
	private BigInteger sessionKey;
	private String initVector;
	
	// Integrity
	private KeyPair keyPair;
	private String publicKey;
	
	private Consumer<Status> accepter;
	
	@Override
	public void run() {
		try {
			Config.log("Opening socket...");
			final Socket socket = new Socket(Config.ADDRESS, Config.PORT);
			try {
				waitOnSocket(socket);
			} finally {
				socket.close();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void waitOnSocket(final Socket socket) throws IOException {
		writer = new PrintWriter(socket.getOutputStream(), true);
		reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		
		// Clear any waiting packets
		if(waiting != null) {
			Config.log("Sending waiting message...");
			writer.println(waiting);
			writer.flush();
			waiting = null;
		}
		
		for(;;) {
			Config.log("Waiting on socket...");
			final String read = reader.readLine();
			
			if(read == null) { 
				Config.log("Lost connection with the server...");
				connected     = false;
				authenticated = false;
				return; 
			}
			final Packet packet = new Packet(read);
			Config.log("Message [type=" + packet.type.name() + "] received...");
			switch(packet.type) {
				case PONG: 
					if(requireConfidentiality) {
						sessionKey = CIA.compute(prime, packet.intermediate, privateNonce);						
					}
					if(requireIntegrity) {
						publicKey = packet.publicKey;
					}
					connected = true;
					if(accepter != null) {
						accepter.accept(new Status(true, "Connected."));
					}
					break;
				case MESSAGE: 
					final String payload = requireConfidentiality
						? CIA.decrypt(sessionKey.toString(), initVector, packet.payload)
						: packet.payload;
					if(connected 
						&& receiver != null 
						&& (!requireAuthentication || authenticated) 
						&& (!requireIntegrity || CIA.checkSignature(publicKey, initVector, packet.signature, packet.serializeSansSig()))
					) {
						receiver.accept(new Message("Server", payload));
					}
					break;
				case DENY:
					if(accepter != null) {
						accepter.accept(new Status(false, "Failed to connect."));
					}
					return;
				default:
					throw new RuntimeException("Unexpected packet type: " + packet.type.name());
			}
		}
	}
	
	@Override
	public void connect(final Consumer<Status> accepter) {
		if(connected) {
			accepter.accept(new Status(true, "Connected."));
			return;
		}
		final Packet packet = new Packet();
		
		// Configuration
		packet.type                   = Packet.Type.PING;
		packet.requireConfidentiality = requireConfidentiality;
		packet.requireIntegrity       = requireIntegrity;
		packet.requireAuthentication  = requireAuthentication;
		
		initVector        = CIA.generateNonce().toString();
		packet.initVector = initVector;
		
		// Confidentiality
		if(requireConfidentiality) {
			prime 		 		= CIA.generatePrime();
			publicNonce 		= CIA.generateNonce();
			privateNonce 		= CIA.generateNonce();
			intermediate 		= CIA.compute(prime, publicNonce, privateNonce); 
			packet.prime        = prime;
			packet.nonce        = publicNonce;
			packet.intermediate = intermediate;
		}
		
		// Integrity
		if(requireIntegrity) {
			keyPair 		 = CIA.generateKeyPair();
			packet.publicKey = keyPair.publicKey;
		}
		
		waiting = packet.serialize();
		
		this.accepter = accepter;
		
		(new Thread(this)).start();
	}
	
	@Override
	public Status authenticate(final String username, final String password) {
		// TODO
		return new Status(false, "Not implemented.");
	}
	
	@Override
	public String name() {
		return "Client";
	}
	
	@Override
	public Status send(final Message message) {	
		if(writer == null || !connected) {
			return new Status(false, "Not connected.");
		}
		if(requireAuthentication && !authenticated) {
			return new Status(false, "Not authenticated.");
		}
		final Packet packet = new Packet();
		packet.type         = Packet.Type.MESSAGE;
		packet.payload      = requireConfidentiality
			? CIA.encrypt(sessionKey.toString(), initVector, message.message)
			: message.message;
		packet.signature    = requireIntegrity
			? CIA.sign(keyPair.privateKey, initVector, packet.serializeSansSig())
			: null;
		
		writer.println(packet.serialize());
		writer.flush();
		return new Status(true, "Sent.");
	}

}
