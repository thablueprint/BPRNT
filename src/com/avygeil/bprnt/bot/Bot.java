package com.avygeil.bprnt.bot;

import com.avygeil.bprnt.command.CommandStore;
import com.avygeil.bprnt.config.GuildConfig;
import com.avygeil.bprnt.config.ModuleConfig;
import com.avygeil.bprnt.config.PermissionsConfig;
import com.avygeil.bprnt.module.Module;
import com.avygeil.bprnt.module.ModuleBase;
import com.avygeil.bprnt.permission.PermissionsHandler;
import com.avygeil.bprnt.permission.SimplePermissionsHandler;
import com.avygeil.bprnt.util.SubclassPool;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.util.Snowflake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class Bot {
	
	public static final Logger LOGGER;
	
	static {
		LOGGER = LoggerFactory.getLogger(Bot.class);
	}
	
	private final BotManager manager;
	private final Snowflake thisGuildId;
	
	private final GuildConfig config;
	private PermissionsHandler permissionsHandler = null;
	private CommandStore commandStore = null;
	private List<Module> moduleInstances = new ArrayList<>();
	
	public Bot(BotManager manager, Guild thisGuild, GuildConfig config) {
		this.manager = manager;
		this.thisGuildId = thisGuild.getId();
		this.config = config;
	}
	
	public void initialize() {
		// first, create the permissions handler for this module
		
		final PermissionsConfig permissionsConfig = config.permissions;
		
		permissionsHandler = new SimplePermissionsHandler(this, permissionsConfig)
				.setGlobalAdmins(manager.getGlobalAdmins())
				.setLocalAdmins(getLocalAdmins());
		
		// create the command store
		commandStore = new CommandStore(config.commandPrefix, permissionsHandler);
		
		// construct all enabled modules dynamically
		
		SubclassPool<ModuleBase> classPool = manager.getModulePool();
		
		for (Class<? extends ModuleBase> clazz : classPool.getClasses()) {
			final String className = clazz.getName();
			
			// first, check if this module exists in the config, and create it if it doesn't
			if (config.modules.putIfAbsent(className, new ModuleConfig()) == null) {
				manager.saveConfig();
			}
			
			final ModuleConfig moduleConfig = config.modules.get(className); // should never be null
			
			// if the module is disabled, skip it
			if (!moduleConfig.enabled) {
				continue;
			}
			
			// each module instance has a specific data folder based on its class name -> guild instance
			
			File moduleDataFolder = null;
			
			try {
				final Path moduleDataPath = Paths.get("data", clazz.getName(), Long.toString(thisGuildId.asLong()));
				moduleDataFolder = Files.createDirectories(moduleDataPath).toFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			// create a logger instance for this module
			final Logger moduleLogger = LoggerFactory.getLogger(clazz.getSimpleName() + "#" + manager.getClient().getGuildById(thisGuildId).block().getName());
			
			// enforce a specific constructor for the class...
			// if the class doesn't exactly implement the constructor, it should logically revert
			// to the base constructor (which would be forced to being accessible), but all classes
			// should technically implement this specific constructor because the base one is protected
			try {
				Constructor<? extends ModuleBase> constructor = clazz.getConstructor(Bot.class, ModuleConfig.class, File.class, Logger.class);
				constructor.setAccessible(true);
				moduleInstances.add(constructor.newInstance(this, moduleConfig, moduleDataFolder, moduleLogger));
			} catch (NoSuchMethodException | SecurityException | InstantiationException
					| IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		
		// sort the module list based on a module defined priority index
		// higher priority means lower index in the list, which means receiving events earlier
		moduleInstances.sort(new Comparator<Module>() {
			
			@Override
			public int compare(Module arg0, Module arg1) {
				// if p0 > p1, p1 - p0 < 0 => p0 gets indexed earlier
				return Integer.signum(arg1.getPriority() - arg0.getPriority());
			}
			
		});
		
		// load modules
		
		Iterator<Module> it = moduleInstances.iterator();
		
		while (it.hasNext()) {
			final Module currentModule = it.next();
			
			// remove the module from the list if it failed to load
			if (!currentModule.load()) {
				currentModule.unload();
				it.remove();
				continue;
			}
		}
		
		// convenience method to give modules a chance to register their commands early on
		moduleInstances.forEach(module -> module.registerCommands(commandStore));
		
		// post load callback
		moduleInstances.forEach(module -> module.postLoad());
	}
	
	public BotManager getManager() {
		return manager;
	}
	
	public Snowflake getGuildId() {
		return thisGuildId;
	}
	
	public CommandStore getCommandStore() {
		return commandStore;
	}
	
	public List<Long> getLocalAdmins() {
		return config.admins;
	}
	
	public void onMessageReceived(Member sender, Message message) {
		// first, handle commands
		commandStore.handleMessage(sender, message);
		
		// even if the command was handled, we still fire a message event (so modules like loggers still work)
		moduleInstances.forEach(m -> m.handleMessage(sender, message));
	}

}
