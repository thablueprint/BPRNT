package com.avygeil.bprnt.bot;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avygeil.bprnt.command.CommandStore;
import com.avygeil.bprnt.config.GuildConfig;
import com.avygeil.bprnt.config.ModuleConfig;
import com.avygeil.bprnt.config.PermissionsConfig;
import com.avygeil.bprnt.module.Module;
import com.avygeil.bprnt.module.ModuleBase;
import com.avygeil.bprnt.permission.PermissionsHandler;
import com.avygeil.bprnt.permission.SimplePermissionsHandler;
import com.avygeil.bprnt.util.SubclassPool;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class Bot {
	
	public static final Logger LOGGER;
	
	static {
		LOGGER = LoggerFactory.getLogger(Bot.class);
	}
	
	private final BotManager manager;
	private final IGuild thisGuild;
	
	private final GuildConfig config;
	private PermissionsHandler permissionsHandler = null;
	private CommandStore commandStore = null;
	private List<Module> moduleInstances = new ArrayList<>();
	
	public Bot(BotManager manager, IGuild thisGuild, GuildConfig config) {
		this.manager = manager;
		this.thisGuild = thisGuild;
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
				final Path moduleDataPath = Paths.get("data", clazz.getName(), Long.toString(thisGuild.getLongID()));
				moduleDataFolder = Files.createDirectories(moduleDataPath).toFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			// enforce a specific constructor for the class...
			// if the class doesn't exactly implement the constructor, it should logically revert
			// to the base constructor (which would be forced to being accessible), but all classes
			// should technically implement this specific constructor because the base one is protected
			try {
				Constructor<? extends ModuleBase> constructor = clazz.getConstructor(Bot.class, ModuleConfig.class, File.class);
				constructor.setAccessible(true);
				moduleInstances.add(constructor.newInstance(this, moduleConfig, moduleDataFolder));
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
		
		// convenience method to give modules a chance to register their commands early on
		moduleInstances.forEach(m -> m.registerCommands(commandStore));
	}
	
	public IGuild getGuild() {
		return thisGuild;
	}
	
	public List<Long> getLocalAdmins() {
		return config.admins;
	}
	
	public void onMessageReceived(IUser sender, IChannel channel, IMessage message) {
		// first, handle commands
		commandStore.handleCommand(sender, channel, message);
		
		// even if the command was handled, we still fire a message event (so modules like loggers still work)
		for (Module module : moduleInstances) {
			module.handleMessage(sender, channel, message);
		}
	}

}
