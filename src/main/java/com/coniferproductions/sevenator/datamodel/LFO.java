package com.coniferproductions.sevenator.datamodel;

import com.coniferproductions.sevenator.UInt8;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

public class LFO {
    public enum Waveform {
        TRIANGLE,
        SAW_DOWN,
        SAW_UP,
        SQUARE,
        SINE,
        SAMPLE_AND_HOLD,
    };

    public Level speed;
    public Level delay;
    public Level pmd;
    public Level amd;
    public boolean sync;
    public Waveform waveform;

    public LFO() {
        this.speed = new Level(35);
        this.delay = new Level(0);
        this.pmd = new Level(0);
        this.amd = new Level(0);
        this.sync = true;
        this.waveform = Waveform.TRIANGLE;
    }

    public static LFO parse(List<UInt8> data) throws ParseException {
        Level speed = new Level(data.get(0).value());
        Level delay = new Level(data.get(1).value());
        Level pmd = new Level(data.get(2).value());
        Level amd = new Level(data.get(3).value());
        boolean sync = data.get(4).equals(UInt8.ONE);
        Waveform waveform = switch (data.get(5).value()) {
            case 0 -> Waveform.TRIANGLE;
            case 1 -> Waveform.SAW_DOWN;
            case 2 -> Waveform.SAW_UP;
            case 3 -> Waveform.SQUARE;
            case 4 -> Waveform.SINE;
            case 5 -> Waveform.SAMPLE_AND_HOLD;
            default -> throw new ParseException("bad LFO waveform");
        };

        LFO lfo = new LFO();
        lfo.speed = speed;
        lfo.delay = delay;
        lfo.pmd = pmd;
        lfo.amd = amd;
        lfo.sync = sync;
        lfo.waveform = waveform;
        return lfo;
    }

    public static List<UInt8> unpack(List<UInt8> data) {
        List<UInt8> result = new ArrayList<>();

        result.add(data.get(0));  // LFO speed
        result.add(data.get(1));  // LFO delay
        result.add(data.get(2));  // LFO PMD
        result.add(data.get(3));  // data[3],  // LFO AMD
        result.add(data.get(4).getBit(0) ? UInt8.ONE : UInt8.ZERO);  // LFO sync
        result.add(new UInt8(data.get(4).getRange(1, 3))); // LFO waveform

        return result;
    }

    public List<UInt8> toData() {
        List<UInt8> result = new ArrayList<>();

        result.add(new UInt8(this.speed.value()));
        result.add(new UInt8(this.delay.value()));
        result.add(new UInt8(this.pmd.value()));
        result.add(new UInt8(this.amd.value()));
        result.add(this.sync ? UInt8.ONE : UInt8.ZERO);
        result.add(new UInt8(this.waveform.ordinal()));

        return result;
    }

    public Element toXMLNamed(Document document, String tagName) {
        Element element = document.createElement(tagName);

        element.setAttribute("speed", Integer.toString(this.speed.value()));
        element.setAttribute("delay", Integer.toString(this.delay.value()));
        element.setAttribute("pmd", Integer.toString(this.pmd.value()));
        element.setAttribute("amd", Integer.toString(this.amd.value()));
        element.setAttribute("sync", Boolean.toString(this.sync));

        // Replace underscore with dash to conform with the XML Schema for voice
        String waveformString = this.waveform.toString()
                .toLowerCase()
                .replace('_', '-');
        element.setAttribute("wave", waveformString);

        return element;
    }
}
