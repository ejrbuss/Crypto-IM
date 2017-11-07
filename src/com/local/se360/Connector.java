package com.local.se360;

import java.util.function.Consumer;

public abstract class Connector {
	
	// Configuration
	protected boolean requireConfidentiality;
	protected boolean requireIntegrity;
	protected boolean requireAuthentication;
	
	// Status
	protected boolean connected     = false;
	protected boolean authenticated = false;
	
	protected Consumer<Message> receiver;

	// Getters and setters for connection properties
	public void requireConfidentiality(boolean yes) {
		if(requireConfidentiality != yes) {
			requireConfidentiality = yes;
			disconnect();
		}
	}
	
	public boolean requireConfidentiality() {
		return requireConfidentiality;
	}
	
	public void requireIntegrity(boolean yes) {
		if(requireIntegrity != yes) {
			requireIntegrity = yes;
			disconnect();
		}
	}
	
	public boolean requireIntegrity() {
		return requireIntegrity;
	}
	
	public void requireAuthentication(boolean yes) {
		if(requireAuthentication != yes) {
			requireAuthentication = yes;
			disconnect();
		}
	}
	
	public boolean requireAuthentication() {
		return requireAuthentication;
	}
	
	// State methods
	public abstract String name();
	public abstract void connect(final Consumer<Status> accepter);
	public abstract Status authenticate(final String username, final String password);
	
	public Status disconnect() {
		authenticated = false;
		connected     = false;
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
	
	// Messaging methods
	public abstract Status send(final Message message);
	
	public void listen(final Consumer<Message> receiver) {
		this.receiver = receiver;
	}
	
}
