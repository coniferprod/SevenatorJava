package com.coniferproductions.sevenator.datamodel;

import com.coniferproductions.sevenator.RangedInteger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class Algorithm extends RangedInteger {
    public Algorithm(int value) {
        super(1, 32, value);
    }

    public static final Algorithm TYPE = new Algorithm(32);

    private final OperatorIndex ONE = new OperatorIndex(1);
    private final OperatorIndex TWO = new OperatorIndex(2);
    private final OperatorIndex THREE = new OperatorIndex(3);
    private final OperatorIndex FOUR = new OperatorIndex(4);
    private final OperatorIndex FIVE = new OperatorIndex(5);
    private final OperatorIndex SIX = new OperatorIndex(6);

    private Map<Integer, Set<OperatorIndex>> carrierLookup = new HashMap<>() {{
        put(1, Set.of(ONE, THREE));
        put(2, Set.of(ONE, THREE));
        put(3, Set.of(ONE, FOUR));
        put(4, Set.of(ONE, FOUR));
        put(5, Set.of(ONE, THREE, FIVE));
        put(6, Set.of(ONE, THREE, FIVE));
        put(7, Set.of(ONE, THREE));
        put(8, Set.of(ONE, THREE));
        put(9, Set.of(ONE, THREE));
        put(10, Set.of(ONE, FOUR));
        put(11, Set.of(ONE, FOUR));
        put(12, Set.of(ONE, THREE));
        put(13, Set.of(ONE, THREE));
        put(14, Set.of(ONE, THREE));
        put(15, Set.of(ONE, THREE));
        put(16, Set.of(ONE));
        put(17, Set.of(ONE));
        put(18, Set.of(ONE));
        put(19, Set.of(ONE, FOUR, FIVE));
        put(20, Set.of(ONE, TWO, FOUR));
        put(21, Set.of(ONE, TWO, FOUR, FIVE));
        put(22, Set.of(ONE, THREE, FOUR, FIVE));
        put(23, Set.of(ONE, TWO, FOUR, FIVE));
        put(24, Set.of(ONE, TWO, THREE, FOUR, FIVE));
        put(25, Set.of(ONE, TWO, THREE, FOUR, FIVE));
        put(26, Set.of(ONE, TWO, FOUR));
        put(27, Set.of(ONE, TWO, FOUR));
        put(28, Set.of(ONE, THREE, SIX));
        put(29, Set.of(ONE, TWO, THREE, FIVE));
        put(30, Set.of(ONE, TWO, THREE, SIX));
        put(31, Set.of(ONE, TWO, THREE, FOUR, FIVE));
        put(32, Set.of(ONE, TWO, THREE, FOUR, FIVE, SIX));
    }};

    /**
     *  Gets the numbers of the carrier operators
     *  for this algorithm as a set of integers.
     */
    public Set<OperatorIndex> getCarriers() {
        return this.carrierLookup.get(this.value());
    }

    /* Original comment:
        //  first gen modulators for our purpose are the first modulator fed into a carrier,
        // where that modulator is the sole modulator for the carrier.
     */
    private Map<Integer, Set<OperatorIndex>> firstGenerationModulatorLookup = new HashMap<>() {{
        put(1, Set.of(TWO, FOUR));
        put(2, Set.of(TWO, FOUR));
        put(3, Set.of(TWO, FIVE));
        put(4, Set.of(TWO, FIVE));
        put(5, Set.of(TWO, FOUR, SIX));
        put(6, Set.of(TWO, FOUR, SIX));
        put(7, Set.of(TWO));
        put(8, Set.of(TWO));
        put(9, Set.of(TWO));
        put(10, Set.of(TWO));
        put(11, Set.of(TWO));
        put(12, Set.of(TWO));
        put(13, Set.of(TWO));
        put(14, Set.of(TWO, FOUR));
        put(15, Set.of(TWO, FOUR));
        put(16, Collections.<OperatorIndex>emptySet());
        put(17, Collections.<OperatorIndex>emptySet());
        put(18, Collections.<OperatorIndex>emptySet());
        put(19, Set.of(TWO, SIX));
        put(20, Set.of(THREE));
        put(21, Set.of(THREE, SIX));
        put(22, Set.of(TWO, SIX));
        put(23, Set.of(THREE, SIX));
        put(24, Set.of(SIX));
        put(25, Set.of(SIX));
        put(26, Set.of(THREE));
        put(27, Set.of(THREE));
        put(28, Set.of(TWO, FOUR));
        put(29, Set.of(FOUR, SIX));
        put(30, Set.of(FOUR));
        put(31, Set.of(SIX));
        put(32, Collections.<OperatorIndex>emptySet());
    }};

    public Set<OperatorIndex> getFirstGenerationModulators() {
        return this.firstGenerationModulatorLookup.get(this.value());
    }
}
