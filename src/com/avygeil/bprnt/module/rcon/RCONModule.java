package com.avygeil.bprnt.module.rcon;

import com.avygeil.bprnt.bot.Bot;
import com.avygeil.bprnt.command.Command;
import com.avygeil.bprnt.command.CommandFactory;
import com.avygeil.bprnt.command.CommandStore;
import com.avygeil.bprnt.config.ModuleConfig;
import com.avygeil.bprnt.module.ModuleBase;
import com.avygeil.bprnt.module.ModulePriority;
import com.avygeil.bprnt.util.DiscordUtils;
import com.avygeil.bprnt.util.FormatUtils;
import com.ibasco.agql.protocols.valve.source.query.SourceRconAuthStatus;
import com.ibasco.agql.protocols.valve.source.query.client.SourceRconClient;
import com.ibasco.agql.protocols.valve.source.query.exceptions.RconNotYetAuthException;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class RCONModule extends ModuleBase {
	
	final Map<String, RCONServerInfo> infoMap = new HashMap<>();
	SourceRconClient rconClient = null; // don't immediately create the object because it initializes netty

	public RCONModule(Bot botInstance, ModuleConfig config, File dataFolder, Logger logger) {
		super(botInstance, config, dataFolder, logger);
	}

	@Override
	public int getPriority() {
		return ModulePriority.NORMAL;
	}

	@Override
	public boolean load() {
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
	}

	@Override
	public void registerCommands(CommandStore store) {
		store.registerCommands(new CommandFactory()
			.newParentCommand("rconConfig")
				.newSubcommand("bind")
					.withPermission("command.rconconfig.bind")
					.setCallback(this::bindRCONServerCommand)
					.done()
				.newSubcommand("unbind")
					.withPermission("command.rconconfig.unbind")
					.setCallback(this::unbindRCONServerCommand)
					.done()
				.newSubcommand("list")
					.withPermission("command.rconconfig.list")
					.setCallback(this::listRCONServersCommand)
					.done()
				.done()
			.newCommand("rcon")
				.withPermission("command.rcon")
				.setCallback(this::rconCommand)
				.done()
		);
	}
	
	public void bindRCONServerCommand(Command cmd, String[] args, Member sender, Message message) {
		if (args.length < 2) {
			DiscordUtils.replyToMessage(message, "Usage: `!rconConfig bind <name> <ip[:port]> [rconpassword]`");
			return;
		}
		
		// parse arguments
		
		final String name = args[0];
		final InetSocketAddress address;
		
		try {
			address = FormatUtils.stringToNetAddress(args[1], RCONServerInfo.DEFAULT_PORT);
		} catch (IllegalArgumentException e) {
			DiscordUtils.replyToMessage(message, "Invalid server address (format: `<ip[:port]>`)");
			return;
		}
		
		final String password = args.length == 2 ? "" : args[2];
		
		// add/replace the server info to the config
		
		final RCONServerInfo serverInfo = new RCONServerInfo(name, address, password);
		
		infoMap.put(serverInfo.name.toLowerCase(), serverInfo); // case insensitive
		config.properties.put(serverInfo.name, serverInfo.toPropertyString());
		botInstance.getManager().saveConfig();

		DiscordUtils.replyToMessage(message, "Bound RCON server \"" + serverInfo.name + "\" successfully");
	}
	
	public void unbindRCONServerCommand(Command cmd, String[] args, Member sender, Message message) {
		if (infoMap.isEmpty()) {
			DiscordUtils.replyToMessage(message, "No servers are bound yet");
			return;
		}
		
		if (args.length < 1) {
			DiscordUtils.replyToMessage(message, "Usage: !rconConfig unbind <server>");
			return;
		}
		
		final RCONServerInfo serverInfo = infoMap.get(args[0].toLowerCase());
		
		if (serverInfo == null) {
			DiscordUtils.replyToMessage(message, "No entry exists for server \"" + args[0] + "\"");
			return;
		}
		
		infoMap.remove(serverInfo.name.toLowerCase());
		config.properties.remove(serverInfo.name);
		botInstance.getManager().saveConfig();

		DiscordUtils.replyToMessage(message, "Unbound server \"" + serverInfo.name + "\" successfully");
	}
	
	public void listRCONServersCommand(Command cmd, String[] args, Member sender, Message message) {
		if (infoMap.isEmpty()) {
			DiscordUtils.replyToMessage(message, "No servers are bound yet");
		} else {
			DiscordUtils.replyToMessage(message, "Bound servers: " + StringUtils.join(infoMap.keySet(), ", "));
		}
	}
	
	public void rconCommand(Command cmd, String[] args, Member sender, Message message) {
		if (infoMap.isEmpty()) {
			DiscordUtils.replyToMessage(message, "No RCON server configured yet");
			return;
		}
		
		if (args.length < 2) {
			DiscordUtils.replyToMessage(message, "Usage: !rcon <server> <command>");
			return;
		}
		
		final RCONServerInfo serverInfo = infoMap.get(args[0].toLowerCase());
		
		if (serverInfo == null) {
			DiscordUtils.replyToMessage(message, "No entry exists for server \"" + args[0] + "\"");
			return;
		}
		
		SourceRconAuthStatus authStatus = rconClient.authenticate(serverInfo.address, serverInfo.password).join();
		
		if (!authStatus.isAuthenticated()) {
			String reason = authStatus.getReason();
			
			if (reason.isEmpty()) {
				reason = "RCON password might be invalid"; // default reason
			}

			DiscordUtils.replyToMessage(message, "Failed to authenticate! " + reason);
			return;
		}

		final String rconCommand = StringUtils.join(args, ' ', 1, args.length);
		
		try {
			String result = rconClient.execute(serverInfo.address, rconCommand).join();
			DiscordUtils.replyToMessage(message, result);
		} catch (RconNotYetAuthException e) {
			// this shouldn't happen unless our auth immediately times out somehow...
			e.printStackTrace();
			DiscordUtils.replyToMessage(message, "Not yet authenticated to RCON server");
			return;
		}
	}

}
