package com.coniferproductions.sevenator.datamodel;

import com.coniferproductions.sevenator.RangedInteger;

public final class Algorithm extends RangedInteger {
    public Algorithm(int value) {
        super(1, 32, value);
    }

    public static final Algorithm TYPE = new Algorithm(32);
}
