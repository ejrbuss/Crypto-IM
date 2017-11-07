package com.local.se360;

import java.util.function.Consumer;

public final class Client extends Connector {

	public static void main(String[] args) {
		ChatApp.connect(new Client());
	}
	
	private Consumer<Status> accepter;
	
	public Client() {
		super();
		
		socket = new SocketController(Config.ADDRESS, Config.PORT);
		socket.onChange((final Status s) -> connected = s.success);
		
		// Handle PONG packets
		socket.on(Packet.Type.PONG, (final Packet packet) -> {
			connected = true;
			if(requireConfidentiality) {
				sessionKey = CIA.compute(prime, packet.intermediate, privateNonce);						
			}
			if(requireIntegrity) {
				publicKey = packet.publicKey;
			}
			if(accepter != null) {
				accepter.accept(new Status(true, "Connected."));
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
				&& (!requireIntegrity || CIA.checkSignature(publicKey, initVector, packet.signature, packet.serializeSansSig()))
			) {
				receiver.accept(new Message("Server", payload));
			}
		});
		
		// Handle DENY packets
		socket.on(Packet.Type.DENY, (final Packet packet) -> {
			if(accepter != null) {
				accepter.accept(new Status(false, "Failed to connect."));
			}
			socket.close();
		});
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
		
		this.accepter = accepter;
		socket.send(packet);
		socket.start();
	}
	
	@Override
	public Status authenticate(final String username, final String password) {
		// TODO
		return new Status(false, "Not implemented.");
	}
	
}
