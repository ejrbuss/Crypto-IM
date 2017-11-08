package com.local.se360.test;

import com.local.se360.Packet;
import com.local.se360.Packet.Type;

public final class TestPacket {

	public static void main(String[] args) {
		TestPacket.start();
	}
	
	public static void start() {
		
		Packet send = new Packet();
		send.type = Type.PING;
		send.payload = "Hello World";
		
		Packet received = Packet.deserialize(send.serialize());
		System.out.println(received.payload);
		System.out.println(received.type.name());
	}
	
}
