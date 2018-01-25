package com.avygeil.bprnt.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BotConfig {
	
	public String token = "";
	public final List<Long> globalAdmins = new ArrayList<>();
	public final Map<Long, GuildConfig> guilds = new HashMap<>();
	
}
