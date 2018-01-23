package com.avygeil.bprnt;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.util.DiscordException;

public class Bot {

	public static void main(String[] args) {
		if (args.length < 1) {
			throw new IllegalArgumentException();
		}
		
		IDiscordClient client = null;
		
		try {
			ClientBuilder builder = new ClientBuilder();
			builder.withToken(args[0]);
			client = builder.login();
		} catch (DiscordException e) {
			e.printStackTrace();
		}
		
		if (client != null) {
			new Bot(client).Run();
		}
	}
	
	private IDiscordClient client;
	
	public Bot(IDiscordClient client) {
		this.client = client;
	}
	
	public void Run() {
		System.out.println("hello world");
	}

}
