package com.coniferproductions.sevenator.datamodel;

import com.coniferproductions.sevenator.RangedInteger;

public final class Transpose extends RangedInteger {
    Transpose(int value) {
        super(-12, 12, value);
    }
}
