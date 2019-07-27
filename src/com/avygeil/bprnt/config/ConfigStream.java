package com.avygeil.bprnt.config;

import com.avygeil.bprnt.util.BaseEmoji;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ConfigStream {
	
	private final Gson gson;
	
	private final File file;
	private BotConfig config = null;
	
	public ConfigStream(File file) {
		gson = new GsonBuilder().setPrettyPrinting().create();
		this.file = file;
	}
	
	public BotConfig getConfig() {
		return config;
	}
	
	public void read() throws IOException {
		if (!file.exists()) {
			// file doesn't exist, create a default config and save it
			config = new BotConfig();
			save();
			return;
		}
		
		config = gson.fromJson(BaseEmoji.decode(FileUtils.readFileToString(file, StandardCharsets.UTF_8)), BotConfig.class);
	}
	
	public void save() throws IOException {
		if (config == null) {
			throw new UnsupportedOperationException();
		}
		
		FileUtils.writeStringToFile(file, BaseEmoji.encode(gson.toJson(config)), StandardCharsets.UTF_8);
	}
	
}
