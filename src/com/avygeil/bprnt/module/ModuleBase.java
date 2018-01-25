package com.avygeil.bprnt.module;

import com.avygeil.bprnt.bot.Bot;
import com.avygeil.bprnt.config.ModuleConfig;

public abstract class ModuleBase implements Module {
	
	protected final Bot botInstance;
	protected final ModuleConfig config;
	
	protected ModuleBase(Bot botInstance, ModuleConfig config) {
		this.botInstance = botInstance;
		this.config = config;
	}

}
