package com.local.se360;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public class HashUtil {

	public static String hash(String text) {
		try {
			final MessageDigest digest = MessageDigest.getInstance("SHA-256");
			final byte[]        hash   = digest.digest(text.getBytes(StandardCharsets.UTF_8));
			return new String(Base64.getEncoder().encode(hash));
		} catch (Exception e) {
			Config.panic("Failed to hash", e);
		}
		throw new RuntimeException("Unreachable");
	}
	
	public static void generatePasswordFile() {
		try {
			final PrintWriter writer = new PrintWriter("Data.txt", "UTF-8");
			writer.println("Admin\n" + hash("admin"));
			writer.println("User1\n" + hash("password"));
			writer.println("User2\n" + hash("12345"));
			writer.println("User3\n" + hash("pass"));
			writer.println("User4\n" + hash("qwerty"));
			writer.println("User5\n" + hash("asdf"));
			writer.println("User6\n" + hash("password1!"));
			writer.println("User7\n" + hash("pa$$word"));
			writer.println("User8\n" + hash("11111"));
			writer.println("User9\n" + hash("54321"));
			writer.close();
		} catch (Exception e) {
			Config.panic("Failed to write data", e);
		} 
	}

}
