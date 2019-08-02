package com.avygeil.bprnt.module.overwatch.meta;

public class OverwatchHero extends OverwatchHeroReferenceBase {

    private final int index;
    private final float pickRate;

    public OverwatchHero(String name, int index, float pickRate) {
        super(name);
        this.index = index;
        this.pickRate = pickRate;
    }

    @Override
    public long getBitflags() {
        return (long)(1 << index);
    }

    public int getIndex() {
        return index;
    }

    public float getPickRate() {
        return pickRate;
    }

}
