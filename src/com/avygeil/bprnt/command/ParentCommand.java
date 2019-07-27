package com.avygeil.bprnt.command;

import com.avygeil.bprnt.util.FormatUtils;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class ParentCommand extends CommandBase {
	
	private final Map<String, Command> subcommands = new CaseInsensitiveMap<>();
	private boolean immutable = false;
	
	public ParentCommand(ParentCommand parent, String commandName) {
		super(parent, commandName);
	}
	
	// We can't make this object immutable from the start, because it would require knowing both
	// the parent and children at creation time, which is sometimes impossible.
	// So instead, we provide a method to make it immutable after adding stuff.
	public ParentCommand setImmutable() {
		immutable = true;
		return this;
	}
	
	public boolean hasSubcommand(String subcommandName) {
		return subcommands.containsKey(subcommandName);
	}
	
	public Command getSubcommandByName(String subcommandName) {
		return subcommands.get(subcommandName);
	}
	
	public void registerSubcommand(Command subcommand) {
		if (immutable) throw new UnsupportedOperationException();
		subcommands.put(subcommand.getCommandName(), subcommand);
	}
	
	public void unregisterSubcommand(String subcommandName) {
		if (immutable) throw new UnsupportedOperationException();
		subcommands.remove(subcommandName);
	}
	
	@Override
	public String getPermission() {
		return "";
	}
	
	@Override
	public String getUsage() {
		// This is a parent command which can have subcommands of various types
		// Generating complete usage strings could be complicated, so for now
		// we just print all the available subcommands for the first level
		
		// TODO: cache this later?
		
		// shouldn't happen for factory made objects which prevents empty subcommands
		if (subcommands.isEmpty()) {
			return "";
		}
		
		List<String> subcommandNames = new ArrayList<>();
		
		for (Command subcommand : subcommands.values()) {
			subcommandNames.add(subcommand.getCommandName());
		}
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("Available subcommands for ");
		sb.append("`" + FormatUtils.getChainedCommandNamesString(parent) + " " + this.getCommandName() + "`");
		sb.append(": ");
		sb.append("`" + StringUtils.join(subcommandNames, ", ") + "`");
		
		return sb.toString();
	}

	@Override
	protected Command invoke_Internal(Queue<String> argQueue, Member sender, Message message)
			throws InvalidUsageException {
		
		// for empty subcommands, just act like we reached the end of the chain, but shouldn't happen
		if (subcommands.isEmpty()) {
			return null;
		}
		
		// if we didn't specify any subcommand, assume we wanted to see the usage (which will be filled upstream)
		if (argQueue.isEmpty()) {
			throw new InvalidUsageException();
		}
		
		// chain with the subcommand if it exists and consume the subcommand name argument
		
		final String subCommandName = argQueue.poll();
		final Command subCommand = getSubcommandByName(subCommandName);
		
		if (subCommand == null) {
			throw new InvalidUsageException("Unknown subcommand `" + subCommandName + "`");
		}
		
		return subCommand;
	}

}
