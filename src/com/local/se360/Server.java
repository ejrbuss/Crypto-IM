package com.local.se360;

import java.util.function.Consumer;

public final class Server implements Connector{
	
	public static void main(String[] args) {
		ChatApp.connect(new Server());
	}
	
	private boolean requireConfidentiality = false;
	private boolean requireIntegrity 	   = false;
	private boolean requireAuthentication  = false;
	
	private boolean connected              = false;
	
	@SuppressWarnings("unused")
	private Consumer<Message> receiver;
	
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
	public Status connect() {
		
		// TODO 
		// Throw error if called. The Server should never be asked to connect.
		
		return null;
	}
	
	@Override
	public Status disconnect() {
		
		// TODO
		// Perform any additional steps that need to occur in order to kick the client
		
		return new Status(this.connected = false, "Disconnected");
	}
	
	@Override
	public Status status() {
		return new Status(this.connected, this.connected
			? "Connected"
			: "Disconnected"
		);
	}
	
	@Override
	public String name() {
		return "Server";
	}
	
	@Override
	public Status send(final Message message) {
		
		// TODO
		// Send a message if connected to the client
		// NOTE: implementing this method this may require the creation of additional methods 
		
		final boolean sent = false;
		
		return new Status(sent, "Unimplemented");
	}
	
	@Override
	public void listen(final Consumer<Message> receiver) {
		
		// TODO 
		// Should register the receiver to receive any messages received by the server
		// ie. the receiver will likely be called on a different thread
		// NOTE: implementing this method this will require the creation of additional methods 
		
		this.receiver = receiver;
	}

}
