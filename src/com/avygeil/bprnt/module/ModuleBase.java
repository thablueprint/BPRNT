package com.avygeil.bprnt.module;

import java.io.File;

import com.avygeil.bprnt.bot.Bot;
import com.avygeil.bprnt.config.ModuleConfig;

public abstract class ModuleBase implements Module {
	
	protected final Bot botInstance;
	protected final ModuleConfig config;
	protected final File dataFolder;
	
	protected ModuleBase(Bot botInstance, ModuleConfig config, File dataFolder) {
		this.botInstance = botInstance;
		this.config = config;
		this.dataFolder = dataFolder;
	}

}
