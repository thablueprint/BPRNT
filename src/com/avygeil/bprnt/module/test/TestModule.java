package com.avygeil.bprnt.module.test;

import com.avygeil.bprnt.module.Module;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class TestModule implements Module {

	@Override
	public void handleMessage(IUser author, IChannel channel, IMessage message) {
		System.out.println("TestModule: message reçu");
		System.out.println("author: " + author.getName());
		System.out.println("channel: " + channel.getName());
		System.out.println("message: " + message.getContent());
	}

}
