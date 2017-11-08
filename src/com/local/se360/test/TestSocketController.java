package com.local.se360.test;

import java.util.Scanner;

import com.local.se360.Config;
import com.local.se360.Packet;
import com.local.se360.SocketController;
import com.local.se360.Status;

public final class TestSocketController {
	
	public static void main(String[] args) {
		TestSocketController.start();
	}
	
	public static void start() {

		Packet packet;
		Scanner input = new Scanner(System.in);
		
		try {
			// Create a test server
			SocketController server = new SocketController(Config.PORT);
			// Create a test client
			SocketController client = new SocketController(Config.ADDRESS, Config.PORT);
			
			// Listen to status changes
			server.onChange((Status s) -> System.out.println("Server: " + s.message));
			client.onChange((Status s) -> System.out.println("Client: " + s.message));
			
			// Respond to messages
			server.on(Packet.Type.MESSAGE, (Packet p) -> System.out.println("Server Received: " + p.payload));
			client.on(Packet.Type.MESSAGE, (Packet p) -> System.out.println("Client Received: " + p.payload));
			
			// Start the server
			server.start();
			
			// Buffer a message on the client
			packet = new Packet();
			packet.type    = Packet.Type.MESSAGE;
			packet.payload = "Hello, World!";
			client.send(packet);
			
			// Start the client
			client.start();
			
			// Echo back from the server
			server.send(packet);
			
			// Wait then disconnect the client
			Thread.sleep(1000);
			client.close();
			
			// Start the client again
			client.start();
		
			for(;;) {
				
				// Server
				Thread.sleep(100);
				System.out.print("> ");
				System.out.flush();
				packet         = new Packet();
				packet.type    = Packet.Type.MESSAGE;
				packet.payload = input.nextLine();
				server.send(packet);
				
				// Client
				Thread.sleep(100);
				System.out.print("> ");
				System.out.flush();
				packet         = new Packet();
				packet.type    = Packet.Type.MESSAGE;
				packet.payload = input.nextLine();
				client.send(packet);
			}
		} catch (InterruptedException e) {
			// Sleep failed!!!
			e.printStackTrace();
			System.exit(-1);
		} finally {			
			input.close();
		}
	}

}
