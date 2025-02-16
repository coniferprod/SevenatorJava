package com.coniferproductions.sevenator;

public final class UInt8 extends RangedInteger {
    public UInt8(int value) {
        super(0, 255, value & 0xFF);
    }

    public static final UInt8 TYPE = new UInt8(0);
}
