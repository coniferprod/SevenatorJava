package com.coniferproductions.sevenator;

import java.util.List;

public final class UInt8 extends RangedInteger {
    public UInt8(int value) {
        super(0, 255, value & 0xFF);
    }

    public UInt8() {
        this(0);
    }

    public UInt8(String bitString) {
        this(Integer.valueOf(bitString, 2));
    }

    // Gets the value as a string of bits
    public String toBitString() {
        String bs = Integer.toBinaryString(this.value());
        String pbs = String.format("%8s", bs).replace(' ', '0');  // left pad to exactly eight bits
        //StringBuilder sb = new StringBuilder(pbs);
        //sb.reverse();
        //return sb.toString();
        return pbs;
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

    public boolean getBit(int at) {
        String bs = this.toBitString();
        StringBuilder sb = new StringBuilder(bs);
        sb.reverse();
        String bs2 = sb.toString();
        return bs2.charAt(at) == '1';
    }

    public UInt8 setRange(int start, int length, int value) {
        String bs = this.toBitString();

        String rev = new StringBuilder(bs).reverse().toString();

        UInt8 result = new UInt8();
        return result;
    }

    public static final UInt8 TYPE = new UInt8(0);

    public static final UInt8 ZERO = new UInt8(0);
    public static final UInt8 ONE = new UInt8(1);

    public static void printList(List<UInt8> data) {
        StringBuffer sb = new StringBuffer();
        for (UInt8 b : data) {
            sb.append(String.format("%02X ", b.value()));
        }
        System.out.println(sb.toString());
    }

    public static UInt8 checksum(List<UInt8> data) {
        // Copy the data into a byte array
        byte[] bytes = new byte[data.size()];
        int index = 0;
        for (UInt8 b : data) {
            bytes[index] = (byte) b.value();
            index++;
        }

        // Compute the checksum
        int checksum = 0;
        for (int i = 0; i < bytes.length; i++) {
            checksum = (checksum + bytes[i]) & 0x7F;
        }
        return new UInt8((0x80 - checksum) & 0x7F);
    }
}
