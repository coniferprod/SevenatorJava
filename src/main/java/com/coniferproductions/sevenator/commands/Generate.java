package com.coniferproductions.sevenator.commands;

import com.coniferproductions.sevenator.datamodel.Cartridge;
import com.coniferproductions.sevenator.datamodel.Operator;
import com.coniferproductions.sevenator.datamodel.Voice;
import com.coniferproductions.sevenator.generators.Generator;
import com.coniferproductions.sevenator.generators.Synthmata;
import com.coniferproductions.sevenator.sysex.Loader;
import picocli.CommandLine;

@CommandLine.Command(name = "generate")
public class Generate implements Runnable {
    @Override
    public void run() {
        Generator g = new Synthmata();
        Voice voice = g.makeRandomVoice();
        System.out.println("algorithm = " + voice.algorithm.value());
        int i = 1;
        for (Operator op : voice.getOperators()) {
            System.out.println("op " + i + " level = " + op.level.value());
            i++;
        }
    }

}
