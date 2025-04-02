package com.coniferproductions.sevenator.datamodel;

public class ParseException extends Exception {
    private int offset;

    public ParseException(String message) {
        this(message, 0);
    }

    public ParseException(String message, int offset) {
        super(message);

        this.offset = offset;
    }

    @Override
    public String getMessage() {
        return String.format("%s (offset %d)", super.getMessage(), this.offset);
    }
}
