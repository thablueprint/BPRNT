package com.avygeil.bprnt.permission;

import discord4j.core.object.entity.Member;

public interface PermissionsHandler {
	
	public boolean hasPermission(Member user, String permissionString);
	
}
