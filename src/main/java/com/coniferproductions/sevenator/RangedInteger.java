package com.coniferproductions.sevenator;

import java.util.Objects;
import java.util.Random;

/**
 * Abstract base class for an integer that conforms to a closed range.
 */
public abstract class RangedInteger {
    private final int value;
    private final int first;
    private final int last;

    /**
     * Constructs an instance with the bounds and an initial value.
     *
     * @param first the lower bound
     * @param last the upper bound
     * @param value the initial value
     */
    public RangedInteger(int first, int last, int value) {
        if (first >= last) {
            throw new IllegalArgumentException(
                String.format(
                        "Lower bound %d must be less than upper bound %d",
                        first, last));
        }
        this.first = first;
        this.last = last;

        if (value < first || value > last) {
            throw new IllegalArgumentException(
                String.format(
                        "Value %d must be within the bounds [%d...%d]",
                        value, first, last));
        }

        this.value = value;

        count++;
    }

    /**
     * Gets the current value of the wrapped integer.
     *
     * @return the wrapped integer value
     */
    public int value() {
        return value;
    }

    /**
     * Gets the lower bound of the range.
     *
     * @return the lower bound
     */
    public int first() {
        return first;
    }

    /**
     * Gets the upper bound of the range.
     *
     * @return the upper bound
     */
    public int last() {
        return last;
    }

    /**
     * Checks if the specified value is inside the bounds.
     *
     * @param value the value to check
     * @return <code>true</code> if the value is inside the bounds, <code>false</code> if not
     */
    public boolean contains(int value) {
        return (value >= this.first() && value <= this.last());
    }

    /**
     * Gets a string representation of the wrapped value.
     *
     * @return formatted value
     */
    @Override
    public String toString() {
        return Integer.toString(this.value);
    }

    /**
     * Checks for equality with this and another wrapped value.
     *
     * @param o the other value
     * @return <code>true</code> if equal, <code>false</code> if not
     */
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

    @Override
    public int hashCode() {
        return Objects.hash(this.value);
    }

    private static int count;
    public static int getCount() { return count; }

    /**
     * Generate a random integer in the range [min, max) (max is excluded).
     *
     * @param min the minimum value
     * @param max the maximum value (excsluded)
     * @return the random integer
     */
    public static int getRandomInteger(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min) + min;
    }

    public int getRandomValue() {
        return RangedInteger.getRandomInteger(this.first(), this.last());
    }
}
