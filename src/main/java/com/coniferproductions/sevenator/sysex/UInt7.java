package com.coniferproductions.sevenator.sysex;

import com.coniferproductions.sevenator.RangedInteger;
import com.coniferproductions.sevenator.UInt8;

import java.util.ArrayList;
import java.util.List;

public final class UInt7 extends RangedInteger {
    public UInt7(int value) {
        super(0, 127, value & 0x7F);
    }
    public UInt7() {
        this(0);
    }
    public UInt7(boolean value) {
        this(value ? 1 : 0);
    }

    public static final UInt7 TYPE = new UInt7(0);

    public static final UInt7 ZERO = new UInt7();
    public static final UInt7 ONE = new UInt7(1);

    public static List<UInt7> fromData(List<UInt8> data) {
        List<UInt7> result = new ArrayList<>();

        for (UInt8 b : data) {
            result.add(new UInt7(b.value()));
        }

        return result;
    }

    public static List<UInt8> toData(List<UInt7> data) {
        List<UInt8> result = new ArrayList<>();

        for (UInt7 b : data) {
            result.add(new UInt8(b.value()));
        }

        return result;
    }

    // Gets the value as a string of bits
    public String toBitString() {
        String bs = Integer.toBinaryString(this.value());
        String pbs = String.format("%7s", bs).replace(' ', '0');  // left pad to exactly seven bits
        return pbs;
    }

    public static UInt7 checksum(List<UInt7> data) {
        // Copy the data into a byte array
        byte[] bytes = new byte[data.size()];
        int index = 0;
        for (UInt7 b : data) {
            bytes[index] = (byte) b.value();
            index++;
        }

        // Compute the checksum
        int checksum = 0;
        for (int i = 0; i < bytes.length; i++) {
            checksum = (checksum + bytes[i]) & 0x7F;
        }

        return new UInt7((0x80 - checksum) & 0x7F);
    }

    public boolean getBit(int at) {
        String bs = this.toBitString();
        StringBuilder sb = new StringBuilder(bs);
        sb.reverse();
        String bs2 = sb.toString();
        return bs2.charAt(at) == '1';
    }

    // Gets the value of a range of bits from this one
    public int getRange(int start, int length) {
        String bs = this.toBitString();
        String rev = new StringBuilder(bs).reverse().toString();
        String range = rev.substring(start, start + length);
        rev = new StringBuilder(range).reverse().toString();
        int result = Integer.valueOf(rev, 2);
        //System.err.printf("getRange(%d, %d): bs = '%s', rev = '%s', result = %d%n",
        //        start, length, bs, rev, result);
        return result;
    }

}
