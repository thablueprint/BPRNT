package com.avygeil.bprnt.bot;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import com.avygeil.bprnt.config.GuildConfig;
import com.avygeil.bprnt.config.ModuleConfig;
import com.avygeil.bprnt.module.Module;
import com.avygeil.bprnt.module.ModuleBase;
import com.avygeil.bprnt.util.SubclassPool;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class Bot {
	
	private final BotManager manager;
	private final GuildConfig config;
	
	private List<Module> moduleInstances = new ArrayList<>();
	
	public Bot(BotManager manager, GuildConfig config) {
		this.manager = manager;
		this.config = config;
	}
	
	public void loadModules() {
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
			
			// enforce a specific constructor for the class...
			// if the class doesn't exactly implement the constructor, it should logically revert
			// to the base constructor (which would be forced to being accessible), but all classes
			// should technically implement this specific constructor because the base one is protected
			try {
				Constructor<? extends ModuleBase> constructor = clazz.getConstructor(Bot.class, ModuleConfig.class);
				constructor.setAccessible(true);
				moduleInstances.add(constructor.newInstance(this, moduleConfig));
			} catch (NoSuchMethodException | SecurityException | InstantiationException
					| IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void onMessageReceived(IUser author, IChannel channel, IMessage message) {
		for (Module module : moduleInstances) {
			module.handleMessage(author, channel, message);
		}
	}

}
