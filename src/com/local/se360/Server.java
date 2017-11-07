package com.local.se360;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.function.Consumer;

public final class Server extends Connector implements Runnable {
	
	public static void main(String[] args) throws IOException {
		ChatApp.connect(new Server());
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
					connected = authenticated = false;
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
								sessionKey   		  = CIA.compute(prime, packet.intermediate, privateNonce);
								response.intermediate = intermediate = CIA.compute(prime, publicNonce, privateNonce);
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
	protected void connect(final Consumer<Status> accepter) {
		assert(!connected);
		assert(!authenticated);		
		(new Thread(this)).start();
	}
	
	@Override
	public Status authenticate(final String username, final String password) {
		// TODO
		authenticated = true;
		return new Status(false, "Not implemented.");
	}

}
