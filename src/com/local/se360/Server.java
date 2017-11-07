package com.local.se360;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.function.Consumer;

public final class Server implements Connector, Runnable {
	
	public static void main(String[] args) throws IOException {
		final Server instance = new Server();
		ChatApp.connect(instance);
	}
	
	// Socket
	private PrintWriter writer;
	private BufferedReader reader;
	
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
	private KeyPair keyPair = CIA.generateKeyPair();
	private String publicKey;
	
	private Consumer<Message> receiver;
	
	public Server() {
		// Provide a default receiver that prints debug messages;
		receiver = (Message m) -> { System.out.println("Recieved: " + m.message); };
	}
	
	@Override
	public void run() {
		try {
			Config.log("Opening socket...");
			final ServerSocket serverSocket = new ServerSocket(Config.PORT);
			try {
				for(;;) { waitOnSocket(serverSocket.accept()); }
			} finally {
				serverSocket.close();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void waitOnSocket(final Socket socket) throws IOException {
		try {
			writer = new PrintWriter(socket.getOutputStream(), true);
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			for(;;) {
				Config.log("Waiting on socket...");
				final String read = reader.readLine();

				// We've lost connection with the client
				if(read == null) { 
					Config.log("Lost connection with the client...");
					connected     = false;
					authenticated = false;
					return; 
				}
				
				final Packet packet = new Packet(read);
				Config.log("Message [type=" + packet.type.name() + "] received...");
				switch(packet.type) {
					case PING:
						if(
							packet.requireConfidentiality == requireConfidentiality &&
							packet.requireIntegrity       == requireIntegrity 	    &&
							packet.requireAuthentication  == requireAuthentication
						) {
							final Packet response = new Packet();
							response.type = Packet.Type.PONG;
							
							initVector   = packet.initVector;
							
							// Accept
							if(requireConfidentiality) {
								prime        		  = packet.prime;
								publicNonce  		  = packet.nonce;
								privateNonce 		  = CIA.generateNonce();
								intermediate 		  = CIA.compute(prime, publicNonce, privateNonce);
								sessionKey   		  = CIA.compute(prime, packet.intermediate, privateNonce);
								response.intermediate = intermediate;
							}
							if(requireIntegrity) {
								publicKey          = packet.publicKey;
								response.publicKey = keyPair.publicKey;
							}
							Config.log("Sending PONG...");
							writer.println(response.serialize());
							writer.flush();
							connected = true;
						} else {
							// Deny
							final Packet response = new Packet();
							response.type = Packet.Type.DENY;
							Config.log("Sending DENY...");
							writer.println(response.serialize());
							writer.flush();
						}
						break;
					case MESSAGE: 
						final String payload = requireConfidentiality
							? CIA.decrypt(sessionKey.toString(), initVector, packet.payload)
							: packet.payload;
						if(connected 
							&& receiver != null 
							&& (!requireAuthentication || authenticated)
							&& (!requireIntegrity || CIA.checkSignature(publicKey, initVector, packet.signature, packet.serializeSansSig()))						) {
							receiver.accept(new Message("Client", payload));
						}
						break;
					default:
						throw new RuntimeException("Unexpected packet type: " + packet.type.name());
				}
			}
		} catch(SocketException e) { // Ignore, client just left ungracefully
			connected     = false;
			authenticated = false;
		} finally {
			socket.close();
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
	public boolean requireAuthentication() {
		return this.requireAuthentication;
	}
	
	@Override
	public void connect(final Consumer<Status> accepter) {
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
		return "Server";
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
	
	@Override
	public void listen(final Consumer<Message> receiver) {
		this.receiver = receiver;
	}

}
