package com.avygeil.bprnt.command;

import com.avygeil.bprnt.permission.NoPermissionException;
import com.avygeil.bprnt.permission.PermissionsHandler;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;

public interface Command {
	
	public String getCommandName();
	public String getPermission();
	public String getUsage();
	
	public void invoke(PermissionsHandler perms, String[] args, Member sender, Message message)
			throws NoPermissionException, InvalidUsageException;
	
}
