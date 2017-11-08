package com.local.se360;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public class Util {

	public static String hashMethod(String text) {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
			return new String(Base64.getEncoder().encode(hash));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static void generatePasswordFile() {
		
		String p1 = "admin";
		String p2 = "password";
		String p3 = "12345";
		String p4 = "pass";
		String p5 = "qwerty";
		String p6 = "asdf";
		String p7 = "password1!";
		String p8 = "pa$$word";
		String p9 = "11111";
		String p10 = "54321";
		
		String p1Hash = Util.hashMethod(p1);
		String p2Hash = Util.hashMethod(p2);
		String p3Hash = Util.hashMethod(p3);
		String p4Hash = Util.hashMethod(p4);
		String p5Hash = Util.hashMethod(p5);
		String p6Hash = Util.hashMethod(p6);
		String p7Hash = Util.hashMethod(p7);
		String p8Hash = Util.hashMethod(p8);
		String p9Hash = Util.hashMethod(p9);
		String p10Hash = Util.hashMethod(p10);
		
		
		
		
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
		
		
		}catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Util.generatePasswordFile();
		System.out.println("In theory you created the data file");

	}

}
