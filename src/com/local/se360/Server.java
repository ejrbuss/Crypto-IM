package com.local.se360;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.function.Consumer;

public final class Server implements Connector, Runnable {
	
	public static void main(String[] args) throws IOException {
		final Server instance = new Server();
		(new Thread(instance)).start();
		ChatApp.connect(instance);
	}
	
	private PrintWriter writer;
	private BufferedReader reader;
	
	private boolean requireConfidentiality = false;
	private boolean requireIntegrity 	   = false;
	private boolean requireAuthentication  = false;
	
	private boolean connected              = false;
	
	private Consumer<Message> receiver;
	
	public Server() {
		// Provide a default receiver that prints debug messages;
		receiver = (Message m) -> { System.out.println("Recieved: " + m.message); };
	}
	
	@Override
	public void run() {
		try {
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
				final String read = reader.readLine();
				if(read == null) { return; }
				if(receiver != null) { 
					receiver.accept(new Message("Client", read));
				}
			}
		} catch(SocketException e) { // Ignore, client just left ungracefully
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
		if(writer != null) {
			writer.println(message.message);
			writer.flush();
			return new Status(true, "Sent.");
		}
		return new Status(false, "Not connected.");
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
