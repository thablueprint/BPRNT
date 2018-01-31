package com.avygeil.bprnt.command;

public final class CommandPriority {
	
	private CommandPriority() {
	}
	
	// fake enum fields for easy int conversion
	// higher priority = earlier in queue
	
	public static final int MONITORING = Integer.MAX_VALUE;
	public static final int HIGHEST = 100000;
	public static final int HIGHER = 10000;
	public static final int HIGH = 1000;
	public static final int NORMAL = 0;
	public static final int LOW = -1000;
	public static final int LOWER = -10000;
	public static final int LOWEST = -100000;
	public static final int LOGGING = Integer.MIN_VALUE;

}
