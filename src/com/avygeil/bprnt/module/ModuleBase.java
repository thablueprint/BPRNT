package com.avygeil.bprnt.module;

import java.io.File;

import org.slf4j.Logger;

import com.avygeil.bprnt.bot.Bot;
import com.avygeil.bprnt.config.ModuleConfig;

public abstract class ModuleBase implements Module {
	
	protected final Bot botInstance;
	protected final ModuleConfig config;
	protected final File dataFolder;
	
	protected final Logger LOGGER;
	
	protected ModuleBase(Bot botInstance, ModuleConfig config, File dataFolder, Logger logger) {
		this.botInstance = botInstance;
		this.config = config;
		this.dataFolder = dataFolder;
		this.LOGGER = logger;
	}

}
