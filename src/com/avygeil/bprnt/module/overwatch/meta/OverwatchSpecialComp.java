package com.avygeil.bprnt.module.overwatch.meta;

public class OverwatchSpecialComp extends OverwatchSixPlayersCompBitmaskBase {

    private final float multiplier;

    public OverwatchSpecialComp(OverwatchNamedBitflagObject[] objects, float multiplier) throws IllegalArgumentException {
        super(objects);
        this.multiplier = multiplier;

        if (objects.length != 6) {
            throw new IllegalArgumentException("Special comp array size must be equal to 6");
        }
    }

    public float getMultiplier() {
        return multiplier;
    }

}
