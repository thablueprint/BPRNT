package com.avygeil.bprnt.command;

import java.util.ArrayList;
import java.util.List;

public class CommandFactory {
	
	private final List<BaseCommandFactory<CommandFactory>> commandFactories = new ArrayList<>();
	
	public ParentCommandFactory<CommandFactory> newParentCommand(String commandName) {
		return newParentCommand().withName(commandName);
	}
	
	public ParentCommandFactory<CommandFactory> newParentCommand() {
		ParentCommandFactory<CommandFactory> result = new ParentCommandFactory<>(this);
		commandFactories.add(result);
		
		return result;
	}
	
	public SimpleCommandFactory<CommandFactory> newCommand(String commandName) {
		return newCommand().withName(commandName);
	}
	
	public SimpleCommandFactory<CommandFactory> newCommand() {
		SimpleCommandFactory<CommandFactory> result = new SimpleCommandFactory<>(this);
		commandFactories.add(result);
		
		return result;
	}
	
	public List<Command> build() throws IllegalStateException {
		final List<Command> result = new ArrayList<>();
		
		for (BaseCommandFactory<CommandFactory> commandFactory : commandFactories) {
			result.add(commandFactory.build(null));
		}
		
		return result;
	}
	
	protected abstract class BaseCommandFactory<T> {
		
		private final T parentFactory;
		protected String commandName = "";
		
		protected BaseCommandFactory(T parentFactory) {
			this.parentFactory = parentFactory;
		}
		
		public final T done() {
			return parentFactory;
		}
		
		public abstract Command build(ParentCommand myParent) throws IllegalStateException;
		
	}
	
	public class ParentCommandFactory<T> extends BaseCommandFactory<T> {
		
		private final List<BaseCommandFactory<ParentCommandFactory<T>>> subcommandFactories = new ArrayList<>();
		
		public ParentCommandFactory(T parentFactory) {
			super(parentFactory);
		}
		
		public ParentCommandFactory<T> withName(String commandName) {
			this.commandName = commandName;
			return this;
		}
		
		public ParentCommandFactory<ParentCommandFactory<T>> newParentSubcommand(String subcommandName) {
			return newParentSubcommand().withName(subcommandName);
		}
		
		public ParentCommandFactory<ParentCommandFactory<T>> newParentSubcommand() {
			ParentCommandFactory<ParentCommandFactory<T>> result = new ParentCommandFactory<>(this);
			subcommandFactories.add(result);
			
			return result;
		}
		
		public SimpleCommandFactory<ParentCommandFactory<T>> newSubcommand(String subcommandName) {
			return newSubcommand().withName(subcommandName);
		}
		
		public SimpleCommandFactory<ParentCommandFactory<T>> newSubcommand() {
			SimpleCommandFactory<ParentCommandFactory<T>> result = new SimpleCommandFactory<>(this);
			subcommandFactories.add(result);
			
			return result;
		}

		@Override
		public Command build(ParentCommand myParent) throws IllegalStateException {
			// parent commands must have a name and at least one subcommand
			if (commandName.isEmpty() || subcommandFactories.isEmpty()) {
				throw new IllegalStateException();
			}
			
			ParentCommand thisCommand = new ParentCommand(myParent, commandName);
			
			for (BaseCommandFactory<ParentCommandFactory<T>> subcommandFactory : subcommandFactories) {
				thisCommand.registerSubcommand(subcommandFactory.build(thisCommand));
			}
			
			return thisCommand.setImmutable();
		}
		
	}
	
	public class SimpleCommandFactory<T> extends BaseCommandFactory<T> {
		
		private String permission = "";
		private CommandCallback callback = null;
		
		public SimpleCommandFactory(T parentFactory) {
			super(parentFactory);
		}
		
		public SimpleCommandFactory<T> withName(String commandName) {
			this.commandName = commandName;
			return this;
		}
		
		public SimpleCommandFactory<T> withPermission(String permission) {
			this.permission = permission;
			return this;
		}
		
		public SimpleCommandFactory<T> setCallback(CommandCallback callback) {
			this.callback = callback;
			return this;
		}

		@Override
		public Command build(ParentCommand myParent) throws IllegalStateException {
			// normal commands must have a name and a callback
			if (commandName.isEmpty() || callback == null) {
				throw new IllegalStateException();
			}
			
			// TODO: setup format correctly
			final CommandFormat format = new CommandFormat();
			
			return new SimpleCommand(myParent, commandName, permission, callback, format);
		}
		
	}

}
