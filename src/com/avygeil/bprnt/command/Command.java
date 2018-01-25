package com.avygeil.bprnt.command;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class Command {
	
	private final String commandName;
	private final String permission;
	private final CommandCallback callback;
	
	public Command(String commandName, CommandCallback callback) {
		this(commandName, "", callback);
	}
	
	public Command(String commandName, String permission, CommandCallback callback) {
		this.commandName = commandName;
		this.permission = permission;
		this.callback = callback;
	}
	
	public String getCommandName() {
		return commandName;
	}
	
	public String getPermission() {
		return permission;
	}
	
	public void invoke(String[] args, IUser sender, IChannel channel, IMessage message) {
		callback.call(args, sender, channel, message);
	}
	
}
