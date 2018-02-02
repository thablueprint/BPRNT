package com.avygeil.bprnt.command;

public class InvalidUsageException extends Exception {
	
	private static final long serialVersionUID = -1266130872942751716L;
	
	private final String details;
	private final String correctUsage;

	public InvalidUsageException() {
		this("");
	}
	
	public InvalidUsageException(String details) {
		this(details, "");
	}

	public InvalidUsageException(String details, String correctUsage) {
		this.details = details;
		this.correctUsage = correctUsage;
	}
	
	public String getDetails() {
		return details;
	}
	
	public String getCorrectUsage() {
		return correctUsage;
	}

}
