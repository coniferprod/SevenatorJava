package com.coniferproductions.sevenator.datamodel;

import com.coniferproductions.sevenator.RangedInteger;

/// Amplitude modulation sensitivity (0...3)
public final class Sensitivity extends RangedInteger {
    public Sensitivity(int value) {
        super(0, 3, value);
    }
}
