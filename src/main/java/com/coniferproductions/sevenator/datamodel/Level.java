package com.coniferproductions.sevenator.datamodel;

import com.coniferproductions.sevenator.RangedInteger;

public final class Level extends RangedInteger {
    public static final int DEFAULT_VALUE = 0;

    public Level(int value) {
        super(0, 99, value);
    }
    public Level() { this(DEFAULT_VALUE); }

    public static final Level TYPE = new Level(DEFAULT_VALUE);
}
