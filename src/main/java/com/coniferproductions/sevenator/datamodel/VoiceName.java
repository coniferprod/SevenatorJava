package com.coniferproductions.sevenator.datamodel;

import java.util.List;
import java.util.Objects;
import java.util.Random;

public final class VoiceName {
    public static final int NAME_LENGTH = 10;

    private String value;

    public VoiceName() {
        this(makeRandomPhrase(5));
    }

    public VoiceName(String name) {
        Objects.requireNonNull(name);
        if (name.length() != NAME_LENGTH) {
            throw new IllegalArgumentException(
                    String.format("Voice name must be exactly %d characters", NAME_LENGTH));
        }
        this.value = name;
    }

    // Makes a random syllable from a consonant and a vowel.
    // The result is not linguistically correct.
    public static String makeRandomPhrase(int count) {
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < count; i++) {
            sb.append(makeRandomSyllable());
        }

        return sb.toString();
    }

    public static String makeRandomSyllable() {
        // Japanese vowels and consonants. See https://www.lingvozone.com/Japanese.
        final List<Character> consonants = List.of(
                'k', 's', 't', 'n', 'h', 'm', 'y', 'r', 'w', 'g', 'z', 'd', 'b', 'p');
        final List<Character> vowels = List.of('a', 'i', 'u', 'e', 'o');

        Random rand = new Random();
        Character consonant = consonants.get(rand.nextInt(consonants.size()));
        Character vowel = vowels.get(rand.nextInt(vowels.size()));

        StringBuffer sb = new StringBuffer();
        sb.append(consonant);
        sb.append(vowel);
        return sb.toString();
    }

    @Override
    public String toString() {
        return this.value.toString();
    }
}
