package com.local.se360;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.PublicKey;
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
	protected PublicKey publicKey;
	
	protected Consumer<Message> receiver;
	protected Consumer<Status> keeper;
	
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
		String passwordHash = HashUtil.hash(password);
		
		
		try {
			File file = new File("Data.txt");
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			//StringBuffer stringBuffer = new StringBuffer();
			boolean foundUser = false;
			String line;
			
			while ((line = bufferedReader.readLine()) != null) {
				if (foundUser == true) {
					if (passwordHash.compareTo(line) == 0) {
						bufferedReader.close();
						fileReader.close();
						authenticated = true;
						return new Status(true, "Authenticated");
					}else {
						bufferedReader.close();
						fileReader.close();
						return new Status(false, "Invallid Username/Password");
					}
				}
				
				if (line.compareTo(username) == 0) {
					foundUser = true;
				}
			}
			bufferedReader.close();
			fileReader.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new Status(false, "Invallid Username/Password");
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
			? CIA.sign(keyPair.getPrivate(), packet.serializeSansSig())
			: null;
			
		socket.send(packet);
		return new Status(true, "Sent.");
	}
	
	public void listen(final Consumer<Message> receiver) {
		this.receiver = receiver;
	}
	
	public void listenStatus(final Consumer<Status> keeper) {
		this.keeper = keeper;
	}
	
}
