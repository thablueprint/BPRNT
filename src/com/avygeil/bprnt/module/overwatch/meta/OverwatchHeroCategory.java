package com.avygeil.bprnt.module.overwatch.meta;

public class OverwatchHeroCategory extends OverwatchHeroContainerBase {

    private static final OverwatchHeroCategory wildcard = new OverwatchHeroCategory("", 0xffffffff);

    public OverwatchHeroCategory(String name, OverwatchHero[] heroes) {
        super(name, heroes);
    }

    private OverwatchHeroCategory(String name, long bitflags) {
        super(name, bitflags);
    }

    public static OverwatchHeroCategory wildcard() {
        return wildcard;
    }

}
