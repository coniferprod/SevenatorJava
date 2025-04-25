package com.coniferproductions.sevenator.commands;

import com.coniferproductions.sevenator.UInt8;
import com.coniferproductions.sevenator.datamodel.Cartridge;
import com.coniferproductions.sevenator.sysex.*;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@CommandLine.Command(name = "import")
public class Import implements Runnable {
    @CommandLine.Parameters(arity = "1", paramLabel = "INPUTFILE")
    Path inputFile;

    @CommandLine.Parameters(arity = "1", paramLabel = "OUTPUTFILE")
    Path outputFile;

    @Override
    public void run() {
        Cartridge cartridge = Loader.loadCartridge(inputFile);
        saveCartridge(outputFile, cartridge);
    }

    private void saveCartridge(Path path, Cartridge cartridge) {
        Header header = new Header(new Channel(1), Header.Format.CARTRIDGE);

        List<UInt7> payload = new ArrayList<>();
        payload.addAll(header.toData());
        List<UInt7> data = cartridge.toData();
        payload.addAll(data);
        payload.add(UInt7.checksum(data));

        Message message = new Message(Manufacturer.YAMAHA, payload);
        byte[] fileData = UInt8.byteArrayFromList(message.toData());
        try {
            Files.write(path, fileData);
        } catch (IOException ex) {
            System.err.println("Error writing file: " + ex.getLocalizedMessage());
        }
    }
}
