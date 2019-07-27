package com.avygeil.bprnt.command;

import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;

@FunctionalInterface
public interface CommandCallback {
	
	void call(Command cmd, String[] args, Member sender, Message message);
	
}
