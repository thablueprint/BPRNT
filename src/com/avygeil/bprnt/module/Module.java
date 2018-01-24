package com.avygeil.bprnt.module;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public interface Module {
	
	void handleMessage(IUser author, IChannel channel, IMessage message);

}
