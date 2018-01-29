package com.avygeil.bprnt.permission;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.avygeil.bprnt.bot.Bot;
import com.avygeil.bprnt.config.PermissionsConfig;

import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;

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
	public boolean hasPermission(IUser user, String permissionString) {
		// guild admins bypass all permission checks by default
		if (user.getPermissionsForGuild(bot.getGuild()).contains(Permissions.ADMINISTRATOR)) {
			return true;
		}
		
		final long userId = user.getLongID();
		
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
		for (IRole role : user.getRolesForGuild(bot.getGuild())) {
			final List<String> groupPermissions = config.groupPermissions.getOrDefault(role.getLongID(), Collections.emptyList());
			
			if (groupPermissions.contains(permissionString)) {
				return true;
			}
		}
			
		return false;
	}

}
