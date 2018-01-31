package com.avygeil.bprnt.module;

import java.time.LocalDateTime;

import com.avygeil.bprnt.command.CommandStore;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IReaction;
import sx.blah.discord.handle.obj.IUser;

public interface Module {
	
	int getPriority();
	
	boolean load();
	void postLoad();
	void unload();
	
	void registerCommands(CommandStore store);
	
	void handleModuleLoad(Module module);
	void handleModuleUnload(Module module);
	void handleMessage(IUser sender, IChannel channel, IMessage message);
	void handleReactionAdd(IUser sender, IChannel channel, IMessage message, IReaction reaction);
	void handleUserJoin(IUser user, LocalDateTime joinTime);
	void handleUserLeave(IUser user);

}
