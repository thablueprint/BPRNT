package com.avygeil.bprnt.command;

import java.util.Queue;

import com.avygeil.bprnt.util.FormatUtils;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class SimpleCommand extends CommandBase {
	
	private final String permission;
	private final CommandCallback callback;
	private final CommandFormat format;
	
	public SimpleCommand(String commandName, CommandCallback callback) {
		this(commandName, "", callback, new CommandFormat());
	}
	
	public SimpleCommand(String commandName, String permission, CommandCallback callback, CommandFormat format) {
		this(null, commandName, permission, callback, format);
	}
	
	public SimpleCommand(ParentCommand parent, String commandName, String permission, CommandCallback callback, CommandFormat format) {
		super(parent, commandName);
		
		this.permission = permission;
		this.callback = callback;
		this.format = format;
	}
	
	@Override
	public String getPermission() {
		return permission;
	}
	
	@Override
	public String getUsage() {
		// this command is always at the end point of a command chain,
		// so to get the usage, we can just pair the format string
		// with the name of all parents to recreate the chain
		
		// TODO: cache this later?
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(FormatUtils.getChainedCommandNamesString(parent));
		if (sb.length() > 0) sb.append(" ");
		sb.append(format.getFormatString());
		
		return sb.toString();
	}

	@Override
	protected Command invoke_Internal(Queue<String> argQueue, IUser sender, IChannel channel, IMessage message)
			throws InvalidUsageException {
		
		// first, parse the arguments according to the format
		// TODO
		
		// invoke the command
		callback.call(this, argQueue.toArray(new String[0]), sender, channel, message);
		
		// since this is an end point, we won't chain with anything else
		return null;
	}

}
