package com.local.se360;

public final class Status {
	
	public final boolean success;
	public final String  message;
	
	public Status(final boolean success, final String message) {
		this.success = success;
		this.message = message;
	}
}