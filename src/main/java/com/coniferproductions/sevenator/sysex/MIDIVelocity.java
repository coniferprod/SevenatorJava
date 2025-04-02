package com.coniferproductions.sevenator.sysex;

import com.coniferproductions.sevenator.RangedInteger;

public final class MIDIVelocity extends RangedInteger {
    public MIDIVelocity(int value) {
        super(0, 127, value);
    }
}
