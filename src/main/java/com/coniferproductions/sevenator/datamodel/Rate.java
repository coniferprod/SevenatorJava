package com.coniferproductions.sevenator.datamodel;

import com.coniferproductions.sevenator.RangedInteger;

public final class Rate extends RangedInteger {
    public static final int DEFAULT_VALUE = 0;

    public Rate(int value) {
        super(0, 99, value);
    }

    public Rate() {
        this(DEFAULT_VALUE);
    }

    public static final Rate TYPE = new Rate(DEFAULT_VALUE);
}
