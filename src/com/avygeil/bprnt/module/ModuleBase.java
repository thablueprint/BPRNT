package com.avygeil.bprnt.module;

import com.avygeil.bprnt.bot.Bot;
import com.avygeil.bprnt.config.ModuleConfig;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import org.slf4j.Logger;

import java.io.File;

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

	// no-op handles by default

	@Override
	public void handleMessage(Member sender, Message message) {
	}

}
