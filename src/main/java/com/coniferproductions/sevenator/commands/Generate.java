package com.coniferproductions.sevenator.commands;

import com.coniferproductions.sevenator.datamodel.*;
import com.coniferproductions.sevenator.generators.Generator;
import com.coniferproductions.sevenator.generators.Synthmata;
import com.coniferproductions.sevenator.sysex.Loader;
import picocli.CommandLine;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@CommandLine.Command(name = "generate")
public class Generate implements Runnable {
    @CommandLine.Parameters(arity = "1", paramLabel = "OUTPUTFILE")
    Path outputFile;

    @Override
    public void run() {
        Generator gen = new Synthmata();

        Cartridge cartridge = new Cartridge();
        for (int i = 0; i < Cartridge.VOICE_COUNT; i++) {
            Voice voice = gen.makeRandomVoice();
            voice.name = new VoiceName(VoiceName.makeRandomPhrase(5).toUpperCase());
            cartridge.voices.set(i, voice);
        }

        String xml = cartridge.toXML();

        try {
            BufferedWriter writer = new BufferedWriter(
                    new FileWriter(outputFile.toString()));
            writer.write(xml);
            writer.close();
        } catch (FileNotFoundException fnfe) {
            System.err.println("Error writing XML to file: " + fnfe.getLocalizedMessage());
        } catch (IOException ioe) {
            System.err.println("Error writing XML to file: " + ioe.getLocalizedMessage());
        }
    }
}
