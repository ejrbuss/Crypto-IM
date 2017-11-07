package com.local.se360;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.function.Consumer;

public final class Client extends Connector implements Runnable {

	public static void main(String[] args) {
		ChatApp.connect(new Client());
	}
	
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
		} catch(SocketException e) { // Ignore, server just left ungracefully
			connected     = false;
			authenticated = false;
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
				connected = authenticated = false;
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
	protected void connect(final Consumer<Status> accepter) {
		
		assert(!connected);
		assert(!authenticated);
		
		final Packet packet = new Packet();
		
		// Configuration
		packet.type                   = Packet.Type.PING;
		packet.requireConfidentiality = requireConfidentiality;
		packet.requireIntegrity       = requireIntegrity;
		packet.requireAuthentication  = requireAuthentication;
		packet.initVector             = initVector = CIA.generateNonce().toString();
		
		// Confidentiality
		if(requireConfidentiality) {	
			privateNonce 		= CIA.generateNonce();
			packet.prime        = prime        = CIA.generatePrime();;
			packet.nonce        = publicNonce  = CIA.generateNonce();;
			packet.intermediate = intermediate = CIA.compute(prime, publicNonce, privateNonce); ;
		}

		// Integrity
		if(requireIntegrity) {
			keyPair 		 = CIA.generateKeyPair();
			packet.publicKey = keyPair.publicKey;
		}
		
		waiting       = packet.serialize();
		this.accepter = accepter;
		(new Thread(this)).start();
	}
	
	@Override
	public Status authenticate(final String username, final String password) {
		// TODO
		return new Status(false, "Not implemented.");
	}

}
