package com.coniferproductions.sevenator;

import java.util.HashMap;
import java.util.Map;

public class Manufacturer {
    private int b1 = 0;
    private int b2 = 0;
    private int b3 = 0;

    private enum Kind {
        STANDARD, EXTENDED
    }

    private Kind kind = Kind.STANDARD;

    public Manufacturer(byte[] data) {
        if (data.length < 1) {
            throw new IllegalArgumentException("Manufacturer data must have at least one byte");
        }

        this.b1 = data[0] & 0xFF;
        this.kind = Kind.STANDARD;
        if (data[0] == 0x00) {
            this.kind = Kind.EXTENDED;
            if (data.length < 3) {
                throw new IllegalArgumentException("Extended manufacturer data must have three bytes");
            }
            this.b2 = data[1];
            this.b3 = data[2];
        }
    }

    public Kind getKind() {
        return this.kind;
    }

    public int getSize() {
        if (this.kind == Kind.STANDARD) {
            return 1;
        } else {
            return 3;
        }
    }

    @Override
    public String toString() {
        String value = Manufacturer.names.get(getIdentifier());
        if (value != null) {
            return value;
        }
        return "unknown";
    }

    public String getIdentifier() {
        if (this.kind == Kind.STANDARD) {
            return String.format("%02X", this.b1);
        }

        return String.format("%02X%02X%02X", this.b1, this.b2, this.b3);
    }

    private static Map<String, String> names = new HashMap<>();

    static {
        names.put("00003B", "Mark Of The Unicorn");

        names.put("40", "Kawai Musical Instruments MFG. CO. Ltd");
        names.put("41", "Roland Corporation");
        names.put("42", "Korg Inc.");
        names.put("43", "Yamaha Corporation");
        names.put("44", "Casio Computer Co. Ltd");

        names.put("7D", "Development / Non-Commercial");
        // Not assigned: 45H, 49H, 4AH, 4DH, 4F, 53H, 58H, 5BH, 5DH, 5EH
    }

    public static void main(String[] args) {
        Manufacturer m;

        byte[] standardData = { 0x43 };
        m = new Manufacturer(standardData);
        System.out.println(String.format("%s %s %s", m.getKind(), m.getIdentifier(), m));

        byte[] extendedData = { 0x00, 0x00, 0x3B };
        m = new Manufacturer(extendedData);
        System.out.println(String.format("%s %s %s", m.getKind(), m.getIdentifier(), m));

        // Correctly throws IllegalArgumentException
        //byte[] tooLittleData = {};
        //m = new Manufacturer(tooLittleData);

        // Correctly throws IllegalArgumentException
        //byte[] notEnoughExtendedData = { 0x00, 0x00 };
        //m = new Manufacturer(notEnoughExtendedData);
    }
}
