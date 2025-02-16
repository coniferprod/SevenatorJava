package com.coniferproductions.sevenator;

import java.util.List;
import java.util.ArrayList;

public class Message {
    public static UInt8 SYSEX_INITIATOR = new UInt8(0xf0);
    public static UInt8 SYSEX_TERMINATOR = new UInt8(0xf7);

    private Manufacturer manufacturer;
    private List<UInt8> payload;

    public Message(Manufacturer manufacturer, byte[] payload) {
        this.manufacturer = manufacturer;

        this.payload = new ArrayList<>();
        for (byte b : payload) {
            this.payload.add(new UInt8(b));
        }
    }

    public static Message parse(List<UInt8> data) {
        boolean ok = false;
        ok = data.get(0).equals(SYSEX_INITIATOR);
        int lastIndex = data.size() - 1;
        ok = ok && (data.get(lastIndex).equals(SYSEX_TERMINATOR));
        if (!ok) {
            throw new IllegalArgumentException("SysEx data must start with F0H and end with F7H");
        }

        List<UInt8> manufacturerData = data.subList(1, 4);
        byte[] manufacturerBytes = new byte[manufacturerData.size()];
        for (int i = 0; i < manufacturerData.size(); i++) {
            manufacturerBytes[i] = Integer.valueOf(manufacturerData.get(i).value()).byteValue();
        }

        Manufacturer manufacturer = new Manufacturer(manufacturerBytes);
        List<UInt8> payloadData = data.subList(1 + manufacturer.getSize(), data.size() - 1);
        byte[] payloadBytes = new byte[payloadData.size()];
        for (int i = 0; i < payloadData.size(); i++) {
            payloadBytes[i] = Integer.valueOf(payloadData.get(i).value()).byteValue();
        }
        Message message = new Message(manufacturer, payloadBytes);
        return message;
    }

    public Manufacturer getManufacturer() {
        return this.manufacturer;
    }

    public List<UInt8> getPayload() {
        return this.payload;
    }

    public static void main(String[] args) {
        List<UInt8> data = new ArrayList<UInt8>();
        //data.add(new UInt8(0));
        data.add(new UInt8(0xf0));
        data.add(new UInt8(0x43));
        data.add(new UInt8(0));
        data.add(new UInt8(0));
        data.add(new UInt8(0));
        data.add(new UInt8(0));
        data.add(new UInt8(0));
        data.add(new UInt8(0xf7));
        Message message = Message.parse(data);
        System.out.println("manufacturer size = " + message.getManufacturer().getSize());
        System.out.println("manufacturer = " + message.getManufacturer());
        System.out.println("message payload = " + message.getPayload().size() + " bytes");

    }
}
