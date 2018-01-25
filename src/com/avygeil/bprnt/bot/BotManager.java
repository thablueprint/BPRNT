package com.avygeil.bprnt.bot;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.avygeil.bprnt.config.BotConfig;
import com.avygeil.bprnt.config.ConfigStream;
import com.avygeil.bprnt.module.Module;
import com.avygeil.bprnt.util.SubclassPool;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.GuildCreateEvent;
import sx.blah.discord.handle.impl.events.guild.GuildLeaveEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.util.DiscordException;

public class BotManager {
	
	private IDiscordClient client = null;
	private SubclassPool<Module> moduleClassPool = null;
	private ConfigStream configStream = null;
	
	// une instance de bot par id de guilde
	private Map<Long, Bot> botInstances = new HashMap<>();
	
	public void initialize() throws IOException, DiscordException {
		// read the config first
		configStream = new ConfigStream(new File("config.emoji"));
		configStream.read();
		
		final BotConfig config = configStream.getConfig();
		
		if (config.token.isEmpty()) {
			System.out.println("Token field is empty (a new config file was probably created)");
			System.out.print("Please input a token to continue: ");
			
			Scanner scanner = new Scanner(System.in);
			String inputToken = scanner.next();
			scanner.close();
			
			System.out.println("Token will be set to: " + inputToken);
			System.out.println("If you made a mistake, just delete the config file to start over");
			
			config.token = inputToken;
			configStream.save();
		}
		
		// build the module class pool
		moduleClassPool = new SubclassPool<>(Module.class);
		
		/*
		 * TODO
		 * pour l'instant je charge les classes avec un chemin statique
		 * On fera une découverte dynamique des sous classes de Module plus tard
		 */
		try {
			moduleClassPool.registerClass("com.avygeil.bprnt.module.test.TestModule");
		} catch (ClassNotFoundException | ClassCastException e) {
			e.printStackTrace();
		}
		
		System.out.println("Registered " + moduleClassPool.getNumClasses() + " module" + (moduleClassPool.getNumClasses() > 1 ? "s" : ""));
		
		// now login using the token
		ClientBuilder builder = new ClientBuilder();
		builder.withToken(config.token);
		client = builder.login();
	}
	
	public void start() {
		if (client == null) {
			throw new UnsupportedOperationException();
		}
		
		client.getDispatcher().registerListener(this);
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
