package com.avygeil.bprnt.command;

import com.avygeil.bprnt.permission.NoPermissionException;
import com.avygeil.bprnt.permission.PermissionsHandler;
import com.avygeil.bprnt.util.DiscordUtils;
import com.avygeil.bprnt.util.FormatUtils;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class CommandStore {
	
	private final String commandPrefix;
	private final PermissionsHandler permissionsHandler;
	private final Map<String, Command> commands = new CaseInsensitiveMap<>();
	
	public CommandStore(String commandPrefix, PermissionsHandler permissionsHandler) {
		if (commandPrefix.length() == 0 ) {
			throw new IllegalArgumentException(); // prefix must not be empty
		}
		
		this.commandPrefix = commandPrefix;
		this.permissionsHandler = permissionsHandler;
	}
	
	public String getPrefix() {
		return commandPrefix;
	}
	
	public boolean hasCommand(String commandName) {
		return commands.containsKey(commandName);
	}
	
	public Command getCommandByName(String commandName) {
		return commands.get(commandName);
	}
	
	public void registerCommand(Command command) {
		commands.put(command.getCommandName(), command);
	}
	
	public void registerCommands(CommandFactory factory) {
		try {
			factory.build().forEach(this::registerCommand);
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}
	
	public void unregisterCommand(String commandName) {
		commands.remove(commandName);
	}
	
	public void handleMessage(Member sender, Message message) {
		message.getContent().ifPresent(content -> {
			final String cleanContent = content.trim();

			if (!cleanContent.startsWith(commandPrefix)) {
				return; // not a valid command
			}

			dispatchCommand(sender, message, cleanContent.substring(commandPrefix.length()));
		});
	}
	
	/*
	 * This method is used to dispatch a command regardless of the command prefix
	 * Message may not refer to a strictly correct message, but rather the message
	 * that "triggered" the command (this could be an aliased command, for instance),
	 * so use with caution
	 */
	public void dispatchCommand(Member sender, Message message, String content) {
		// for now, just split in two parts for efficiency: <command> <arguments>
		final String[] commandParts = StringUtils.split(content, null, 2);
		
		if (commandParts.length == 0) {
			return; // empty message?
		}
		
		// get the command name without the prefix part
		final String commandName = commandParts[0].trim();
		
		if (commandName.isEmpty()) {
			return; // we probably typed the prefix alone
		}
		
		final Command command = getCommandByName(commandName);
		
		if (command == null) {
			return; // no such command in store
		}
		
		// split arguments and invoke the command
		final String[] args = FormatUtils.tokenize(commandParts.length > 1 ? commandParts[1].trim() : "");
		
		try {
			command.invoke(permissionsHandler, args, sender, message);
		} catch (NoPermissionException e) {
			if (!e.getPermissionString().isEmpty()) {
				DiscordUtils.replyToMessage(message, "You don't have permission to use this command (" + e.getPermissionString() + ")");
			} else {
				DiscordUtils.replyToMessage(message, "You don't have permission to use this command");
			}
		} catch (InvalidUsageException e) {
			final String details = e.getDetails();
			final String correctUsage = e.getCorrectUsage();
			
			if (!details.isEmpty() || !correctUsage.isEmpty()) {
				StringBuilder sb = new StringBuilder();
				
				if (!details.isEmpty()) {
					sb.append(details);
					if (!correctUsage.isEmpty()) sb.append("\n\n");
				}
				
				if (!correctUsage.isEmpty()) {
					sb.append(correctUsage);
				}

				DiscordUtils.replyToMessage(message, sb.toString());
			}
		}
	}

}
