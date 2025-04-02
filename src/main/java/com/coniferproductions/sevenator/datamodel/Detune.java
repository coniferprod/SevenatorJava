package com.coniferproductions.sevenator.datamodel;

import com.coniferproductions.sevenator.RangedInteger;

public class Detune extends RangedInteger {
    public static final int DEFAULT_VALUE = 0;

    public Detune(int value) {
        super(-7, 7, value);
    }
    public Detune() { this(DEFAULT_VALUE); }

    public static final Detune TYPE = new Detune(DEFAULT_VALUE);
}
