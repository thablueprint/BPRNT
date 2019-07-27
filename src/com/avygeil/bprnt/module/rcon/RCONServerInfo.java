package com.avygeil.bprnt.module.rcon;

import com.avygeil.bprnt.util.FormatUtils;
import org.apache.commons.lang3.StringUtils;

import java.net.InetSocketAddress;

public class RCONServerInfo {
	
	public static final int DEFAULT_PORT = 27015;
	
	// just use public fields since they are read only
	public final String name;
	public final InetSocketAddress address;
	public final String password;
	
	public RCONServerInfo(String name, InetSocketAddress address, String password) {
		this.name = name;
		this.address = address;
		this.password = password;
	}
	
	public String toPropertyString() {
		return address.getHostString() + ":" + address.getPort() + "/" + password;
	}

	public static RCONServerInfo fromPropertyString(String propertyKey, String propertyValue) throws IllegalArgumentException {
		// server info is stored as key/value pairs in properties with this format:
		// "propertyKey": "propertyValue"
		// "name":        "hostname:port/password"
		// I don't think RCON passwords can contain a /, so the separator is ok to use
		
		// empty params
		if (propertyKey.isEmpty() || propertyValue.isEmpty()) {
			throw new IllegalArgumentException();
		}
		
		final String name = propertyKey;
		
		// split ip:port/password
		final String[] properties = StringUtils.split(propertyValue, "/", 2);
		
		// malformed property string
		if (properties.length == 0) {
			throw new IllegalArgumentException();
		}
		
		final InetSocketAddress address = FormatUtils.stringToNetAddress(properties[0], DEFAULT_PORT);
		
		final String password;
		
		if (properties.length == 2) {
			password = properties[1];
		} else {
			password = "";
		}
		
		return new RCONServerInfo(name, address, password);
	}
	
}
