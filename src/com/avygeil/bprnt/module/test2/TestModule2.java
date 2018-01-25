package com.avygeil.bprnt.module.test2;

import com.avygeil.bprnt.bot.Bot;
import com.avygeil.bprnt.command.CommandFactory;
import com.avygeil.bprnt.command.CommandStore;
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
	public int getPriority() {
		return 1;
	}
	
	@Override
	public void registerCommands(CommandStore store) {
		store.registerCommand(
			new CommandFactory()
				.setCommandName("baz")
				.setPermission("command.test2.baz")
				.setCallback(this::onBazCommand)
				.build()
		);
	}

	@Override
	public void handleMessage(IUser sender, IChannel channel, IMessage message) {
		System.out.println("message: " + message.getContent());
	}
	
	private void onBazCommand(String[] args, IUser sender, IChannel channel, IMessage message) {
		message.reply("commande Baz reçue");
	}

}
