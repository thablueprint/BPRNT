package com.avygeil.bprnt.module.overwatch;

import com.avygeil.bprnt.module.overwatch.meta.OverwatchHero;
import com.avygeil.bprnt.module.overwatch.meta.OverwatchHeroCategory;
import com.avygeil.bprnt.module.overwatch.meta.OverwatchHeroTag;
import com.avygeil.bprnt.module.overwatch.meta.OverwatchMultiBitmaskObject;
import com.avygeil.bprnt.module.overwatch.meta.OverwatchNamedBitflagObject;
import com.avygeil.bprnt.module.overwatch.meta.OverwatchPairing;
import com.avygeil.bprnt.module.overwatch.meta.OverwatchSpecialComp;
import com.avygeil.bprnt.util.FormatUtils;
import org.apache.commons.collections4.ComparatorUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class OverwatchMeta {

    private final Set<OverwatchNamedBitflagObject> references = new HashSet<>();
    private final List<OverwatchHero> heroes = new ArrayList<>();
    private final List<OverwatchSpecialComp> specialComps = new ArrayList<>();
    private final List<OverwatchPairing> pairings = new ArrayList<>();
    private final Map<String, Float> tierMultipliers = new HashMap<>();

    public Optional<OverwatchNamedBitflagObject> findReference(String refName) {
        for (OverwatchNamedBitflagObject reference : references) {
            if (reference.getName().equalsIgnoreCase(refName)) {
                return Optional.of(reference);
            }
        }

        return Optional.empty();
    }

    public int getNumHeroes() {
        return heroes.size();
    }

    public OverwatchHero getHeroWithIndex(int index) throws IndexOutOfBoundsException {
        return heroes.get(index);
    }

    public Optional<Integer> getIndexForHeroName(String name) {
        int i;
        for (i = 0; i < heroes.size(); ++i) {
            if (heroes.get(i).getName().equalsIgnoreCase(name)) {
                return Optional.of(i);
            }
        }

        return Optional.empty();
    }

    public float getGlobalCompModifier(int[] compInput) throws IllegalArgumentException {
        if (compInput.length != 6) {
            throw new IllegalArgumentException("Comp array size must be equal to 6");
        }

        // clone the array so that we can safely shuffle its order later
        int[] comp = Arrays.copyOf(compInput, compInput.length);

        return shuffleAndTestComps(comp.length, comp, new MutableBoolean(false), new MutableFloat(1.0f), new HashSet<>(), new MutableFloat(0.0f));
    }

    private float shuffleAndTestComps(int k, int[] a, MutableBoolean foundSpecial, MutableFloat global, Set<Integer> pairingsApplied, MutableFloat additional) {
        if (k == 1) {
            // check for special comps unless we already have one
            if (foundSpecial.isFalse()) {
                for (OverwatchSpecialComp specialComp : specialComps) {
                    if (correspondsToComp(specialComp, a)) {
                        global.setValue(specialComp.getMultiplier());
                        foundSpecial.setTrue();
                        break;
                    }
                }
            }

            // check for all pairings regardless (we don't want to count them multiple times)
            int i;
            for (i = 0; i < pairings.size(); ++i) {
                if (!pairingsApplied.contains(i)) {
                    OverwatchPairing pairing = pairings.get(i);
                    if (correspondsToComp(pairing, a)) {
                        additional.add(pairing.getOffset());
                        pairingsApplied.add(i);
                    }
                }
            }
        } else {
            // heap's permutation algorithm

            shuffleAndTestComps(k - 1, a, foundSpecial, global, pairingsApplied, additional);

            int i;
            for(i = 0; i < (k - 1); ++i) {
                int t;
                if(k % 2 == 0) {
                    t = a[i];
                    a[i] = a[k - 1];
                    a[k - 1] = t;
                } else {
                    t = a[0];
                    a[0] = a[k - 1];
                    a[k - 1] = t;
                }

                shuffleAndTestComps(k - 1, a, foundSpecial, global, pairingsApplied, additional);
            }
        }

        return global.floatValue() + additional.floatValue();
    }

    private boolean correspondsToComp(OverwatchMultiBitmaskObject object, int[] comp) {
        long[] bitmasks = object.getBitmasks();

        int i;
        for (i = 0; i < comp.length; ++i) {
            if ((bitmasks[i] & (1 << comp[i])) == 0) {
                return false;
            }
        }

        return true;
    }

    public Optional<Float> getSkillMultiplierForTierString(String tierString) {
        if (!tierMultipliers.containsKey(tierString.toLowerCase())) {
            return Optional.empty();
        }

        return Optional.of(tierMultipliers.get(tierString.toLowerCase()));
    }

    public void loadMetaFromLines(List<String> lines) throws OverwatchParserException {
        references.clear();
        heroes.clear();
        specialComps.clear();
        pairings.clear();
        tierMultipliers.clear();

        Set<OverwatchNamedBitflagObject> newReferences = new HashSet<>();
        List<OverwatchHero> newHeroes = new ArrayList<>();
        List<OverwatchSpecialComp> newSpecialComps = new ArrayList<>();
        List<OverwatchPairing> newPairings = new ArrayList<>();
        Map<String, Float> newTierMultipliers = new HashMap<>();

        Map<String, List<OverwatchHero>> newCategories = new HashMap<>();
        Map<String, List<OverwatchHero>> newTags = new HashMap<>();

        List<Pair<String[], Float>> futureNewSpecialComps = new ArrayList<>();
        List<Pair<String[], Float>> futureNewPairings = new ArrayList<>();

        final int totalLines = lines.size();

        int stage = 0;

        for (int lineNum = 1; lineNum <= totalLines; ++lineNum) {
            String line = lines.get(lineNum - 1).trim();

            if (line.startsWith("#")) {
                continue;
            }

            if (line.equals("{") || line.equals("}")) {
                continue;
            }

            final String[] args = FormatUtils.tokenize(line);

            if (args.length == 0) {
                continue;
            }

            int nextStage = stage;

            if (line.equals("Heroes")) {
                nextStage = 1;
            } else if (line.equals("SpecialCompStrengthMultipliers")) {
                nextStage = 2;
            } else if (line.equals("PairingStrengthOffsets")) {
                nextStage = 3;
            } else if (line.equals("CSVTierMultipliers")) {
                nextStage = 4;
            }

            if (stage != nextStage) {
                if (stage == 0 && nextStage != 1) {
                    throw new OverwatchParserException("Heroes section must be the first section", lineNum);
                }

                stage = nextStage;

                continue;
            }

            switch (stage) {
                case 1: {
                    if (args.length != 3 && args.length != 4) {
                        throw new OverwatchParserException("Expected 3 or 4 arguments in section Heroes, got " + args.length, lineNum);
                    }

                    String newHeroName = args[0];
                    float newPickRate;

                    try {
                        newPickRate = Float.valueOf(args[1]);
                    } catch (NumberFormatException e) {
                        throw new OverwatchParserException("Expected float for pick rate argument in section Heroes", lineNum);
                    }

                    // assume the index is going to be equal to size, since it is inserted at the end of the array
                    OverwatchHero newHero = new OverwatchHero(newHeroName, newHeroes.size(), newPickRate);

                    if (newHeroes.contains(newHero)) {
                        throw new OverwatchParserException("Hero " + newHeroName + " already defined", lineNum);
                    }

                    newHeroes.add(newHero);

                    // now parse categories (mandatory)
                    String[] categoryList = FormatUtils.tokenize(args[2].trim());

                    if (categoryList.length != 1) {
                        throw new OverwatchParserException("Heroes must define one category, at most and at least", lineNum);
                    }

                    String category = categoryList[0];

                    List<OverwatchHero> listForThisCategory = newCategories.get(category);

                    if (listForThisCategory == null) {
                        listForThisCategory = new ArrayList<>();
                        newCategories.put(category, listForThisCategory);
                    }

                    if (listForThisCategory.contains(newHero)) {
                        throw new OverwatchParserException("Category \"" + category + "\" listed multiple times for hero \"" + newHeroName + "\"", lineNum);
                    }

                    listForThisCategory.add(newHero);

                    // parse tags which are optional
                    if (args.length == 4) {
                        String[] tagList = FormatUtils.tokenize(args[3].trim());

                        for (String tag : tagList) {
                            List<OverwatchHero> listForThisTag = newTags.get(tag);

                            if (listForThisTag == null) {
                                listForThisTag = new ArrayList<>();
                                newTags.put(tag, listForThisTag);
                            }

                            if (listForThisTag.contains(newHero)) {
                                throw new OverwatchParserException("Tag \"" + tag + "\" listed multiple times for hero \"" + newHeroName + "\"", lineNum);
                            }

                            listForThisTag.add(newHero);
                        }
                    }
                } break;

                case 2: {
                    if (args.length != 2) {
                        throw new OverwatchParserException("Expected 2 arguments in section SpecialCompStrengthMultipliers", lineNum);
                    }

                    String[] referenceList = FormatUtils.tokenize(args[0].trim());

                    if (referenceList.length != 6) {
                        throw new OverwatchParserException("Special comp must have 6 references", lineNum);
                    }

                    float newMultiplier;

                    try {
                        newMultiplier = Float.valueOf(args[1]);
                    } catch (NumberFormatException e) {
                        throw new OverwatchParserException("Expected float for multiplier argument in section SpecialCompStrengthMultipliers", lineNum);
                    }

                    futureNewSpecialComps.add(Pair.of(referenceList, newMultiplier));
                } break;

                case 3: {
                    if (args.length != 2) {
                        throw new OverwatchParserException("Expected 2 arguments in section PairingStrengthOffsets", lineNum);
                    }

                    String[] referenceList = FormatUtils.tokenize(args[0].trim());

                    if (referenceList.length < 1 || referenceList.length > 6) {
                        throw new OverwatchParserException("Special comp must have between 1 and 6 references", lineNum);
                    }

                    float newOffset;

                    try {
                        newOffset = Float.valueOf(args[1]);
                    } catch (NumberFormatException e) {
                        throw new OverwatchParserException("Expected float for offset argument in section PairingStrengthOffsets", lineNum);
                    }

                    futureNewPairings.add(Pair.of(referenceList, newOffset));
                } break;

                case 4: {
                    if (args.length != 2) {
                        throw new OverwatchParserException("Expected 2 arguments in section CSVTierMultipliers", lineNum);
                    }

                    String newTierName = args[0];
                    float newTierMultiplier;

                    try {
                        newTierMultiplier = Float.valueOf(args[1]);
                    } catch (NumberFormatException e) {
                        throw new OverwatchParserException("Expected float for multiplier argument in section CSVTierMultipliers", lineNum);
                    }

                    if (newTierMultipliers.containsKey(newTierName.toLowerCase())) {
                        throw new OverwatchParserException("Tier " + newTierName + " already defined", lineNum);
                    }

                    newTierMultipliers.put(newTierName.toLowerCase(), newTierMultiplier);
                } break;

                default: // no-op
            }
        }

        if (newHeroes.isEmpty()) {
            throw new OverwatchParserException("Parsing ended with no heroes defined", totalLines);
        }

        // add heroes to references and convert categories and tags to objects

        for (OverwatchHero newHero : newHeroes) {
            if (newReferences.contains(newHero)) {
                throw new OverwatchParserException("Reference \"" + newHero.getName() + "\" is already used", totalLines);
            }

            newReferences.add(newHero);
        }

        for (Map.Entry<String, List<OverwatchHero>> entry : newCategories.entrySet()) {
            String newCategoryName = entry.getKey();
            List<OverwatchHero> newCategoryHeroList = entry.getValue();

            OverwatchHeroCategory newCategory = new OverwatchHeroCategory(newCategoryName, newCategoryHeroList.stream().toArray(OverwatchHero[]::new));

            if (newReferences.contains(newCategory)) {
                throw new OverwatchParserException("Reference \"" + newCategoryName + "\" is already used", totalLines);
            }

            newReferences.add(newCategory);
        }

        for (Map.Entry<String, List<OverwatchHero>> entry : newTags.entrySet()) {
            String newTagName = entry.getKey();
            List<OverwatchHero> newTagHeroList = entry.getValue();

            OverwatchHeroTag newTag = new OverwatchHeroTag(newTagName, newTagHeroList.stream().toArray(OverwatchHero[]::new));

            if (newReferences.contains(newTag)) {
                throw new OverwatchParserException("Reference \"" + newTagName + "\" is already used", totalLines);
            }

            newReferences.add(newTag);
        }

        references.addAll(newReferences);
        heroes.addAll(newHeroes);

        // create special comp/pairing objects

        for (Pair<String[], Float> specialCompPair : futureNewSpecialComps) {
            String[] referenceList = specialCompPair.getLeft();
            float newMultiplier = specialCompPair.getRight();

            OverwatchNamedBitflagObject[] newObjects = new OverwatchNamedBitflagObject[6];

            int i;
            for (i = 0; i < 6; ++i) {
                final String referenceName = referenceList[i];
                OverwatchNamedBitflagObject newObject = findReference(referenceName)
                        .orElseThrow(() -> new OverwatchParserException("Reference \"" + referenceName + "\" does not exist", totalLines));
                newObjects[i] = newObject;
            }

            try {
                OverwatchSpecialComp newSpecialComp = new OverwatchSpecialComp(newObjects, newMultiplier);
                newSpecialComps.add(newSpecialComp);
            } catch (IllegalArgumentException e) {
                throw new OverwatchParserException(e.getMessage(), totalLines);
            }
        }

        for (Pair<String[], Float> pairingPair : futureNewPairings) {
            String[] referenceList = pairingPair.getLeft();
            float newOffset = pairingPair.getRight();

            OverwatchNamedBitflagObject[] newObjects = new OverwatchNamedBitflagObject[referenceList.length];

            int i;
            for (i = 0; i < referenceList.length; ++i) {
                final String referenceName = referenceList[i];
                OverwatchNamedBitflagObject newObject = findReference(referenceName)
                        .orElseThrow(() -> new OverwatchParserException("Reference \"" + referenceName + "\" does not exist", totalLines));
                newObjects[i] = newObject;
            }

            try {
                OverwatchPairing newPairing = new OverwatchPairing(newObjects, newOffset);
                newPairings.add(newPairing);
            } catch (IllegalArgumentException e) {
                throw new OverwatchParserException(e.getMessage(), totalLines);
            }
        }

        // sort special comp and pairing arrays by multiplier/offset
        newSpecialComps.sort(ComparatorUtils.reversedComparator((o1, o2) -> Float.compare(o1.getMultiplier(), o2.getMultiplier())));
        newPairings.sort(ComparatorUtils.reversedComparator((o1, o2) -> Float.compare(o1.getOffset(), o2.getOffset())));

        specialComps.addAll(newSpecialComps);
        pairings.addAll(newPairings);
        tierMultipliers.putAll(newTierMultipliers);
    }

}
