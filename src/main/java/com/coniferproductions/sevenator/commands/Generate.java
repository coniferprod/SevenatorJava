package com.coniferproductions.sevenator.commands;

import com.coniferproductions.sevenator.datamodel.*;
import com.coniferproductions.sevenator.generators.Generator;
import com.coniferproductions.sevenator.generators.Synthmata;
import com.coniferproductions.sevenator.sysex.Loader;
import picocli.CommandLine;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CommandLine.Command(name = "generate")
public class Generate implements Runnable {
    @CommandLine.Option(names = { "-p", "--parameterfile" }, paramLabel = "Parameter file", description = "Randomization parameters from file")
    Path parameterFile;

    @CommandLine.Parameters(arity = "1", paramLabel = "OUTPUTFILE")
    Path outputFile;

    @Override
    public void run() {
        boolean parametersDone = false;
        try {
            List<String> parameterLines = Files.readAllLines(parameterFile);

            Map<Synthmata.Parameter, Level> parameters = new HashMap<>();

            for (String line : parameterLines) {
                String[] parts = line.split(" ");
                String namePart = parts[0];
                String valuePart = parts[1];
                int rawValue = Integer.parseInt(valuePart);
                Level value = new Level(rawValue);
                Synthmata.Parameter parameter = Synthmata.Parameter.valueOf(namePart.toUpperCase());
                parameters.put(parameter, value);
            }

            parametersDone = true;

            Generator gen = new Synthmata(parameters);

            Cartridge cartridge = new Cartridge();
            for (int i = 0; i < Cartridge.VOICE_COUNT; i++) {
                Voice voice = gen.makeRandomVoice();
                voice.name = new VoiceName(VoiceName.makeRandomPhrase(5).toUpperCase());
                cartridge.voices.set(i, voice);
            }

            String xml = cartridge.toXML();
            BufferedWriter writer = new BufferedWriter(
                    new FileWriter(outputFile.toString()));
            writer.write(xml);
            writer.close();

        } catch (FileNotFoundException fnfe) {
            System.err.println("Error writing XML to file: " + fnfe.getLocalizedMessage());
            fnfe.printStackTrace();
        } catch (IOException ioe) {
            if (!parametersDone) {
                System.err.println("Error processing parameters: " + ioe.getLocalizedMessage());
            } else {
                System.err.println("Error writing XML to file: " + ioe.getLocalizedMessage());
            }
            ioe.printStackTrace();
        }
    }
}
