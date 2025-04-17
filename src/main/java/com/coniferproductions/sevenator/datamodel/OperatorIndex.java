package com.coniferproductions.sevenator.datamodel;

import com.coniferproductions.sevenator.RangedInteger;

public class OperatorIndex extends RangedInteger {
    public OperatorIndex(int value) {
        super(1, Voice.OPERATOR_COUNT, value);
    }
    // no default value constructor, on purpose

    public static final OperatorIndex TYPE = new OperatorIndex(1);
}
