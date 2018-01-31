package com.avygeil.bprnt.module.rcon;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import com.avygeil.bprnt.bot.Bot;
import com.avygeil.bprnt.command.CommandFactory;
import com.avygeil.bprnt.command.CommandStore;
import com.avygeil.bprnt.config.ModuleConfig;
import com.avygeil.bprnt.module.Module;
import com.avygeil.bprnt.module.ModuleBase;
import com.avygeil.bprnt.util.FormatUtils;
import com.ibasco.agql.protocols.valve.source.query.SourceRconAuthStatus;
import com.ibasco.agql.protocols.valve.source.query.client.SourceRconClient;
import com.ibasco.agql.protocols.valve.source.query.exceptions.RconNotYetAuthException;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class RCONModule extends ModuleBase {
	
	final Map<String, RCONServerInfo> infoMap = new HashMap<>();
	SourceRconClient rconClient = null; // don't immediately create the object because it initializes netty

	public RCONModule(Bot botInstance, ModuleConfig config, File dataFolder, Logger logger) {
		super(botInstance, config, dataFolder, logger);
	}

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	public boolean load() {
		// TODO Auto-generated method stub
		return true;
	}
	
	@Override
	public void postLoad() {
		// parse config properties to build the info map

		for (Map.Entry<String, String> property : config.properties.entrySet()) {
			final RCONServerInfo serverInfo;

			try {
				serverInfo = RCONServerInfo.fromPropertyString(property.getKey(), property.getValue());
			} catch (IllegalArgumentException e) {
				LOGGER.error("Malformed RCON server info property: \"" + property.getKey() + "\"=\""
						+ property.getValue() + "\"");
				continue;
			}

			infoMap.put(serverInfo.name.toLowerCase(), serverInfo); // case insensitive
			LOGGER.info("Loaded RCON server info for \"" + serverInfo.name + "\"");
		}

		rconClient = new SourceRconClient();
	}

	@Override
	public void unload() {
		// TODO Auto-generated method stub
	}

	@Override
	public void registerCommands(CommandStore store) {
		store.registerCommand(new CommandFactory()
			.setCommandName("bindRCONServer")
			.setPermission("rcon.command.bindrconserver")
			.setCallback(this::bindRCONServerCommand)
			.build()
		);
		
		store.registerCommand(new CommandFactory()
			.setCommandName("unbindRCONServer")
			.setPermission("rcon.command.unbindrconserver")
			.setCallback(this::unbindRCONServerCommand)
			.build()
		);
		
		store.registerCommand(new CommandFactory()
			.setCommandName("boundRCONServers")
			.setPermission("rcon.command.boundrconservers")
			.setCallback(this::boundRCONServersCommand)
			.build()
		);
		
		store.registerCommand(new CommandFactory()
			.setCommandName("rcon")
			.setPermission("rcon.command.rcon")
			.setCallback(this::rconCommand)
			.build()
		);
	}
	
	public void bindRCONServerCommand(String command, String[] args, IUser sender, IChannel channel, IMessage message) {
		if (args.length < 2) {
			message.reply("Usage: `!bindRCONServer <name> <ip[:port]> [rconpassword]`");
			return;
		}
		
		// parse arguments
		
		final String name = args[0];
		final InetSocketAddress address;
		
		try {
			address = FormatUtils.stringToNetAddress(args[1], RCONServerInfo.DEFAULT_PORT);
		} catch (IllegalArgumentException e) {
			message.reply("Invalid server address (format: `<ip[:port]>`)");
			return;
		}
		
		final String password = args.length == 2 ? "" : args[2];
		
		// add/replace the server info to the config
		
		final RCONServerInfo serverInfo = new RCONServerInfo(name, address, password);
		
		infoMap.put(serverInfo.name.toLowerCase(), serverInfo); // case insensitive
		config.properties.put(serverInfo.name, serverInfo.toPropertyString());
		botInstance.getManager().saveConfig();
		
		message.reply("Bound RCON server \"" + serverInfo.name + "\" successfully");
	}
	
	public void unbindRCONServerCommand(String command, String[] args, IUser sender, IChannel channel, IMessage message) {
		if (infoMap.isEmpty()) {
			message.reply("No servers are bound yet");
			return;
		}
		
		if (args.length < 1) {
			message.reply("Usage: !rcon <server>");
			return;
		}
		
		final RCONServerInfo serverInfo = infoMap.get(args[0].toLowerCase());
		
		if (serverInfo == null) {
			message.reply("No entry exists for server \"" + args[0] + "\"");
			return;
		}
		
		infoMap.remove(serverInfo.name.toLowerCase());
		config.properties.remove(serverInfo.name);
		botInstance.getManager().saveConfig();
		
		message.reply("Unbound server \"" + serverInfo.name + "\" successfully");
	}
	
	public void boundRCONServersCommand(String command, String[] args, IUser sender, IChannel channel, IMessage message) {
		if (infoMap.isEmpty()) {
			message.reply("No servers are bound yet");
		} else {
			message.reply("Bound servers: " + StringUtils.join(infoMap.keySet(), ", "));
		}
	}
	
	public void rconCommand(String command, String[] args, IUser sender, IChannel channel, IMessage message) {					
		if (infoMap.isEmpty()) {
			message.reply("No RCON server configured yet");
			return;
		}
		
		if (args.length < 2) {
			message.reply("Usage: !rcon <server> <command>");
			return;
		}
		
		final RCONServerInfo serverInfo = infoMap.get(args[0].toLowerCase());
		
		if (serverInfo == null) {
			message.reply("No entry exists for server \"" + args[0] + "\"");
			return;
		}
		
		if (!rconClient.isAuthenticated(serverInfo.address)) {
			SourceRconAuthStatus authStatus = rconClient.authenticate(serverInfo.address, serverInfo.password).join();
			
			if (!authStatus.isAuthenticated()) {
				String reason = authStatus.getReason();
				
				if (reason.isEmpty()) {
					reason = "RCON password might be invalid"; // default reason
				}
				
				message.reply("Failed to authenticate! " + reason);
				return;
			}
		}

		final String rconCommand = StringUtils.join(args, ' ', 1, args.length);
		
		try {
			String result = rconClient.execute(serverInfo.address, rconCommand).join();
			message.reply(result);
		} catch (RconNotYetAuthException e) {
			// this shouldn't happen unless our auth immediately times out somehow...
			e.printStackTrace();
			message.reply("Not yet authenticated to RCON server");
			return;
		}
	}

	@Override
	public void handleModuleLoad(Module module) {
		// TODO Auto-generated method stub
	}

	@Override
	public void handleModuleUnload(Module module) {
		// TODO Auto-generated method stub
	}

	@Override
	public void handleMessage(IUser sender, IChannel channel, IMessage message) {
		// TODO Auto-generated method stub
	}

}
