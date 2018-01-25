package com.avygeil.bprnt.module.test2;

import com.avygeil.bprnt.bot.Bot;
import com.avygeil.bprnt.config.ModuleConfig;
import com.avygeil.bprnt.module.ModuleBase;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class TestModule2 extends ModuleBase {

	public TestModule2(Bot botInstance, ModuleConfig config) {
		super(botInstance, config);
	}

	@Override
	public void handleMessage(IUser author, IChannel channel, IMessage message) {
		System.out.println("message: " + message.getContent());
	}

}
