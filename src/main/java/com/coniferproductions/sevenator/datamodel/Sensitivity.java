package com.coniferproductions.sevenator.datamodel;

import com.coniferproductions.sevenator.RangedInteger;

/// Amplitude modulation sensitivity (0...3)
public final class Sensitivity extends RangedInteger {
    public static final int DEFAULT_VALUE = 0;

    public Sensitivity() {
        this(DEFAULT_VALUE);
    }
    public Sensitivity(int value) {
        super(0, 3, value);
    }

    public static final Sensitivity TYPE = new Sensitivity(DEFAULT_VALUE);
}
