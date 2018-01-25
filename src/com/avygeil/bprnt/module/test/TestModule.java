package com.avygeil.bprnt.module.test;

import com.avygeil.bprnt.bot.Bot;
import com.avygeil.bprnt.command.CommandFactory;
import com.avygeil.bprnt.command.CommandStore;
import com.avygeil.bprnt.config.ModuleConfig;
import com.avygeil.bprnt.module.ModuleBase;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class TestModule extends ModuleBase {

	public TestModule(Bot botInstance, ModuleConfig config) {
		super(botInstance, config);
	}
	
	@Override
	public int getPriority() {
		return 2;
	}
	
	@Override
	public void registerCommands(CommandStore store) {
		store.registerCommand(
			new CommandFactory()
				.setCommandName("bar")
				.setPermission("command.test.bar")
				.setCallback(this::onBarCommand)
				.build()
		);
	}

	@Override
	public void handleMessage(IUser sender, IChannel channel, IMessage message) {
		System.out.println("author: " + sender.getName());
	}
	
	private void onBarCommand(String[] args, IUser sender, IChannel channel, IMessage message) {
		message.reply("commande Bar reçue");
	}

}
