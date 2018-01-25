package com.avygeil.bprnt.bot;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.avygeil.bprnt.config.ConfigStream;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.GuildCreateEvent;
import sx.blah.discord.handle.impl.events.guild.GuildLeaveEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.util.DiscordException;

public class BotManager {

	public static void main(String[] args) {
		if (args.length < 1) {
			throw new IllegalArgumentException();
		}
		
		IDiscordClient client = null;
		
		try {
			ClientBuilder builder = new ClientBuilder();
			builder.withToken(args[0]);
			client = builder.login();
		} catch (DiscordException e) {
			e.printStackTrace();
		}
		
		if (client != null) {
			new BotManager(client).Run();
		}
	}
	
	private IDiscordClient client;
	
	// une instance de bot par id de guilde
	private Map<Long, Bot> botInstances = new HashMap<>();
	
	public BotManager(IDiscordClient client) {
		this.client = client;
	}
	
	public void Run() {
		client.getDispatcher().registerListener(this);
		ConfigStream testConfig = new ConfigStream(new File("config.emoji"));
		
		try {
			testConfig.read();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	@EventSubscriber
	public void onGuildCreateEvent(GuildCreateEvent event) {
		System.out.println("Initializing Bot for guild: " + event.getGuild().getName());
		botInstances.putIfAbsent(event.getGuild().getLongID(), new Bot());
	}
	
	@EventSubscriber
	public void onGuildLeaveEvent(GuildLeaveEvent event) {
		System.out.println("Removing Bot from guild: " + event.getGuild().getName());
		botInstances.remove(event.getGuild().getLongID());
	}
	
	@EventSubscriber
	public void onMessageReceivedEvent(MessageReceivedEvent event) {
		Bot botInstance = botInstances.get(event.getGuild().getLongID());
		
		if (botInstance == null) {
			return;
		}
		
		botInstance.onMessageReceived(event.getAuthor(), event.getChannel(), event.getMessage());
	}

}
