package com.avygeil.bprnt.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermissionsConfig {
	
	public Map<Long, List<String>> groupPermissions = new HashMap<>();
	public Map<Long, List<String>> userPermissions = new HashMap<>();

}
