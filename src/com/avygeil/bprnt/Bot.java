package com.avygeil.bprnt;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

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
		
		try {
			Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("test.txt"), "UTF-8"));
			try {
				out.write("" + ("\uD83C\uDF4C").length());
			} finally {
				out.close();
			}
		} catch ( Exception e ) {
			
		}
	}

}
