package com.coniferproductions.sevenator;

public enum Octave {
    YAMAHA(-1),
    ROLAND(-2);

    private final int offset;

    Octave(int offset) {
        this.offset = offset;
    }

    public int offset() {
        return this.offset;
    }
}
