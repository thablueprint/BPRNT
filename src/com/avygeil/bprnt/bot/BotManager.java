package com.avygeil.bprnt.bot;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avygeil.bprnt.config.BotConfig;
import com.avygeil.bprnt.config.ConfigStream;
import com.avygeil.bprnt.config.GuildConfig;
import com.avygeil.bprnt.module.ModuleBase;
import com.avygeil.bprnt.util.SubclassPool;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.GuildCreateEvent;
import sx.blah.discord.handle.impl.events.guild.GuildLeaveEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.util.DiscordException;

public class BotManager {
	
	public static final Logger LOGGER;
	
	static {
		LOGGER = LoggerFactory.getLogger(BotManager.class);
	}
	
	private IDiscordClient client = null;
	
	private ConfigStream configStream = null;
	private SubclassPool<ModuleBase> moduleClassPool = null;
	
	// une instance de bot par id de guilde
	private Map<Long, Bot> botInstances = new HashMap<>();
	
	public void initialize() throws IOException, DiscordException {
		// read the config first
		configStream = new ConfigStream(new File("config.emoji"));
		configStream.read();
		
		final BotConfig config = configStream.getConfig();
		
		if (config.token.isEmpty()) {
			LOGGER.info("Token field is empty (a new config file was probably created)");
			LOGGER.info("Please input a token to continue:");
			
			Scanner scanner = new Scanner(System.in);
			String inputToken = scanner.next();
			scanner.close();
			
			LOGGER.info("Token will be set to: " + inputToken);
			LOGGER.info("If you made a mistake, just delete the config file to start over");
			
			config.token = inputToken;
			saveConfig(); // use non error propagating saving method
		}
		
		// detect modules on the classpath and build the initial modules pool
		moduleClassPool = new SubclassPool<>(ModuleBase.class);
		
		final Reflections reflections = new Reflections("com.avygeil.bprnt.module");
		Set<Class<? extends ModuleBase>> detectedModuleClasses = reflections.getSubTypesOf(ModuleBase.class);
		
		for (Class<? extends ModuleBase> moduleClass : detectedModuleClasses) {
			LOGGER.info("Discovered module: " + moduleClass.getName());
			moduleClassPool.registerClass(moduleClass);
		}
		
		LOGGER.info("Registered " + moduleClassPool.getNumClasses() + " modules");
		
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
	
	public List<Long> getGlobalAdmins() {
		return configStream.getConfig().globalAdmins;
	}
	
	public void saveConfig() {
		try {
			configStream.save();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public SubclassPool<ModuleBase> getModulePool() {
		return moduleClassPool;
	}
	
	@EventSubscriber
	public void onGuildCreateEvent(GuildCreateEvent event) {
		final long guildId = event.getGuild().getLongID();
		
		if (botInstances.containsKey(guildId)) {
			return; // this can happen from a rejoin etc, just leave it as it is
		}
		
		// we just connected to a guild, whether at startup or because someone just added us
		
		// if we don't have a config for this guild yet, this is the first connection, so create it		
		if (configStream.getConfig().guilds.putIfAbsent(guildId, new GuildConfig()) == null) {
			LOGGER.info("Created new guild config for new guild: " + event.getGuild().getName());
			saveConfig();
		}
		
		// create the new instance
		final GuildConfig instanceConfig = configStream.getConfig().guilds.get(guildId); // should never be null
		final Bot newBotInstance = new Bot(this, event.getGuild(), instanceConfig);
		newBotInstance.initialize();
		botInstances.put(guildId, newBotInstance);
	}
	
	@EventSubscriber
	public void onGuildLeaveEvent(GuildLeaveEvent event) {
		botInstances.remove(event.getGuild().getLongID());
	}
	
	@EventSubscriber
	public void onMessageReceivedEvent(MessageReceivedEvent event) {
		Bot botInstance = botInstances.get(event.getGuild().getLongID());
		
		if (botInstance == null) {
			LOGGER.warn("Received message from an unmapped guild!");
			return;
		}
		
		botInstance.onMessageReceived(event.getAuthor(), event.getChannel(), event.getMessage());
	}

}
