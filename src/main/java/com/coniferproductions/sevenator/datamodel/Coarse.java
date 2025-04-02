package com.coniferproductions.sevenator.datamodel;

import com.coniferproductions.sevenator.RangedInteger;

public class Coarse extends RangedInteger {
    public static final int DEFAULT_VALUE = 0;

    public Coarse(int value) {
        super(0, 31, value);
    }
    public Coarse() { this(DEFAULT_VALUE); }

    public static final Coarse TYPE = new Coarse(DEFAULT_VALUE);
}
