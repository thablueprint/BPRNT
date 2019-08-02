package com.avygeil.bprnt.module.overwatch.meta;

public abstract class OverwatchHeroReferenceBase implements OverwatchNamedBitflagObject {

    private final String name;

    protected OverwatchHeroReferenceBase(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }

        OverwatchHeroReferenceBase other = (OverwatchHeroReferenceBase)o;
        return this.name.equalsIgnoreCase(other.getName());
    }

}
