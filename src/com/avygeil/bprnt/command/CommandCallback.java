package com.avygeil.bprnt.command;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

@FunctionalInterface
public interface CommandCallback {
	
	void call(Command cmd, String[] args, IUser sender, IChannel channel, IMessage message);
	
}
