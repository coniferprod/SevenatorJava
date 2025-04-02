package com.coniferproductions.sevenator.datamodel;

import com.coniferproductions.sevenator.RangedInteger;

public class Key extends RangedInteger {
    public Key() {
        this(39);
    }

    public Key(int value) {
        super(0, 99, value);
    }
}
