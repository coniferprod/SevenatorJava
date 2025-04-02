package com.coniferproductions.sevenator;

import com.coniferproductions.sevenator.datamodel.Cartridge;
import com.coniferproductions.sevenator.datamodel.Octave;
import com.coniferproductions.sevenator.datamodel.ParseException;
import com.coniferproductions.sevenator.sysex.Channel;
import com.coniferproductions.sevenator.sysex.Header;
import com.coniferproductions.sevenator.sysex.MIDINote;
import com.coniferproductions.sevenator.sysex.Message;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import java.nio.ByteOrder;

import static java.lang.System.Logger.Level.*;

public class App {
    public static final String LOGGER_NAME = "com.coniferproductions.sevenator";
    private static System.Logger logger = System.getLogger(LOGGER_NAME);

    public static void main(String[] args) {
        List<UInt8> data = new ArrayList<>();
        try {
            byte[] contents = Files.readAllBytes(Paths.get(args[0]));

            for (byte b : contents) {
                int value = b & 0xff;
                data.add(new UInt8(value));
            }
        } catch (IOException ioe) {
            System.err.println(ioe.getLocalizedMessage());
            ioe.printStackTrace();
            System.exit(1);
        }

        Message message = Message.parse(data);
        logger.log(DEBUG, "data length = " + data.size());

        Header header = Header.parse(message.getPayload());
        logger.log(DEBUG, header);

        List<UInt8> payload = message.getPayload();
        List<UInt8> cartridgeData = payload.subList(header.getDataSize(), payload.size() - 1);

        try {
            //UInt8.printList(cartridgeData);
            Cartridge cartridge = Cartridge.parse(cartridgeData);

            String xml = cartridge.toXML();
            System.out.println(xml);

        } catch (ParseException pe) {
            System.err.println("Parse error: " + pe.getMessage());
            System.exit(1);
        }
    }

    public static void test(String arg0) {
        System.out.println("System byte order = " + ByteOrder.nativeOrder());

        Channel channel = new Channel(1);
        System.out.println("Channel value = " + channel);
        System.out.println(String.format("Channel = [%d, %d]", Channel.TYPE.first(), Channel.TYPE.last()));
        System.out.println("Channel contains -7? " + Channel.TYPE.contains(-7));
        System.out.println("Channel contains 10? " + Channel.TYPE.contains(10));

        Channel channel1 = new Channel(1);
        Channel channel2 = new Channel(2);
        if (channel1.equals(channel2)) {
            System.out.println("Same channel.");
        } else {
            System.out.println("Different channel.");
        }

        MIDINote middleC = new MIDINote(60);
        System.out.println("Middle C is MIDI note number " + middleC.value()
            + ", or " + middleC);

        for (int value = middleC.last(); value >= middleC.first(); value--) {
            MIDINote note = new MIDINote(value);
            MIDINote.octave = Octave.ROLAND;
            System.out.print(value + " = " + note + " / ");
            MIDINote.octave = Octave.YAMAHA;
            System.out.println(note);
        }
        System.out.println();

        List<UInt8> data = new ArrayList<>();
        try {
            byte[] contents = Files.readAllBytes(Paths.get(arg0));

            for (byte b : contents) {
                int value = b & 0xff;
                data.add(new UInt8(value));
            }

            System.out.printf("%02X ... %02X", contents[0], contents[contents.length - 1]);

            Message message = Message.parse(data);
            System.out.printf("%nMessage information:%n");
            System.out.printf("payload = %d bytes%n%n", message.getPayload().size());

            Header header = Header.parse(message.getPayload());
            System.out.println("Header: format = " + header.getFormat());
            System.out.println("byte count = " + header.getByteCount());
            System.out.println("channel = " + header.getChannel());

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        /*
        for (UInt8 b : data) {
            System.out.print(String.format("%02x", b.value()));
            System.out.print(" ");
        }
        System.out.println();
        */

        System.out.printf("RangedInteger subclass instances created: %d%n", RangedInteger.getCount());

        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        System.out.printf("Total memory: %10d%n", totalMemory);
        System.out.printf("Free memory:  %10d%n", freeMemory);
        System.out.printf("Used memory:  %10d%n", usedMemory);
    }
}
