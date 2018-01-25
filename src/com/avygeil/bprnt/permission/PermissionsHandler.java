package com.avygeil.bprnt.permission;

import sx.blah.discord.handle.obj.IUser;

public interface PermissionsHandler {
	
	public boolean hasPermission(IUser user, String permissionString);
	
}
