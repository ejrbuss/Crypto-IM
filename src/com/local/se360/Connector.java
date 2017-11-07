package com.local.se360;

import java.util.function.Consumer;

public interface Connector {

	// Getters and setters for connection properties
	public void    requireConfidentiality(final boolean yes);
	public boolean requireConfidentiality();
	
	public void    requireIntegrity(final boolean yes);
	public boolean requireIntegrity();
	
	public void    requireAuthentication(final boolean yes);
	public boolean requireAuthentication();
	
	// State methods
	public void connect(final Consumer<Status> accepter);
	public Status authenticate(final String username, final String password);
	public Status disconnect();
	public Status status();
	public String name();
	
	// Messaging methods
	public Status send(final Message message);
	public void listen(final Consumer<Message> receiver);
	
	// Inner Classes and Interfaces
	public final class Status {
		
		public final boolean success;
		public final String  message;
		
		public Status(final boolean success, final String message) {
			this.success = success;
			this.message = message;
		}
	}
	
	public class Message {
		
		public final String sender;
		public final String message;
		
		public Message(final String sender, final String message) {
			this.sender  = sender;
			this.message = message;
		}
	}
	
}
