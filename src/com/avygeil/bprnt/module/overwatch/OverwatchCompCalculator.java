package com.avygeil.bprnt.module.overwatch;

import discord4j.core.object.entity.Member;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class OverwatchCompCalculator {

    private final OverwatchMeta meta;
    private final OverwatchPlayerTiers tiers;
    private final StringBuilder output;

    private final int numHeroes;

    // skill tier lookup table
    private float[][] skillTierTable;

    // for each player, an array of size numHeroes, ordered with indexes of heroes cycled from for this player
    private int[][] heroIndexCycle;
    private int[] heroIndexNumCycles;

    // rotating index buffer of heroes
    private int[] rotatingCompBuffer;
    private int rotateIndex;
    private int[] rotateCycleCursors;

    public OverwatchCompCalculator(List<Member> users, OverwatchMeta meta, OverwatchPlayerTiers tiers, StringBuilder output) {
        this.meta = meta;
        this.tiers = tiers;
        this.output = output;

        numHeroes = meta.getNumHeroes();

        if (numHeroes == 0) {
            return;
        }

        // build the individual skill tier lookup table
        skillTierTable = new float[6][numHeroes];
        int numRandoms = 0;
        int i, j;
        for ( i = 0; i < 6; ++i) {
            if (i < users.size()) {
                // a defined user
                Member user = users.get(i);
                int numUndefined = 0;
                for (j = 0; j < numHeroes; ++j) {
                    Optional<Float> skillOpt = tiers.getTierMultiplierForPlayer(user.getId(), j);

                    float skill;
                    if (skillOpt.isPresent()) {
                        skill = skillOpt.get();
                    } else {
                        skill = 1.0f;
                        ++numUndefined;
                    }

                    skillTierTable[i][j] = skill;
                }

                if (numUndefined > 0) {
                    output.append("Player " + user.getDisplayName() + " has no skill tier for " + numUndefined + " heroes, using default multipliers. Add them to the Sheet and run the command again.\n");
                }
            } else {
                // a random, default to 1.0 for all heroes
                for (j = 0; j < numHeroes; ++j) {
                    skillTierTable[i][j] = 1.0f;
                }
                ++numRandoms;
            }
        }
        if (numRandoms > 0) {
            output.append("Assuming " + numRandoms + " randoms, all with the default skill tier for all heroes\n");
        }

        // TODO: for now, init the cycle array with [0:numHeroes-1] but this will change in the future
        heroIndexCycle = new int[6][numHeroes];
        heroIndexNumCycles = new int[6];
        for (i = 0; i < 6; ++i) {
            for (j = 0; j < numHeroes; ++j) {
                heroIndexCycle[i][j] = j;
                heroIndexNumCycles[i]++;
            }
        }

        /*heroIndexCycle = new int[][] {
            {1, 2, 25, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {4, 9, 17, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29},
            {21, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29},
            {4, 9, 17, 20, 25, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        };
        heroIndexNumCycles = new int[] {
                3, 3, 30, 1, 30, 5
        };*/

        // the cursors always start at 1 so that we don't count the 0 state twice while advancing
        rotateCycleCursors = new int[6];
        for (i = 0; i < 6; ++i) {
            rotateCycleCursors[i] = 1;
        }

        // init the rotating buffer with the first index of each cycle array
        rotatingCompBuffer = new int[6];
        rotateIndex = 5;
        for (i = 0; i < 6; ++i) {
            rotatingCompBuffer[i] = heroIndexCycle[i][0];
        }

        // the initialized comp is likely not valid, running advance just once will fix it until the first state is valid
        if (!isValidComp()) {
            advance();
        }
    }

    public boolean advance() {
        do {
            // did we already finish?
            if (rotateIndex < 0) {
                return false;
            }

            // take the next value from the cycle and put it in the primary buffer
            rotatingCompBuffer[rotateIndex] = heroIndexCycle[rotateIndex][rotateCycleCursors[rotateIndex]];

            // now, we need to prepare the cursor for the next read so that when this function is
            // called, the cursor is already in place for the next read
            rotateCycleCursors[rotateIndex]++;
            if (rotateIndex == 5 && rotateCycleCursors[rotateIndex] >= heroIndexNumCycles[rotateIndex]) {
                while (rotateIndex >= 0 && rotateCycleCursors[rotateIndex] >= heroIndexNumCycles[rotateIndex]) {
                    --rotateIndex;
                }
            } else {
                while (rotateIndex < 5) {
                    ++rotateIndex;
                    rotateCycleCursors[rotateIndex] = 1;
                    rotatingCompBuffer[rotateIndex] = heroIndexCycle[rotateIndex][0];
                }
            }
        } while (!isValidComp());
        // if this isn't a valid comp, advance until we find a valid one

        return true;
    }

    private boolean isValidComp() {
        // check for duplicates
        long heroesBitmask = 0L;
        int i;
        for (i = 0; i < 6; ++i) {
            if ((heroesBitmask & (1 << rotatingCompBuffer[i])) != 0) {
                return false;
            }

            heroesBitmask |= (1 << rotatingCompBuffer[i]);
        }

        return true;
    }

    public int[] copyCompBuffer() {
        return Arrays.copyOf(rotatingCompBuffer, rotatingCompBuffer.length);
    }

    public float calculateCurrentScore() {
        float score = 0;

        int i;
        for (i = 0; i < 6; ++i) {
            final int heroIndex = rotatingCompBuffer[i];
            float individualScore = skillTierTable[i][heroIndex] * (1 + meta.getHeroWithIndex(heroIndex).getPickRate());
            score += individualScore;
        }

        score *= meta.getGlobalCompModifier(rotatingCompBuffer);

        return score;
    }

    public static String[] convertCompIndexToNames(OverwatchMeta meta, int[] comp) {
        String[] result = new String[6];

        int i;
        for (i = 0; i < 6; ++i) {
            result[i] = meta.getHeroWithIndex(comp[i]).getName();
        }

        return result;
    }

}
