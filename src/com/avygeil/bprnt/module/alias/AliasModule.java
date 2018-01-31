package com.avygeil.bprnt.module.alias;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import com.avygeil.bprnt.bot.Bot;
import com.avygeil.bprnt.command.CommandFactory;
import com.avygeil.bprnt.command.CommandStore;
import com.avygeil.bprnt.config.ModuleConfig;
import com.avygeil.bprnt.module.Module;
import com.avygeil.bprnt.module.ModuleBase;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class AliasModule extends ModuleBase {
	
	final CommandStore commandStore;
	
	// we have to keep a list of actively registered aliases separated from
	// the config which may contain invalid, unregistered aliases, in order
	// to dynamically redefine and remove aliases without accidentally
	// unregistering legit commands from other modules
	final Set<String> activeAliases = new HashSet<>();

	public AliasModule(Bot botInstance, ModuleConfig config, File dataFolder, Logger logger) {
		super(botInstance, config, dataFolder, logger);
		commandStore = botInstance.getCommandStore();
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
		for (Map.Entry<String, String> property : config.properties.entrySet()) {
			final String alias = property.getKey();
			final String aliasedCmd = property.getValue();
			
			if (!registerAlias(alias, aliasedCmd, false)) {
				LOGGER.error("Attempted to register command \"" + alias + "\" as an alias, but it already exists as a normal command!");
				continue;
			}
			
			LOGGER.info("Registered alias: " + alias);
		}
	}

	@Override
	public void unload() {
		// TODO Auto-generated method stub
	}

	@Override
	public void registerCommands(CommandStore store) {
		store.registerCommand(new CommandFactory()
			.setCommandName("setAlias")
			.setPermission("alias.command.setalias")
			.setCallback(this::setAliasCommand)
			.build()
		);
		
		store.registerCommand(new CommandFactory()
			.setCommandName("removeAlias")
			.setPermission("alias.command.removealias")
			.setCallback(this::removeAliasCommand)
			.build()
		);
		
		store.registerCommand(new CommandFactory()
			.setCommandName("listAliases")
			.setPermission("alias.command.listaliases")
			.setCallback(this::listAliasesCommand)
			.build()
		);
	}
	
	public void setAliasCommand(String command, String[] args, IUser sender, IChannel channel, IMessage message) {
		if (args.length < 2) {
			message.reply("Usage: `!setAlias <alias> <aliasedCmd ...>`");
			return;
		}
		
		final String alias = args[0];
		final String aliasedCmd = StringUtils.join(args, ' ', 1, args.length);
				
		if (!registerAlias(alias, aliasedCmd, true)) {
			message.reply("This command already exists as a non-alias command!");
			return;
		}
		
		message.reply("Alias \"" + alias + "\" set successfully");
	}
	
	public void removeAliasCommand(String command, String[] args, IUser sender, IChannel channel, IMessage message) {
		if (args.length < 1) {
			message.reply("Usage: `!removeAlias <alias>`");
			return;
		}
		
		if (unregisterAlias(args[0])) {
			message.reply("Removed alias \"" + args[0] + "\" successfully");
		} else {
			message.reply("There is no alias with this name!");
		}
	}
	
	public void listAliasesCommand(String command, String[] args, IUser sender, IChannel channel, IMessage message) {
		if (config.properties.isEmpty()) {
			message.reply("No alias set yet");
			return;
		}
		
		final List<String> registeredAliases = new ArrayList<>();
		final List<String> invalidAliases = new ArrayList<>();
		
		for (Map.Entry<String, String> property : config.properties.entrySet()) {
			final String alias = property.getKey();
			final String aliasedCmd = property.getValue();
			
			final List<String> refList = activeAliases.contains(alias.toLowerCase()) ? registeredAliases : invalidAliases;
			
			refList.add("\"" + alias + "\"" + " = " + "\"" + aliasedCmd + "\"");
		}
		
		StringBuilder sb = new StringBuilder();
		
		if (registeredAliases.size() > 0) {
			sb.append("\n");
			sb.append("Registered aliases:");
			
			for (String registeredAlias : registeredAliases) {
				sb.append("\n");
				sb.append(registeredAlias);
			}
		}
		
		if (invalidAliases.size() > 0) {
			sb.append("\n");
			sb.append("Invalid/Conflicting aliases (use removeAlias to delete, or fix the conflict and restart):");
			
			for (String invalidAlias : invalidAliases) {
				sb.append("\n");
				sb.append(invalidAlias);
			}
		}
		
		message.reply(sb.toString());
	}
	
	public void aliasCommand(String command, String[] args, IUser sender, IChannel channel, IMessage message) {
		if (!activeAliases.contains(command.toLowerCase())) {
			return;
		}
		
		final String aliasedCmd = config.properties.get(command.toLowerCase());
		
		if (aliasedCmd == null) {
			return; // this should never happen
		}
		
		// TODO: capture arguments/substitution
		
		// dispatch the aliased cmd
		commandStore.dispatchCommand(sender, channel, message, aliasedCmd);
	}
	
	private boolean registerAlias(String alias, String aliasedCmd, boolean updateConfig) {
		// if the alias is already registered, unregister the command so it is redefined
		// if we didn't keep a separate list of active aliases, we would end up
		// removing legit commands here
		if (activeAliases.contains(alias.toLowerCase())) {
			commandStore.unregisterCommand(alias);
		}
		
		// don't allow registering aliases that already exist as commands
		if (commandStore.hasCommand(alias)) {
			return false;
		}
		
		activeAliases.add(alias);
		commandStore.registerCommand(new CommandFactory()
			.setCommandName(alias)
			.setPermission("") // no permission because it will use the aliased command permission
			.setCallback(this::aliasCommand)
			.build()
		);
		
		if (updateConfig) {
			config.properties.put(alias.toLowerCase(), aliasedCmd);
			botInstance.getManager().saveConfig();
		}
		
		return true;
	}
	
	private boolean unregisterAlias(String alias) {
		// first, remove it from the config, as we could be removing a bad alias that isn't registered
		if (config.properties.remove(alias.toLowerCase()) != null) {
			botInstance.getManager().saveConfig();
			
			// if it was also a registered alias, unregister it from there as well
			if (activeAliases.contains(alias)) {
				activeAliases.remove(alias);
				commandStore.unregisterCommand(alias);
			}
			
			return true;
		}
		
		return false;
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
