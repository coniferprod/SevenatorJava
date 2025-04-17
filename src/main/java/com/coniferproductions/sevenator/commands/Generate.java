package com.coniferproductions.sevenator.commands;

import com.coniferproductions.sevenator.datamodel.Cartridge;
import com.coniferproductions.sevenator.datamodel.Operator;
import com.coniferproductions.sevenator.datamodel.OperatorIndex;
import com.coniferproductions.sevenator.datamodel.Voice;
import com.coniferproductions.sevenator.generators.Generator;
import com.coniferproductions.sevenator.generators.Synthmata;
import com.coniferproductions.sevenator.sysex.Loader;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@CommandLine.Command(name = "generate")
public class Generate implements Runnable {
    @Override
    public void run() {
        Generator g = new Synthmata();
        Voice voice = g.makeRandomVoice();
        System.out.println("algorithm = " + voice.algorithm.value());
        Map<OperatorIndex, Operator> operators = voice.getOperators();
        List<OperatorIndex> operatorIndices = new ArrayList<>(operators.keySet());
        for (OperatorIndex index : operatorIndices) {
            Operator op = operators.get(index);
            System.out.println("op " + index.value() + " level = " + op.level.value());
        }
    }

}
