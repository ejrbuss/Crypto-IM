package com.local.se360;

import java.util.function.Consumer;

public final class Server extends Connector {
	
	public static void main(String[] args) {
		ChatApp.connect(new Server());
	}
	
	public Server() {
		super();
		
		
		socket = new SocketController(Config.PORT);
		socket.onChange((final Status s) -> {
			connected     = s.success;
			authenticated = authenticated && connected;
			if(keeper != null) { keeper.accept(status()); }
		});
		
		// Handle PING packets
		socket.on(Packet.Type.PING, (final Packet packet) -> {
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
					keyPair 		   = CIA.generateKeyPair();
					publicKey          = packet.publicKey;
					response.publicKey = keyPair.getPublic();
				}
				
				socket.send(response);
				connected = true;
			} else {
				// Deny
				final Packet response = new Packet();
				response.type = Packet.Type.DENY;
				socket.send(response);
			}
		});
		
		// Handle MESSAGE packets
		socket.on(Packet.Type.MESSAGE, (final Packet packet) -> {
			final String payload = requireConfidentiality
				? CIA.decrypt(sessionKey.toString(), initVector, packet.payload)
				: packet.payload;
			if(connected 
				&& receiver != null 
				&& (!requireAuthentication || authenticated)
				&& (!requireIntegrity || CIA.checkSignature(publicKey, packet.signature, packet.serializeSansSig()))) {
				System.out.println("accepting message...");
				receiver.accept(new Message("Client", payload));
			}
		});
	}
	
	@Override
	protected void connect(final Consumer<Status> accepter) {
		assert(!connected);
		assert(!authenticated);		
		socket.start();
	}

}
