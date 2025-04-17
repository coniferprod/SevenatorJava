package com.coniferproductions.sevenator.commands;

import com.coniferproductions.sevenator.UInt8;
import com.coniferproductions.sevenator.datamodel.Cartridge;
import com.coniferproductions.sevenator.sysex.Loader;
import picocli.CommandLine;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@CommandLine.Command(name = "export")
public class Export implements Runnable {
    @CommandLine.Parameters(arity = "1", paramLabel = "INPUTFILE")
    Path inputFile;

    @CommandLine.Parameters(arity = "1", paramLabel = "OUTPUTFILE")
    Path outputFile;

    @Override
    public void run() {
        Cartridge cartridge = Loader.loadCartridge(inputFile);
        exportCartridge(outputFile, cartridge);
    }

    private void exportCartridge(Path path, Cartridge cartridge) {
        String xml = cartridge.toXML();
        try {
            BufferedWriter writer = new BufferedWriter(
                    new FileWriter(path.toString()));
            writer.write(xml);
            writer.close();
        } catch (FileNotFoundException fnfe) {
            System.err.println("Error writing XML to file: " + fnfe.getLocalizedMessage());
        } catch (IOException ioe) {
            System.err.println("Error writing XML to file: " + ioe.getLocalizedMessage());
        }
    }
}
