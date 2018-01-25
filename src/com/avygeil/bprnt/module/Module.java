package com.avygeil.bprnt.module;

import com.avygeil.bprnt.command.CommandStore;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public interface Module {
	
	int getPriority();
	
	void registerCommands(CommandStore store);
	
	void handleMessage(IUser sender, IChannel channel, IMessage message);

}
