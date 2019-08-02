package com.avygeil.bprnt.util;

import com.avygeil.bprnt.command.ParentCommand;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringTokenizer;
import org.apache.commons.text.matcher.StringMatcherFactory;

import java.net.InetSocketAddress;
import java.util.Stack;

public final class FormatUtils {
	
	private FormatUtils() {
	}
	
	private static final StringTokenizer tokenizer = new StringTokenizer();

	static {
		tokenizer.setDelimiterMatcher(StringMatcherFactory.INSTANCE.charSetMatcher(' ', '\t', '\n', '\r', '\f'));
		tokenizer.setQuoteMatcher(StringMatcherFactory.INSTANCE.doubleQuoteMatcher());
		tokenizer.setEmptyTokenAsNull(false);
		tokenizer.setIgnoreEmptyTokens(true);
	}
	
	public static String[] tokenize(String string) {
		return tokenizer.reset(string).getTokenArray();
	}
	
	public static String getChainedCommandNamesString(ParentCommand lastLevel) {
		Stack<String> commandNames = new Stack<>();
		
		ParentCommand currentLevel = lastLevel;
		while (currentLevel != null) {
			commandNames.add(currentLevel.getCommandName());
			currentLevel = currentLevel.getParent();
		}
		
		return StringUtils.join(commandNames, ' ');
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
