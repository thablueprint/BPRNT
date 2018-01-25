package com.avygeil.bprnt.command;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.avygeil.bprnt.permission.PermissionsHandler;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class CommandStore {
	
	private final String commandPrefix;
	private final PermissionsHandler permissionsHandler;
	private final Map<String, Command> commands = new HashMap<>();
	
	public CommandStore(String commandPrefix, PermissionsHandler permissionsHandler) {
		if (commandPrefix.length() == 0 ) {
			throw new IllegalArgumentException(); // prefix must not be empty
		}
		
		this.commandPrefix = commandPrefix;
		this.permissionsHandler = permissionsHandler;
	}
	
	public boolean hasCommand(String commandName) {
		return commands.containsKey(commandName);
	}
	
	public void registerCommand(Command command) {
		commands.put(command.getCommandName().toLowerCase(), command);
	}
	
	public void handleCommand(IUser sender, IChannel channel, IMessage message) {
		String content = message.getContent().trim();
		
		if (!content.startsWith(commandPrefix)) {
			return; // not a valid command
		}
		
		// for now, just split in two parts for efficiency: <command> <arguments>
		final String[] commandParts = StringUtils.split(content, null, 2);
		
		if (commandParts.length == 0) {
			return; // empty message?
		}
		
		// get the command name without the prefix part
		final String commandName = commandParts[0].trim().substring(commandPrefix.length());
		
		if (commandName.isEmpty()) {
			return; // we probably typed the prefix alone
		}
		
		// since command names are case insensitive, we search for it as lower case
		final Command command = commands.get(commandName.toLowerCase());
		
		if (command == null) {
			return; // no such command in store
		}
		
		// we know the command exists, so let's test for permissions before the rest
		if (!permissionsHandler.hasPermission(sender, command.getPermission())) {
			message.reply("You don't have permission to run this command");
			return;
		}
		
		// split arguments and invoke the command
		
		final String[] args = StringUtils.split(commandParts.length > 1 ? commandParts[1].trim() : "");
		
		command.invoke(args, sender, channel, message);
	}

}
