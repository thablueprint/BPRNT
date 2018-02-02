package com.avygeil.bprnt.command;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import com.avygeil.bprnt.permission.NoPermissionException;
import com.avygeil.bprnt.permission.PermissionsHandler;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public abstract class CommandBase implements Command {
	
	protected final ParentCommand parent;
	protected final String commandName;
	
	protected CommandBase(ParentCommand parent, String commandName) {
		this.parent = parent;
		this.commandName = commandName;
	}
	
	public ParentCommand getParent() {
		return parent;
	}
	
	@Override
	public String getCommandName() {
		return commandName;
	}
	
	@Override
	public void invoke(PermissionsHandler perms, String[] args, IUser sender, IChannel channel, IMessage message)
			throws NoPermissionException, InvalidUsageException {
		
		// we need a modifiable list of args to chain them
		Queue<String> argQueue = new LinkedList<>(Arrays.asList(args));
		
		// permission checking is done here to avoid duplicate code in subclasses
		if (perms != null && !perms.hasPermission(sender, this.getPermission())) {
			throw new NoPermissionException(this.getPermission());
		}
		
		final Command nextCommand;
		
		// actual subclass implementation
		try {
			nextCommand = invoke_Internal(argQueue, sender, channel, message);
		} catch (InvalidUsageException e) {
			// rethrow the exception with the correct usage only if it is empty
			// this allows subclasses to override it if needed
			if (e.getCorrectUsage().isEmpty()) {
				throw new InvalidUsageException(e.getDetails(), this.getUsage());
			} else {
				throw e;
			}
		}
		
		// chain the next command, assuming the previous invokation changed the arg queue
		if (nextCommand != null) {
			nextCommand.invoke(perms, argQueue.toArray(new String[0]), sender, channel, message);
		}
	}
	
	protected abstract Command invoke_Internal(Queue<String> argQueue, IUser sender, IChannel channel, IMessage message)
			throws InvalidUsageException;

}
