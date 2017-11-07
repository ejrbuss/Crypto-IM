package com.local.se360;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.util.function.Consumer;

public final class Client implements Connector, Runnable {

	public static void main(String[] args) {
		final Client instance = new Client();
		ChatApp.connect(instance);
	}
	
	// Sockets
	private PrintWriter writer;
	private BufferedReader reader;
	private String waiting;
	
	// Configuration
	private boolean requireConfidentiality = false;
	private boolean requireIntegrity 	   = false;
	private boolean requireAuthentication  = false;
	
	// Status
	private boolean connected     = false;
	private boolean authenticated = false;
	
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
	
	private Consumer<Message> receiver;
	private Consumer<Status> accepter;
	
	public Client() {
		// Provide a default receiver that prints debug messages;
		receiver = (Message m) -> { System.out.println("Recieved: " + m.message); };
	}
	
	@Override
	public void run() {
		try {
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
			writer.println(waiting);
			writer.flush();
			waiting = null;
		}
		
		for(;;) {
			final String read = reader.readLine();
			
			// We've lost connection with the server
			if(read == null) { 
				connected     = false;
				authenticated = false;
				return; 
			}
			
			final Packet packet = new Packet(read);
			switch(packet.type) {
				case PONG: 
					if(requireConfidentiality) {
						sessionKey = CIA.compute(prime, packet.intermediate, privateNonce);						
					}
					if(requireIntegrity) {
						publicKey = packet.publicKey;
					}
					connected = true;
					break;
				case MESSAGE: 
					final String payload = requireConfidentiality
						? CIA.decrypt(sessionKey.toString(), initVector, packet.payload)
						: packet.payload;
					if(connected 
						&& receiver != null 
						&& (!requireAuthentication || authenticated) 
						&& (!requireIntegrity || CIA.checkSignature(publicKey, packet.signature, packet.serializeSansSig()))
					) {
						receiver.accept(new Message("Server", payload));
					}
					break;
				case DENY:
					if(accepter != null) {
						accepter.accept(new Status(false, "Failed to connect."));
					}
					break;
				default:
					throw new RuntimeException("Unexpected packet type: " + packet.type.name());
			}
		}
	}
	
	@Override
	public void requireConfidentiality(boolean yes) {
		if(this.requireConfidentiality != yes) {
			this.requireConfidentiality = yes;
			this.disconnect();
		}
	}
	
	@Override
	public boolean requireConfidentiality() {
		return this.requireConfidentiality;
	}
	
	@Override
	public void requireIntegrity(boolean yes) {
		if(this.requireIntegrity != yes) {
			this.requireIntegrity = yes;
			this.disconnect();
		}
	}
	
	@Override
	public boolean requireIntegrity() {
		return this.requireIntegrity;
	}
	
	@Override
	public void requireAuthentication(boolean yes) {
		if(this.requireAuthentication != yes) {
			this.requireAuthentication = yes;
			this.disconnect();
		}
	}
	
	@Override
	public boolean requireAuthenticationy() {
		return this.requireAuthentication;
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
	public Status disconnect() {
		authenticated = false;
		connected     = false;
		return new Status(connected, "Disconnected");
	}
	
	@Override
	public Status status() {
		if(!connected) {
			return new Status(false, "Not connected.");
		}
		if(requireAuthentication && !authenticated) {
			return new Status(false, "Not authenticated.");
		}
		return new Status(true, "Connected.");
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
			? CIA.sign(keyPair.privateKey, packet.serializeSansSig())
			: null;
		
		writer.println(packet.serialize());
		writer.flush();
		return new Status(true, "Sent.");
	}
	
	@Override
	public void listen(final Consumer<Message> receiver) {
		this.receiver = receiver;
	}

}
