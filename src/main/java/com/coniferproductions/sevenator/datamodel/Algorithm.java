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

    private Map<Integer, Set<Integer>> carrierLookup = new HashMap<>() {{
        put(1, Set.of(1, 3));
        put(2, Set.of(1, 3));
        put(3, Set.of(1, 4));
        put(4, Set.of(1, 4));
        put(5, Set.of(1, 3, 5));
        put(6, Set.of(1, 3, 5));
        put(7, Set.of(1, 3));
        put(8, Set.of(1, 3));
        put(9, Set.of(1, 3));
        put(10, Set.of(1, 4));
        put(11, Set.of(1, 4));
        put(12, Set.of(1, 3));
        put(13, Set.of(1, 3));
        put(14, Set.of(1, 3));
        put(15, Set.of(1, 3));
        put(16, Set.of(1));
        put(17, Set.of(1));
        put(18, Set.of(1));
        put(19, Set.of(1, 4, 5));
        put(20, Set.of(1, 2, 4));
        put(21, Set.of(1, 2, 4, 5));
        put(22, Set.of(1, 3, 4, 5));
        put(23, Set.of(1, 2, 4, 5));
        put(24, Set.of(1, 2, 3, 4, 5));
        put(25, Set.of(1, 2, 3, 4, 5));
        put(26, Set.of(1, 2, 4));
        put(27, Set.of(1, 2, 4));
        put(28, Set.of(1, 3, 6));
        put(29, Set.of(1, 2, 3, 5));
        put(30, Set.of(1, 2, 3, 6));
        put(31, Set.of(1, 2, 3, 4, 5));
        put(32, Set.of(1, 2, 3, 4, 5, 6));
    }};

    /**
     *  Gets the numbers of the carrier operators
     *  for this algorithm as a set of integers.
     */
    public Set<Integer> getCarriers() {
        return this.carrierLookup.get(this.value());
    }
}
