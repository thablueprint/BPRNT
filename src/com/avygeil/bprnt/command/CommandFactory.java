package com.avygeil.bprnt.command;

public class CommandFactory {
	
	private String commandName;
	private String permission;
	private CommandCallback callback;
	
	public CommandFactory setCommandName(String commandName) {
		this.commandName = commandName;
		return this;
	}
	
	public CommandFactory setPermission(String permission) {
		this.permission = permission;
		return this;
	}
	
	public CommandFactory setCallback(CommandCallback callback) {
		this.callback = callback;
		return this;
	}
	
	public Command build() {
		return new Command(commandName, permission, callback);
	}

}
