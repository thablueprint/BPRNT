package com.avygeil.bprnt;

import java.io.IOException;

import com.avygeil.bprnt.bot.BotManager;

import sx.blah.discord.util.DiscordException;

public class Main {
	
	public static void main(String[] args) {
		final BotManager botManager = new BotManager();
		
		try {
			botManager.initialize();
		} catch (DiscordException | IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		botManager.start();
		
	}

}
