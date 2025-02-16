package com.coniferproductions.sevenator;

public abstract class RangedInteger {
    private final int value;
    private final int first;
    private final int last;

    public RangedInteger(int first, int last, int value) {
        if (first >= last) {
            throw new IllegalArgumentException(
                String.format("Lower bound %d must be less than upper bound %d", first, last));
        }
        this.first = first;
        this.last = last;

        if (value < first || value > last) {
            throw new IllegalArgumentException(
                String.format("Value %d must be within the bounds [%d...%d]", value, first, last));
        }
        this.value = value;
    }

    public int value() {
        return value;
    }

    public int first() {
        return first;
    }

    public int last() {
        return last;
    }

    public boolean contains(int value) {
        return (value >= this.first() && value <= this.last());
    }

    @Override
    public String toString() {
        return Integer.toString(this.value);
    }

    @Override
    public boolean equals(Object o) {
        // Check if the argument is a reference to this object
        // (just a performance optimization really)
        if (o == this) return true;

        // Check that the argument has the correct type.
        // Also implicitly checks for null.
        if (!(o instanceof RangedInteger)) return false;

        // Cast the argument to the correct type.
        // This is guaranteed to succeed because the instance test passed.
        RangedInteger that = (RangedInteger) o;

        // We only care about the wrapped value, not the bounds
        return this.value == that.value;
    }
}
