package com.avygeil.bprnt.permission;

import java.util.ArrayList;
import java.util.List;

import com.avygeil.bprnt.config.PermissionsConfig;

import sx.blah.discord.handle.obj.IUser;

public class SimplePermissionsHandler implements PermissionsHandler {
	
	private final PermissionsConfig config;
	private List<Long> globalAdmins = new ArrayList<>();
	private List<Long> localAdmins = new ArrayList<>();
	
	public SimplePermissionsHandler(PermissionsConfig config) {
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
		return true;
	}

}
