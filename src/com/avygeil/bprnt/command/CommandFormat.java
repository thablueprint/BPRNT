package com.avygeil.bprnt.command;

import com.avygeil.bprnt.command.arg.ArgumentDescriptor;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class CommandFormat {
	
	private final List<ArgumentDescriptor> descriptors = new ArrayList<>();
	private String formatString;
	
	public CommandFormat addArgument(ArgumentDescriptor newDescriptor) throws IllegalArgumentException {
		// This is the only function that checks whether or not the sequence of descriptors
		// makes sense. ArgumentDescriptor has no restriction on its own, but it cannot be
		// put in a random sequence. That is why you can't construct this object directly
		// from a list of descriptors, we want this function to check that it makes sense.
		
		if (!descriptors.isEmpty()) {
			final ArgumentDescriptor lastDescriptor = descriptors.get(descriptors.size() - 1);
			
			// Rule 1: you can't add a required argument after an optional argument
			if (lastDescriptor.optional && newDescriptor.required) {
				throw new IllegalArgumentException();
			}
			
			// Rule 2: arguments that propagate can only be the last arguments
			if (lastDescriptor.propagate) {
				throw new IllegalArgumentException();
			}
		}
		
		// we passed all tests, add to the end of the list and regenerate the cached usage string
		descriptors.add(newDescriptor);
		updateCachedFormatString();
		
		return this;
	}
	
	public String getFormatString() {
		return formatString;
	}
	
	private void updateCachedFormatString() {
		List<String> usages = new ArrayList<>();
		
		for (ArgumentDescriptor descriptor : descriptors) {
			StringBuilder sb = new StringBuilder();
			
			final char openingDelimiter = descriptor.required ? '<' : '[';
			final char closingDelimiter = descriptor.required ? '>' : ']';
			
			sb.append(openingDelimiter);
			sb.append(descriptor.name);
			if (descriptor.propagate) sb.append(" ...");
			sb.append(closingDelimiter);
			
			usages.add(sb.toString());
		}
		
		formatString = StringUtils.join(usages, ' ');
	}

}
