package com.avygeil.bprnt.module.overwatch.meta;

public abstract class OverwatchSixPlayersCompBitmaskBase implements OverwatchMultiBitmaskObject {

    private final long[] validatorHeroBitflags;

    protected OverwatchSixPlayersCompBitmaskBase(OverwatchNamedBitflagObject[] objects) throws IllegalArgumentException {
        if (objects.length == 0) {
            throw new IllegalArgumentException("Cannot create bitmasks from zero length array");
        }

        if (objects.length > 6) {
            throw new IllegalArgumentException("Cannot create bitmasks from more than six array elements");
        }

        validatorHeroBitflags = new long[6];

        // build the validation table
        int i;
        for (i = 0; i < 6; ++i) {
            if (i < objects.length) {
                // use a provided object
                validatorHeroBitflags[i] = objects[i].getBitflags();
            } else {
                // no more provided objects, so make this slot a wildcard slot where all heroes are valid
                validatorHeroBitflags[i] = 0xffffffff;
            }
        }
    }

    @Override
    public long[] getBitmasks() {
        return validatorHeroBitflags;
    }

}
