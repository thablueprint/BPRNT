package com.avygeil.bprnt.permission;

public class NoPermissionException extends Exception {
	
	private static final long serialVersionUID = 7136951635718673241L;
	
	private final String permissionString;

	public NoPermissionException() {
		this("");
	}

	public NoPermissionException(String permissionString) {
		this.permissionString = "";
	}
	
	public String getPermissionString() {
		return permissionString;
	}

}
