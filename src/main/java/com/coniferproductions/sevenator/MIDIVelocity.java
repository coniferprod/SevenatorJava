package com.coniferproductions.sevenator;

public final class MIDIVelocity extends RangedInteger {
    public MIDIVelocity(int value) {
        super(0, 127, value);
    }
}
