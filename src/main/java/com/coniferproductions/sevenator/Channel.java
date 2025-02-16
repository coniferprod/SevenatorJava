package com.coniferproductions.sevenator;

public final class Channel extends RangedInteger {
    public Channel(int value) {
        super(1, 16, value);
    }

    // No redefinition of equals() since it is in the superclass.

    /*
    @Override
    public boolean equals(Object o) {
        // Identical references?
        if (o == this) {
            return true;
        }

        // Correct type and non-null?
        if (!(o instanceof Channel)) return false;

        // Cast to our type.
        Channel that = (Channel) o;

        return RangedInteger.hasEqualFields(this, that);
    }
    */


    // Make one instance of this class as a static member,
    // so that you don't need to make an instance whenever you call
    // first(), last() or contains().
    public static final Channel TYPE = new Channel(1);
}
