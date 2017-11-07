package com.local.se360;

import java.util.function.Consumer;

public final class Server extends Connector {
	
	public static void main(String[] args) {
		ChatApp.connect(new Server());
	}
	
	public Server() {
		super();
		
		socket = new SocketController(Config.PORT);
		socket.onChange((final Status s) -> connected = s.success);
		
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
					publicKey          = packet.publicKey;
					response.publicKey = keyPair.publicKey;
				}
				Config.log("Sending PONG...");
				socket.send(response);
				connected = true;
			} else {
				// Deny
				final Packet response = new Packet();
				response.type = Packet.Type.DENY;
				Config.log("Sending DENY...");
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
				&& (!requireIntegrity || CIA.checkSignature(publicKey, initVector, packet.signature, packet.serializeSansSig()))						) {
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
	
	@Override
	public Status authenticate(final String username, final String password) {
		// TODO
		return new Status(false, "Not implemented.");
	}

}
