package com.avygeil.bprnt.util;

import discord4j.core.object.entity.Message;

public final class DiscordUtils {

    private DiscordUtils() {
    }

    public static void replyToMessage(Message message, String response) {
        message.getChannel().block().createMessage(response).block();
    }

}
