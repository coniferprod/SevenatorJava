package com.coniferproductions.sevenator;

import java.util.List;

public class Header {
    public enum Format {
        VOICE(0),
        CARTRIDGE(9);

        private final int format;

        Format(int format) {
            this.format = format;
        }

        public int format() {
            return this.format;
        }
    }

    private int substatus;  // 0=voice/cartridge, 1=parameter
    private Channel channel;
    private Format format;
    private Short byteCount;  // 14-bit number distributed evenly over two bytes
    // voice=155 (00000010011011 = 0x009B, appears as "01 1B")
    // cartridge=4096 (01000000000000 = 0x1000, appears as "20 00")

    public static Header parse(List<UInt8> data) {
        UInt8 byteCountMSB = data.get(2);
        UInt8 byteCountLSB = data.get(3);
        UInt8 channelByte = new UInt8(data.get(0).value() & 0b00001111);
        if (!Channel.TYPE.contains(channelByte.value() + 1)) {
            throw new IllegalArgumentException("MIDI channel must be 1...16");
        }

        Header header = new Header();

        UInt8 formatByte = data.get(1);
        if (formatByte.value() == 0) {
            header.format = Format.VOICE;
        } else if (formatByte.value() == 9) {
            header.format = Format.CARTRIDGE;
        } else {
            throw new IllegalArgumentException("Not a voice or a cartridge!");
        }

        header.substatus = ((data.get(0).value() >> 4) & 0b00000111);
        header.channel = new Channel(channelByte.value() + 1);
        switch (header.format) {
            case VOICE:
                header.byteCount = 155;
                break;
            case CARTRIDGE:
                header.byteCount = 4069;
                break;
        }

        return header;
    }

    public Format getFormat() {
        return this.format;
    }

    public Short getByteCount() {
        return this.byteCount;
    }

    public Channel getChannel() {
        return this.channel;
    }
}
