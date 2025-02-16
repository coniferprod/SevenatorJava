package com.coniferproductions.sevenator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import java.nio.ByteOrder;

public class App {
    public static void main(String[] args) {
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

        //RangedInteger ri = new RangedInteger();  // abstract class, can't instantiate
        //BadChannel bc = new BadChannel(10);  // internally inconsistent, throws exception
        //Level level = new Level(100);  // initial value out of range, throws exception

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

        System.out.println(String.format("Level = [%d, %d]", Level.TYPE.first(), Level.TYPE.last()));
        Level level1 = new Level(50);
        Level level2 = new Level(50);
        if (level1.equals(level2)) {
            System.out.println("The levels are equal.");
        } else {
            System.out.println("The levels are not equal.");
        }

        List<UInt8> data = new ArrayList<>();
        try {
            byte[] contents = Files.readAllBytes(Paths.get(args[0]));

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

        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        System.out.println(String.format("Total memory: %10d", totalMemory));
        System.out.println(String.format("Free memory:  %10d", freeMemory));
        System.out.println(String.format("Used memory:  %10d", usedMemory));
    }
}
