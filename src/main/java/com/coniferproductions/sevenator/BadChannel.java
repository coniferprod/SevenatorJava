package com.coniferproductions.sevenator;

public final class BadChannel extends RangedInteger {
    public BadChannel() {
        this(1);
    }

    public BadChannel(int value) {
        super(16, 1, value);
    }
}
