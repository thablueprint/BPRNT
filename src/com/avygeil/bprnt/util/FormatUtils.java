package com.avygeil.bprnt.util;

import java.net.InetSocketAddress;

import org.apache.commons.lang3.StringUtils;

public final class FormatUtils {
	
	private FormatUtils() {
	}
	
	public static InetSocketAddress stringToNetAddress(String string, int defaultPort) throws IllegalArgumentException {
		if (string.isEmpty()) {
			throw new IllegalArgumentException();
		}
		
		String[] contents = StringUtils.split(string, ":", 2);
		
		if (contents.length == 0) {
			throw new IllegalArgumentException(); // malformed string
		}
		
		final String hostname = contents[0];
		final int port;
		
		if (contents.length == 2) {
			try {
				port = Integer.parseInt(contents[1]);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException(e);
			}
		} else {
			port = defaultPort;
		}
		
		return new InetSocketAddress(hostname, port);
	}

}
