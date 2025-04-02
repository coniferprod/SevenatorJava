package com.coniferproductions.sevenator.datamodel;

import com.coniferproductions.sevenator.UInt8;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

public class Operator {
    public enum Mode {
        RATIO,
        FIXED,
    };

    public Envelope eg;
    public KeyboardLevelScaling keyboardLevelScaling;
    public Depth keyboardRateScaling;
    public Sensitivity amplitudeModulationSensitivity;
    public Depth keyVelocitySensitivity;
    public Level outputLevel;
    public Mode mode;
    public Coarse coarse;
    public Level fine;
    public Detune detune;

    public static Operator parse(List<UInt8> data) throws ParseException {
        System.out.print("OP data = "); UInt8.printList(data);
        Envelope eg = Envelope.parse(data.subList(0, 8));
        KeyboardLevelScaling kls = KeyboardLevelScaling.parse(data.subList(8, 13));
        Depth keyboardRateScaling = new Depth(data.get(13).value());
        Sensitivity ams = new Sensitivity(data.get(14).value());
        Depth kvs = new Depth(data.get(15).value());
        Level level = new Level(data.get(16).value());
        Mode mode = data.get(17).equals(UInt8.ONE) ? Mode.FIXED : Mode.RATIO;
        Coarse coarse = new Coarse(data.get(18).value());
        Level fine = new Level(data.get(19).value());
        Detune detune = new Detune(data.get(20).value() - 7);

        // TODO: check adjustments to raw data!

        Operator op = new Operator();
        op.eg = eg;
        op.keyboardLevelScaling = kls;
        op.keyboardRateScaling = keyboardRateScaling;
        op.amplitudeModulationSensitivity = ams;
        op.keyVelocitySensitivity = kvs;
        op.outputLevel = level;
        op.mode = mode;
        op.coarse = coarse;
        op.fine = fine;
        op.detune = detune;

        return op;
    }

    public static List<UInt8> pack(List<UInt8> data) {
        List<UInt8> result = new ArrayList<>();

        int offset = 0;

        // Copy the EG bytes as is.
        result.addAll(data.subList(0, 8));

        // KLS breakpoint, left and right depths:
        result.add(data.get(8));
        result.add(data.get(9));
        result.add(data.get(10));

        result.add(new UInt8(data.get(11).value() | (data.get(12).value() << 2)));
        result.add(new UInt8(data.get(13).value() | (data.get(20).value() << 3)));
        result.add(new UInt8(data.get(14).value() | (data.get(15).value() << 2)));
        result.add(data.get(16));
        result.add(new UInt8(data.get(17).value() | (data.get(18).value()) << 1));  // coarse + mode
        result.add(data.get(19));  // fine

        assert result.size() == 17;

        return result;
    }

    public static List<UInt8> unpack(List<UInt8> data) {
        List<UInt8> result = new ArrayList<>();

        // EG data is unpacked
        result.addAll(data.subList(0, 8));

        // KLS
        result.add(data.get(8));  // BP
        result.add(data.get(9));  // LD
        result.add(data.get(10)); // RD

        result.add(new UInt8(data.get(11).getRange(0, 2)));  // LC
        result.add(new UInt8(data.get(11).getRange(2, 2)));  // RC

        result.add(new UInt8(data.get(12).getRange(0, 3)));  // RS
        result.add(new UInt8(data.get(13).getRange(0, 2)));  // AMS
        result.add(new UInt8(data.get(13).getRange(2, 3)));  // KVS

        result.add(new UInt8(data.get(14).value()));  // output level

        result.add(data.get(15).getBit(0) ? UInt8.ONE : UInt8.ZERO);  // osc mode

        result.add(new UInt8(data.get(15).getRange(1, 5))); // coarse
        result.add(new UInt8(data.get(16).value())); // fine
        result.add(new UInt8(data.get(12).getRange(3, 4))); // detune

        return result;
    }

    public List<UInt8> toData() {
        List<UInt8> result = new ArrayList<>();

        result.addAll(this.eg.toData());
        result.addAll(this.keyboardLevelScaling.toData());
        result.add(new UInt8(this.keyboardRateScaling.value()));
        result.add(new UInt8(this.amplitudeModulationSensitivity.value()));
        result.add(new UInt8(this.keyVelocitySensitivity.value()));
        result.add(new UInt8(this.outputLevel.value()));
        result.add(new UInt8(this.mode.ordinal()));
        result.add(new UInt8(this.coarse.value()));
        result.add(new UInt8(this.fine.value()));
        result.add(new UInt8(this.detune.value())); // 0 = detune -7, 7 = 0, 14 = +7

        assert result.size() == 21;

        return result;
    }

    public Element toXML(Document document) {
        Element element = document.createElement("operator");

        element.setAttribute("level", Integer.toString(this.outputLevel.value()));
        element.setAttribute("mode", this.mode.toString().toLowerCase());
        element.setAttribute("coarse", Integer.toString(this.coarse.value()));
        element.setAttribute("fine", Integer.toString(this.fine.value()));
        element.setAttribute("detune", Integer.toString(this.detune.value()));
        element.setAttribute("amplitudeModulationSensitivity", Integer.toString(this.amplitudeModulationSensitivity.value()));
        element.setAttribute("keyVelocitySensitivity", Integer.toString(this.keyVelocitySensitivity.value()));
        element.setAttribute("keyboardRateScaling", Integer.toString(this.keyboardRateScaling.value()));

        element.appendChild(this.eg.toXMLNamed(document, "eg"));
        element.appendChild(this.keyboardLevelScaling.toXML(document));

        return element;
    }
}
