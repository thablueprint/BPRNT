package com.avygeil.bprnt.command;

import com.avygeil.bprnt.permission.NoPermissionException;
import com.avygeil.bprnt.permission.PermissionsHandler;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public interface Command {
	
	public String getCommandName();
	public String getPermission();
	public String getUsage();
	
	public void invoke(PermissionsHandler perms, String[] args, IUser sender, IChannel channel, IMessage message)
			throws NoPermissionException, InvalidUsageException;
	
}
