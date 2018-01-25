package com.avygeil.bprnt.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuildConfig {
	
	public final List<Long> admins = new ArrayList<>();
	public final Map<String, ModuleConfig> modules = new HashMap<>();
	public final PermissionsConfig permissions = new PermissionsConfig();

}
