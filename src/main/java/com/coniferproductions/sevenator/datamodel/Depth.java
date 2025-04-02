package com.coniferproductions.sevenator.datamodel;

import com.coniferproductions.sevenator.RangedInteger;

/** Depth (0...7) for keyboard rate scaling,
 *  key velocity sensitivity, feedback, pitch mod sensitivity.
 */
public final class Depth extends RangedInteger {
    public static final int DEFAULT_VALUE = 0;

    public Depth(int value) {
        super(0, 7, value);
    }

    public Depth() {
        this(DEFAULT_VALUE);
    }
}
