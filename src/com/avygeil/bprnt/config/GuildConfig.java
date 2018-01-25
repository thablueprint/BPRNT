package com.avygeil.bprnt.config;

import java.util.ArrayList;
import java.util.List;

public class GuildConfig {
	
	public final List<Long> admins = new ArrayList<>();
	public final List<ModuleConfig> modules = new ArrayList<>();
	public final PermissionsConfig permissions = new PermissionsConfig();

}
