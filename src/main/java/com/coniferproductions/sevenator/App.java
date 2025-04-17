package com.coniferproductions.sevenator;

import com.coniferproductions.sevenator.commands.Dump;
import com.coniferproductions.sevenator.commands.Export;
import com.coniferproductions.sevenator.commands.Import;
import com.coniferproductions.sevenator.commands.Generate;
import com.coniferproductions.sevenator.datamodel.Cartridge;
import com.coniferproductions.sevenator.sysex.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "sevenator", subcommands = { Dump.class, Export.class, Import.class, Generate.class }, description = "Manages Yamaha DX7 cartridges")
public class App {
    public static final String LOGGER_NAME = "com.coniferproductions.sevenator";
    private static System.Logger logger = System.getLogger(LOGGER_NAME);

    private Cartridge cartridge;

    public App() {
        this.cartridge = new Cartridge();

        // Hard-code XML document load for testing
        /*
        Path xmlFile = Paths.get(System.getProperty("user.home") + "/tmp/rom1a.xml");
        try {
            loadXmlDocument(xmlFile); // sets the cartridge from the parsed document
        } catch (ParserConfigurationException | IOException ex) {
            logger.log(ERROR, "Error loading XML: " + ex.getMessage());
        } catch (SAXException se) {
            logger.log(ERROR, "Error parsing XML: " + se.getMessage());
        }
        */
    }


    private void saveCartridge(Path path) {
        byte[] fileData = UInt8.byteArrayFromList(this.cartridge.toData());
        try {
            Files.write(path, fileData);
        } catch (IOException ex) {
            System.err.println("Error writing file: " + ex.getLocalizedMessage());
        }
    }

    private void exportCartridge(Path path) {
        String xml = this.cartridge.toXML();
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

    public static void main(String[] args) {
        int exitCode = new CommandLine(new App()).execute(args);
        System.exit(exitCode);
    }
}
