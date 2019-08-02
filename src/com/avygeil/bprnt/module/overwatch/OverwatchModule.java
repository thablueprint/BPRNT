package com.avygeil.bprnt.module.overwatch;

import com.avygeil.bprnt.bot.Bot;
import com.avygeil.bprnt.command.Command;
import com.avygeil.bprnt.command.CommandFactory;
import com.avygeil.bprnt.command.CommandStore;
import com.avygeil.bprnt.config.ModuleConfig;
import com.avygeil.bprnt.module.ModuleBase;
import com.avygeil.bprnt.module.ModulePriority;
import com.avygeil.bprnt.util.DiscordUtils;
import com.google.common.base.Charsets;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.util.Snowflake;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class OverwatchModule extends ModuleBase {

    public static final String META_FILENAME = "meta.txt";
    public static final String TIERS_FILENAME = "tiers.csv";

    private final OverwatchMeta meta = new OverwatchMeta();
    private final OverwatchPlayerTiers tiers = new OverwatchPlayerTiers();

    private File metaFile;
    private File tiersFile;

    public OverwatchModule(Bot botInstance, ModuleConfig config, File dataFolder, Logger logger) {
        super(botInstance, config, dataFolder, logger);
    }

    public OverwatchMeta getMeta() {
        return meta;
    }

    public OverwatchPlayerTiers getTiers() {
        return tiers;
    }

    @Override
    public int getPriority() {
        return ModulePriority.NORMAL;
    }

    @Override
    public boolean load() {
        return true;
    }

    @Override
    public void postLoad() {
        String failReason;

        metaFile = new File(dataFolder, META_FILENAME);

        if ((failReason = loadMetaFromFile(metaFile)) != null) {
            LOGGER.warn("Failed to load meta file, disabling feature. Reload with commands after fixing. Reason: " + failReason);
        }

        tiersFile = new File(dataFolder, TIERS_FILENAME);

        int numLoaded = 0;
        for (Map.Entry<String, String> configEntries : config.properties.entrySet()) {
            final Snowflake userId = Snowflake.of(configEntries.getKey());
            final String alias = configEntries.getValue();

            if (tiers.hasAliasForUserId(userId)) {
                LOGGER.warn("User ID defined twice in config, ignoring: " + userId.asString());
                continue;
            }

            if (tiers.hasUserIdForAlias(alias)) {
                LOGGER.warn("Alias defined twice in config, ignoring: " + userId.asString());
                continue;
            }

            tiers.setAliasForUserId(userId, alias);
            ++numLoaded;
        }

        LOGGER.info("Loaded " + numLoaded + " aliases from config");

        if ((failReason = loadTiersFromFile(tiersFile)) != null) {
            LOGGER.warn("Failed to load tiers CSV file, disabling feature. Reload with commands after fixing. Reason: " + failReason);
        }
    }

    private String loadMetaFromFile(File input) {
        if (!input.exists()) {
            return "File not found";
        }

        try {
            meta.loadMetaFromLines(FileUtils.readLines(input, Charsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        } catch (OverwatchParserException e) {
            return "Line " + e.getLine() + ": " + e.getMessage();
        }

        return null;
    }

    private String loadTiersFromFile(File input) {
        if (!input.exists()) {
            return "File not found";
        }

        try {
            tiers.loadTiersFromString(this, FileUtils.readFileToString(input, Charsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (OverwatchParserException e) {
            return "Line " + e.getLine() + ": " + e.getMessage();
        }

        return null;
    }

    @Override
    public void unload() {
    }

    @Override
    public void registerCommands(CommandStore store) {
        store.registerCommands(new CommandFactory()
        	.newParentCommand("owsetup")
                .newSubcommand("setAlias")
                    .withPermission("command.owsetup.setalias")
                    .setCallback(this::overwatchSetupSetAliasCommand)
                    .done()
                .newSubcommand("listAliases")
                    .withPermission("command.owsetup.listaliases")
                    .setCallback(this::overwatchSetupListAliasesCommand)
                    .done()
                .newSubcommand("reloadmeta")
                    .withPermission("command.owsetup.reloadmeta")
                    .setCallback(this::overwatchSetupReloadMetaCommand)
                    .done()
                .newSubcommand("reloadcsv")
                    .withPermission("command.owsetup.reloadcsv")
                    .setCallback(this::overwatchSetupReloadCSVCommand)
                    .done()
                .done()
            .newCommand("owcomp")
                .withPermission("command.owcomp")
                .setCallback(this::overwatchCompCommand)
                .done()
        );
    }

    public void overwatchSetupSetAliasCommand(Command cmd, String[] args, Member sender, Message message) {
        if (args.length < 1) {
            DiscordUtils.replyToMessage(message, "Usage: `!owsetup setAlias <discord id> [csv name]`");
            return;
        }

        Snowflake userId;

        try {
            userId = Snowflake.of(args[0]);
        } catch (NumberFormatException e) {
            DiscordUtils.replyToMessage(message, "Invalid Discord ID, right click on user and use \"Copy ID\" for a valid ID");
            return;
        }

        if (args.length == 1) {
            // removing alias for id

            if (!tiers.hasAliasForUserId(userId)) {
                DiscordUtils.replyToMessage(message, "No CSV alias is defined for user ID \"" + userId.asString() + "\"");
                return;
            }

            tiers.setAliasForUserId(userId, "");

            config.properties.remove(userId);
        } else {
            // setting alias for id
            final String alias = args[1];

            if (tiers.hasUserIdForAlias(alias)) {
                DiscordUtils.replyToMessage(message, "CSV alias \"" + alias + "\" is already defined for a user ID");
                return;
            }

            boolean isAnUpdate = tiers.hasAliasForUserId(userId);
            tiers.setAliasForUserId(userId, alias);

            if (!isAnUpdate) {
                DiscordUtils.replyToMessage(message, "CSV alias for user ID \"" + userId.asString() + "\" set to \"" + alias + "\"");
            } else {
                DiscordUtils.replyToMessage(message, "CSV alias for user ID \"" + userId.asString() + "\" updated to \"" + alias + "\"");
            }

            config.properties.put(userId.asString(), alias);
        }

        botInstance.getManager().saveConfig();
    }

    public void overwatchSetupListAliasesCommand(Command cmd, String[] args, Member sender, Message message) {
        if (config.properties.isEmpty()) {
            DiscordUtils.replyToMessage(message, "No CSV alias set yet");
            return;
        }

        StringBuilder sb = new StringBuilder();
        config.properties.entrySet().forEach(e -> sb.append(e.getKey() + " - " + e.getValue() + '\n'));
        DiscordUtils.replyToMessage(message, sb.toString());
    }

    public void overwatchSetupReloadMetaCommand(Command cmd, String[] args, Member sender, Message message) {
        String failReason = loadMetaFromFile(metaFile);

        if (failReason == null) {
            DiscordUtils.replyToMessage(message,"Successfully parsed " + meta.getNumHeroes() + " heroes");
        } else {
            DiscordUtils.replyToMessage(message, "Reload failed. Reason: " + failReason);
        }
    }

    public void overwatchSetupReloadCSVCommand(Command cmd, String[] args, Member sender, Message message) {
        String failReason = loadTiersFromFile(tiersFile);

        if (failReason == null) {
            DiscordUtils.replyToMessage(message,"Successfully parsed");
        } else {
            DiscordUtils.replyToMessage(message, "Reload failed. Reason: " + failReason);
        }
    }

    public void overwatchCompCommand(Command cmd, String[] args, Member sender, Message message) {
        if (args.length < 1) {
            DiscordUtils.replyToMessage(message, "Usage: `!owcomp <@Player1 [@Player2 ...]>`");
            return;
        }

        List<Member> mentions = message.getUserMentions().flatMap(u -> u.asMember(sender.getGuildId())).collectList().block();

        if (mentions.isEmpty()) {
            DiscordUtils.replyToMessage(message, "You must mention at least one user");
            return;
        }

        if (mentions.size() > 6) {
            DiscordUtils.replyToMessage(message, "You can only mention at most 6 users");
            return;
        }

        StringBuilder calculatorOutput = new StringBuilder();

        OverwatchCompCalculator calculator = new OverwatchCompCalculator(mentions, meta, tiers, calculatorOutput);

        int comps = 0;
        float highestScore = -1.0f;
        int[] bestComp = new int[6];

        long startTime = System.nanoTime();
        do {
            float score = calculator.calculateCurrentScore();

            if (score > highestScore) {
                highestScore = score;
                bestComp = calculator.copyCompBuffer();
            }

            ++comps;
        } while (calculator.advance());
        long elapsedTime = System.nanoTime() - startTime;

        calculatorOutput.append('\n');

        String[] bestCompNames = OverwatchCompCalculator.convertCompIndexToNames(meta, bestComp);
        int i;
        for (i = 0; i < 6; ++i) {
            if (i < mentions.size()) {
                calculatorOutput.append(mentions.get(i).getDisplayName() + ": " + bestCompNames[i] + '\n');
            } else {
                calculatorOutput.append("Random " + (i - mentions.size() + 1) + ": " + bestCompNames[i] + '\n');
            }
        }

        calculatorOutput.append('\n');
        calculatorOutput.append("System took " + elapsedTime / 1000000L + " milliseconds for " + comps + " buffer rotations");

        DiscordUtils.replyToMessage(message, calculatorOutput.toString());
    }

}
