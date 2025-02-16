package com.coniferproductions.sevenator;

public final class Level extends RangedInteger {
    public Level(int value) {
        super(0, 99, value);
    }

    public static final Level TYPE = new Level(0);
}
