package com.coniferproductions.sevenator;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class RangedIntegerTest {
    static final class Grade extends RangedInteger {
        public Grade(int value) {
            super(1, 5, value);
        }

        public static final Grade TYPE = new Grade(1);
    }

    private static Grade grade;

    @BeforeAll
    static void makeGrade() {
         grade = new Grade(4);
    }

    @Test
    public void firstIsCorrect() {
        assertTrue(grade.first() == 1);
    }

    @Test
    public void lastIsCorrect() {
        assertTrue(grade.last() == 5);
    }

    @Test
    public void valueIsCorrect() {
        assertTrue(grade.value() == 4);
    }

    @Test
    public void rejectsIncorrectValue() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new Grade(6));
    }

    @Test
    public void rejectsFirstLargerThanLast() {
        class BadGrade extends RangedInteger {
            public BadGrade(int value) {
                super(5, 1, value);
            }
        }

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new BadGrade(4));
    }
}
