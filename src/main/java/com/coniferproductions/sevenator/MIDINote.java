package com.coniferproductions.sevenator;

public final class MIDINote extends RangedInteger {
    public static Octave octave = Octave.ROLAND;

    public MIDINote(int value) {
        super(0, 127, value);
    }

    public String name() {
        final String[] names = { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
        int octaveOffset = MIDINote.octave.offset();
        int noteValue = this.value();

        int octaveNumber = noteValue / 12 + octaveOffset;
        String noteName = names[noteValue % 12];
        return noteName + Integer.toString(octaveNumber);
    }

    @Override
    public String toString() {
        return this.name();
    }
}
