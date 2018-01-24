package com.avygeil.bprnt;

import com.avygeil.bprnt.util.BaseEmoji;

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
		client.getDispatcher().registerListener(new EventListener());
		
		String test = "je suis une tulipe";
		String encoded = BaseEmoji.encode(test);
		String decoded = BaseEmoji.decode(encoded);
		
		System.out.println("Base: " + test);
		System.out.println("Encodé: " + encoded);
		System.out.println("Decodé: " + decoded);
	}

}
