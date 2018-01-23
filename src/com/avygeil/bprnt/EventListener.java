package com.avygeil.bprnt;

import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public class EventListener {
	
	@EventSubscriber
	public void onMessageReceivedEvent(MessageReceivedEvent event) {
        System.out.println(event.getMessage().getContent());
    }

}
