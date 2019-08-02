package com.avygeil.bprnt.module.overwatch.meta;

public class OverwatchPairing extends OverwatchSixPlayersCompBitmaskBase {

    private final float offset;

    public OverwatchPairing(OverwatchNamedBitflagObject[] objects, float offset) throws IllegalArgumentException {
        super(objects);
        this.offset = offset;
    }

    public float getOffset() {
        return offset;
    }

}
