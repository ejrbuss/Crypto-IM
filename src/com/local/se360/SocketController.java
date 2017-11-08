package com.local.se360;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.function.Consumer;

public final class SocketController {
	
	public final int port;
	public final boolean host;
	public final String address;
	
	private final Stack<Packet> waiting;
	private final Map<Packet.Type, Consumer<Packet>> handlers;
	
	private PrintWriter writer;
	private BufferedReader reader;
	private Consumer<Status> accepter;
	
	private ServerSocket serverSocket;
	private Socket socket;
	
	private boolean open;
	
	public SocketController(final int port) {
		this.port     = port;
		this.address  = null;
		this.host     = true;
		this.waiting  = new Stack<Packet>();
		this.handlers = new HashMap<Packet.Type, Consumer<Packet>>(); 
	}
	
	public SocketController(final String address, final int port) {
		this.port 	  = port;
		this.address  = address;
		this.host     = false;
		this.waiting  = new Stack<Packet>();
		this.handlers = new HashMap<Packet.Type, Consumer<Packet>>();
	}
	
	public void start() {
		open = true;
		(new Thread(() -> {
			try {
				if(host) {
					Config.log("Opening host socket...");
					serverSocket = new ServerSocket(port);		
					try {
						while(host && open) { waitOnSocket(serverSocket.accept()); }
					} finally {
						serverSocket.close();
					}
				} else {
					Config.log("Opening client socket...");
					socket = new Socket(address, port);
					try {
						waitOnSocket(socket);
					} finally {
						socket.close();
					}
				}				
			} catch(IOException e) {
				// Socket Already in use!!
				e.printStackTrace();
				System.exit(-1);
			}
			Config.log("Closing connection...");
		})).start();
	}
	
	private void waitOnSocket(final Socket socket) throws IOException {
		
		boolean connected = false;
		
		try {
			
			writer = new PrintWriter(socket.getOutputStream(), true);
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			while(!waiting.empty()) {
				send(waiting.pop());
			}
		
			while(open) {
				Config.log("Waiting on socket...");
				final String read = reader.readLine();

				if(read == null) { 
					Config.log("Lost connection...");
					change(new Status(false, "Lost Connection."));
					return; 
				}
				if(!connected) {
					change(new Status(true, "Connected."));
					connected = true;
				}
				
				final Packet packet = Packet.parse(read);
				Config.log("Recieved " + packet.type.name() + " packet...");
				assert(handlers.containsKey(packet.type));
				handlers.get(packet.type).accept(packet);
			}
		} catch(SocketException e) { 
			// Socket partner left ungracefully
			change(new Status(false, "Lost Connection."));
		} finally {
			socket.close();
		}
	}
	
	private void change(final Status newStatus) {
		if(accepter != null) {
			accepter.accept(newStatus);
		}
	}
	
	public void on(final Packet.Type type, final Consumer<Packet> handler) {
		handlers.put(type, handler);
	}
	
	public void onChange(final Consumer<Status> accepter) {
		this.accepter = accepter;
	}
	
	public void send(final Packet packet) {
		if(writer == null) {
			waiting.push(packet);
		} else {
			Config.log("Sending " + packet.type.name() + " packet...");
			writer.println(packet.serialize());
			writer.flush();			
		}
	}
	
	public void close() {
		open   = false;
		writer = null;
		reader = null;
		try {
			if(socket != null)       { socket.close();       }
			if(serverSocket != null) { serverSocket.close(); }
		} catch(IOException e) {
			Config.panic("Failed to close socket", e);
		}
	}
	
}
