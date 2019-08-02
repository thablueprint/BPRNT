package com.avygeil.bprnt.module.overwatch.meta;

public abstract class OverwatchHeroContainerBase extends OverwatchHeroReferenceBase {

    private final long heroBitflags;

    protected OverwatchHeroContainerBase(String name, OverwatchHero[] heroes) {
        super(name);

        long flags = 0L;
        for (OverwatchHero hero : heroes) {
            flags |= (1 << hero.getIndex());
        }
        heroBitflags = flags;
    }

    protected OverwatchHeroContainerBase(String name, long bitflags) {
        super(name);
        this.heroBitflags = bitflags;
    }

    @Override
    public long getBitflags() {
        return heroBitflags;
    }

    public boolean hasHero(OverwatchHero hero) {
        return hasHero(hero.getIndex());
    }

    public boolean hasHero(int heroIndex) {
        return (heroBitflags & (1 << heroIndex)) != 0;
    }

}
