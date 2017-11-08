package com.local.se360;

public final class Config {

	public final static int PORT       = 9090;
	public final static String ADDRESS = "127.0.0.1";
	
	public static void log(final String message) {
		// Comment out to hide logs
		System.out.println(message);
	}
	
	public static void panic(final String message, final Exception e) {
		// Something bad has happened!
		System.err.println("PANIC! " + message);
		e.printStackTrace();
		System.exit(-1);
	}
	
}
