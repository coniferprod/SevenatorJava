package com.coniferproductions.sevenator.datamodel;

import com.coniferproductions.sevenator.RangedInteger;

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
}
