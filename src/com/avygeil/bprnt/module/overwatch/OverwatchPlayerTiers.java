package com.avygeil.bprnt.module.overwatch;

import discord4j.core.object.util.Snowflake;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualLinkedHashBidiMap;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class OverwatchPlayerTiers {

    private final BidiMap<Snowflake, String> playerAliases = new DualLinkedHashBidiMap<>();
    private final Map<Snowflake, float[]> playerTiers = new HashMap<>();

    public String getAliasForUserId(Snowflake userId) {
        return playerAliases.get(userId);
    }

    public Snowflake getUserIdForAlias(String alias) {
        return playerAliases.getKey(alias.toLowerCase());
    }

    public boolean hasAliasForUserId(Snowflake userId) {
        return playerAliases.containsKey(userId);
    }

    public boolean hasUserIdForAlias(String alias) {
        return playerAliases.containsValue(alias.toLowerCase());
    }

    public void setAliasForUserId(Snowflake userId, String alias) {
        if (!alias.isEmpty()) {
            playerAliases.put(userId, alias.toLowerCase());
        } else {
            playerAliases.remove(userId);
        }
    }

    public Optional<Float> getTierMultiplierForPlayer(Snowflake userId, int heroIndex) {
        if (!playerTiers.containsKey(userId)) {
            return Optional.empty();
        }

        return Optional.of(playerTiers.get(userId)[heroIndex]);
    }

    public void loadTiersFromString(OverwatchModule module, String inputString) throws IOException, OverwatchParserException {
        playerTiers.clear();

        Map<Snowflake, float[]> newPlayerTiers = new HashMap<>();

        CSVParser parser = CSVParser.parse(inputString, CSVFormat.DEFAULT);
        List<CSVRecord> rows =  parser.getRecords();

        if (rows.isEmpty()) {
            throw new OverwatchParserException("No record defined in CSV", 1);
        }

        final int numHeroes = module.getMeta().getNumHeroes();

        // maps hero meta index to csv column index
        int[] heroColumnIndexMapping = new int[numHeroes];
        int numMapped = 0;
        int maxColumnIndex = -1;

        CSVRecord headerRow = rows.get(0);

        int i;
        for (i = 0; i < headerRow.size(); ++i) {
            String cellContent = headerRow.get(i);
            int heroIndex = module.getMeta().getIndexForHeroName(cellContent).orElse(-1);

            if (heroIndex != -1) {
                // this column is a hero defined in the column map, map its index
                heroColumnIndexMapping[heroIndex] = i;
                ++numMapped;

                if (maxColumnIndex < i) {
                    maxColumnIndex = i;
                }
            }
        }

        if (numMapped == 0) {
            throw new OverwatchParserException("First row of the CSV does not contain any valid hero name", 1);
        }

        if (numMapped != numHeroes) {
            throw new OverwatchParserException("First row of the CSV does not define all heroes (expected " + numHeroes + " columns)", 1);
        }

        int lineNum;
        for (lineNum = 2; lineNum <= rows.size(); ++lineNum) {
            CSVRecord row = rows.get(lineNum - 1);

            if (row.size() == 0) {
                continue;
            }

            final String playerAlias = row.get(0);

            if (playerAlias.isEmpty()) {
                continue;
            }

            if (!hasUserIdForAlias(playerAlias)) {
                throw new OverwatchParserException("Alias \"" + playerAlias + "\" is not bound to any user ID", lineNum);
            }

            if (row.size() <= maxColumnIndex) {
                throw new OverwatchParserException("Not enough columns for row", lineNum);
            }

            Snowflake userId = getUserIdForAlias(playerAlias);

            if (newPlayerTiers.containsKey(userId)) {
                throw new OverwatchParserException("Tiers for user ID \"" + userId.asString() + "\" were already defined", lineNum);
            }

            float[] tierMultipliers = new float[numHeroes];

            for (i = 0; i < numHeroes; ++i) {
                String tierString = row.get(heroColumnIndexMapping[i]);
                final int currentLine = lineNum; // fuck lambdas
                float tierMultiplier = module.getMeta().getSkillMultiplierForTierString(tierString)
                        .orElseThrow(() -> new OverwatchParserException("Tier string \"" + tierString + "\" is undefined in meta file", currentLine));
                tierMultipliers[i] = tierMultiplier;
            }

            newPlayerTiers.put(userId, tierMultipliers);
        }

        if (newPlayerTiers.size() == 0) {
            throw new OverwatchParserException("Must define at least one player tier in CSV", rows.size());
        }

        playerTiers.putAll(newPlayerTiers);
    }
}
