package com.avygeil.bprnt.command.arg;

public class ArgumentDescriptor {
	
	public final String name;
	public final ArgumentType type;
	public final boolean optional;
	public final boolean required;
	public final boolean propagate;
	
	public ArgumentDescriptor(String name, ArgumentType type) {
		this(name, type, false, false);
	}
	
	public ArgumentDescriptor(String name, ArgumentType type, boolean optional, boolean propagate) {
		this.name = name;
		this.type = type;
		this.optional = optional;
		this.required = !optional;
		this.propagate = propagate;
	}

}
