package com.avygeil.bprnt.module.test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;

import com.avygeil.bprnt.bot.Bot;
import com.avygeil.bprnt.command.CommandFactory;
import com.avygeil.bprnt.command.CommandStore;
import com.avygeil.bprnt.config.ModuleConfig;
import com.avygeil.bprnt.module.ModuleBase;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class TestModule extends ModuleBase {
	
	private final File testFile;

	public TestModule(Bot botInstance, ModuleConfig config, File dataFolder) {
		super(botInstance, config, dataFolder);
		testFile = new File(dataFolder, "testlog.txt");
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
		try {
			FileUtils.writeStringToFile(testFile, message.getAuthor().getName() + ": " + message.getContent() + "\n", StandardCharsets.UTF_8, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void onBarCommand(String[] args, IUser sender, IChannel channel, IMessage message) {
		message.reply("commande Bar reçue");
	}

}
