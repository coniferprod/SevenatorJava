package com.coniferproductions.sevenator.sysex;

import com.coniferproductions.sevenator.UInt8;

import java.util.ArrayList;
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

    public Header() {
        this.channel = new Channel(1);
        this.format = Format.CARTRIDGE;
        this.substatus = 0;
        this.byteCount = 4096;
    }

    public Header(Channel channel, Format format) {
        this.channel = channel;
        this.format = format;
        this.substatus = 0;
        this.byteCount = switch (format) {
            case VOICE -> 155;
            case CARTRIDGE -> 4096;
        };
    }

    public static Header parse(List<UInt7> data) {
        UInt7 byteCountMSB = data.get(2);
        UInt7 byteCountLSB = data.get(3);
        UInt7 channelByte = new UInt7(data.get(0).value() & 0b00001111);
        if (!Channel.TYPE.contains(channelByte.value() + 1)) {
            throw new IllegalArgumentException("MIDI channel must be 1...16");
        }

        Header header = new Header();

        UInt7 formatByte = data.get(1);
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("Header[channel=%d,format=%s]", this.channel.value(), this.format.toString()));

        return sb.toString();
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

    public int getDataSize() { return 4; }

    public List<UInt7> toData() {
        List<UInt7> result = new ArrayList<>();

        result.add(new UInt7(this.channel.value() - 1));  // adjust to 0...15
        result.add(new UInt7(this.format.format()));
        switch (this.format) {
            case VOICE:
                result.add(new UInt7(0x01));
                result.add(new UInt7(0x1B));
                break;
            case CARTRIDGE:
                result.add(new UInt7(0x20));
                result.add(new UInt7(0x00));
                break;
        }

        return result;
    }
}
