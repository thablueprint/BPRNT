package com.avygeil.bprnt.module;

import com.avygeil.bprnt.command.CommandStore;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;

public interface Module {
	
	int getPriority();
	
	boolean load();
	void postLoad();
	void unload();
	
	void registerCommands(CommandStore store);

	void handleMessage(Member sender, Message message);

}
