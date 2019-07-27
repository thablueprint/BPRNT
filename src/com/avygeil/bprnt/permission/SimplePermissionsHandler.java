package com.avygeil.bprnt.permission;

import com.avygeil.bprnt.bot.Bot;
import com.avygeil.bprnt.config.PermissionsConfig;
import discord4j.core.object.entity.Member;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.Snowflake;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimplePermissionsHandler implements PermissionsHandler {
	
	private final Bot bot;
	private final PermissionsConfig config;
	private List<Long> globalAdmins = new ArrayList<>();
	private List<Long> localAdmins = new ArrayList<>();
	
	public SimplePermissionsHandler(Bot bot, PermissionsConfig config) {
		this.bot = bot;
		this.config = config;
	}
	
	public SimplePermissionsHandler setGlobalAdmins(List<Long> globalAdmins) {
		this.globalAdmins = globalAdmins;
		return this;
	}
	
	public SimplePermissionsHandler setLocalAdmins(List<Long> localAdmins) {
		this.localAdmins = localAdmins;
		return this;
	}

	@Override
	public boolean hasPermission(Member user, String permissionString) {
		// everyone has the empty permission
		if (permissionString.isEmpty()) {
			return true;
		}

		boolean isAdmin = user.getBasePermissions().map(s -> s.contains(Permission.ADMINISTRATOR)).block();
		
		// guild admins bypass all permission checks by default
		if (isAdmin) {
			return true;
		}
		
		final long userId = user.getId().asLong();
		
		// global bot and local bot admins also bypass all permission checks
		if (globalAdmins.contains(userId) || localAdmins.contains(userId)) {
			return true;
		}
		
		// check if the user specifically has the permission
		final List<String> userPermissions = config.userPermissions.getOrDefault(userId, Collections.emptyList());
		
		if (userPermissions.contains(permissionString)) {
			return true;
		}
		
		// check all user groups for their permissions
		for (Snowflake roleId : user.getRoleIds()) {
			final List<String> groupPermissions = config.groupPermissions.getOrDefault(roleId.asLong(), Collections.emptyList());
			
			if (groupPermissions.contains(permissionString)) {
				return true;
			}
		}
			
		return false;
	}

}
