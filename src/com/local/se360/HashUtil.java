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

		String p1  = "admin";
		String p2  = "password";
		String p3  = "12345";
		String p4  = "pass";
		String p5  = "qwerty";
		String p6  = "asdf";
		String p7  = "password1!";
		String p8  = "pa$$word";
		String p9  = "11111";
		String p10 = "54321";
		
		String p1Hash  = HashUtil.hash(p1);
		String p2Hash  = HashUtil.hash(p2);
		String p3Hash  = HashUtil.hash(p3);
		String p4Hash  = HashUtil.hash(p4);
		String p5Hash  = HashUtil.hash(p5);
		String p6Hash  = HashUtil.hash(p6);
		String p7Hash  = HashUtil.hash(p7);
		String p8Hash  = HashUtil.hash(p8);
		String p9Hash  = HashUtil.hash(p9);
		String p10Hash = HashUtil.hash(p10);
		
		try {
			PrintWriter writer = new PrintWriter("Data.txt", "UTF-8");
			writer.println("Admin\n" + p1Hash);
			writer.println("User1\n" + p2Hash);
			writer.println("User2\n" + p3Hash);
			writer.println("User3\n" + p4Hash);
			writer.println("User4\n" + p5Hash);
			writer.println("User5\n" + p6Hash);
			writer.println("User6\n" + p7Hash);
			writer.println("User7\n" + p8Hash);
			writer.println("User8\n" + p9Hash);
			writer.println("User9\n" + p10Hash);
			writer.close();
		} catch (Exception e) {
			Config.panic("Failed to write data", e);
		} 
	}

}
