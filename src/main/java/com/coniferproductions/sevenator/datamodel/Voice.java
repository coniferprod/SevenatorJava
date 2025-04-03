package com.coniferproductions.sevenator.datamodel;

import com.coniferproductions.sevenator.UInt8;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Voice {
    public static final int DATA_SIZE = 155;
    public static final int PACKED_DATA_SIZE = 128;
    public static final int OPERATOR_COUNT = 6;

    private List<Operator> operators;
    public Algorithm algorithm;
    public Depth feedback;
    public boolean oscSync;
    public LFO lfo;
    public Envelope pitchEnvelope;
    public Depth pitchModulationSensitivity;
    public Transpose transpose;
    public VoiceName name;

    public Voice() {
        this.operators = new ArrayList<>();

        for (int i = 0; i < OPERATOR_COUNT; i++) {
            this.operators.add(new Operator());
        }

        this.algorithm = new Algorithm(32);
        this.feedback = new Depth(0);
        this.oscSync = false;
        this.lfo = new LFO();
        this.pitchEnvelope = new Envelope();
        this.pitchModulationSensitivity = new Depth(0);
        this.transpose = new Transpose(0);
        this.name = new VoiceName("INIT VOICE");
    }

    public static List<UInt8> unpack(List<UInt8> data) {
        //System.out.print("packed voice data (" + data.size() + ") = "); UInt8.printList(data);

        List<UInt8> result = new ArrayList<>();

        int offset = 0;
        for (int i = 5; i >= 0; i--) {  // NOTE: reverse order!
            final int size = 17;  // packed operator data length
            result.addAll(Operator.unpack(data.subList(offset, offset + size)));
            offset += size;
        }

        // Now offset should be at the start of the pitch EG.
        assert offset == 102;

        result.addAll(data.subList(offset, offset + 8));  // PEG = 4xrate + 4xlevel
        offset += 8;

        // Algorithm
        assert offset == 110;
        UInt8 alg = data.get(offset);
        result.add(alg);
        offset += 1;

        UInt8 feedback = new UInt8(data.get(offset).getRange(0, 3));
        result.add(feedback); // feedback
        result.add(data.get(offset).getBit(3) ? UInt8.ONE : UInt8.ZERO); // osc sync
        offset += 1;

        result.addAll(LFO.unpack(data.subList(offset, offset + 5)));
        offset += 4;  // we'll use the last byte soon
        result.add(new UInt8(data.get(offset).getRange(4, 3)));  // pitch mod sens
        offset += 1;

        result.add(data.get(offset)); // transpose
        offset += 1;

        // Voice name (last 10 characters)
        result.addAll(data.subList(offset, offset + VoiceName.NAME_LENGTH));
        offset += VoiceName.NAME_LENGTH;

        assert offset == Voice.PACKED_DATA_SIZE;
        assert result.size() == Voice.DATA_SIZE;
        return result;
    }

    public static List<UInt8> pack(List<UInt8> data) {
        List<UInt8> result = new ArrayList<>();

        int offset = 0;

        // The operator data is already in reverse order (OP6 first),
        // so just take each chunk and pack it.

        final int size = 21;
        for (int i = 0; i < OPERATOR_COUNT; i++) {
            var operatorData = data.subList(offset, offset + size);
            var packedOperatorData = Operator.pack(operatorData);
            result.addAll(packedOperatorData);
            offset += size;
        }

        assert offset == OPERATOR_COUNT * size;

        // Copy the pitch EG as is.
        result.addAll(data.subList(offset, offset + 8));
        offset += 8;

        result.add(data.get(offset));  // algorithm
        offset += 1;

        var byte111 = new UInt8(data.get(offset).value() // feedback
                | (data.get(offset + 1).value() << 3));  // osc sync
        result.add(byte111);
        offset += 2;

        // LFO speed, delay, PMD, AMD
        result.addAll(data.subList(offset, offset + 4));
        offset += 4;

        int byte116Value = data.get(offset).value();  // LFO sync
        int waveformValue = data.get(offset + 1).value();
        int pmsValue = data.get(offset + 2).value();

        // Thought there might be an error in the Dexed description of packed data:
        byte116Value |= (waveformValue << 1);  // LFO waveform
        byte116Value |= (pmsValue << 4);  // pitch mod sens (voice)
        // Working instead from the Dexed documentation:
        //byte116.set_bit_range(1..5, data[offset + 1]);
        //byte116.set_bit_range(5..7, data[offset + 2]);

        //println!("byte116 = {:#04x}", byte116);
        result.add(new UInt8(byte116Value));
        offset += 3;

        result.add(data.get(offset));  // transpose
        offset += 1;

        // voice name
        result.addAll(data.subList(offset, offset + VoiceName.NAME_LENGTH));

        assert offset == PACKED_DATA_SIZE;
        return result;
    }

    public List<UInt8> toData() {
        List<UInt8> result = new ArrayList<>();

        for (int i = 5; i >= 0; i--) { // NOTE: reverse order!
            result.addAll(this.operators.get(i).toData());
        }

        result.addAll(this.pitchEnvelope.toData());

        result.add(new UInt8(this.algorithm.value() - 1));  // adjust to 0...31
        result.add(new UInt8(this.feedback.value()));
        result.add(this.oscSync ? UInt8.ONE : UInt8.ZERO);
        result.addAll(this.lfo.toData());
        result.add(new UInt8(this.pitchModulationSensitivity.value()));
        result.add(new UInt8(this.transpose.value() * 12 + 24));  // adjust -2...+2 to 0...48

        byte[] nameBytes = this.name.toString().getBytes(StandardCharsets.US_ASCII);
        List<UInt8> nameData = new ArrayList<>();
        for (int i = 0; i < nameBytes.length; i++) {
            nameData.add(new UInt8(nameBytes[i]));
        }
        result.addAll(nameData);

        assert result.size() == DATA_SIZE;

        return result;
    }

    public static Voice parse(List<UInt8> data) throws ParseException {
        //System.out.print(" (" + data.size() + "): "); UInt8.printList(data);

        // Note that the operator data is in reverse order:
        // OP6 is first, OP1 is last.
        Operator op6 = Operator.parse(data.subList(0, 21));
        Operator op5 = Operator.parse(data.subList(21, 42));
        Operator op4 = Operator.parse(data.subList(42, 63));
        Operator op3 = Operator.parse(data.subList(63, 84));
        Operator op2 = Operator.parse(data.subList(84, 105));
        Operator op1 = Operator.parse(data.subList(105, 126));

        Envelope peg = Envelope.parse(data.subList(126, 134));

        Algorithm alg = new Algorithm(data.get(134).value() + 1);  // 0...31 to 1...32

        Depth feedback = new Depth(data.get(135).value());

        // number of octaves to transpose (-2...+2) (12 = C2, value is 0...48 in SysEx)
        Transpose transpose = new Transpose((data.get(144).value() - 24) / 12);

        List<UInt8> nameData = data.subList(145, 155);
        byte[] nameBytes = new byte[nameData.size()];
        int i = 0;
        for (UInt8 b : nameData) {
            nameBytes[i] = (byte) b.value();
            i++;
        }
        // TODO: Put this in a VoiceName constructor
        VoiceName name = new VoiceName(new String(nameBytes, StandardCharsets.UTF_8));

        LFO lfo = LFO.parse(data.subList(137, 143));

        Voice voice = new Voice();
        voice.operators = List.of(op1, op2, op3, op4, op5, op6);  // TODO: does this need to be mutable?
        voice.pitchEnvelope = peg;
        voice.algorithm = alg;
        voice.feedback = feedback;
        voice.oscSync = data.get(136).equals(UInt8.ONE);
        voice.lfo = lfo;
        voice.pitchModulationSensitivity = new Depth(data.get(143).value());
        voice.transpose = transpose;
        voice.name = name;

        return voice;
    }

    public Element toXML(Document document) {
        return this.toXMLNamed(document, "voice");
    }

    public Element toXMLNamed(Document document, String tagName) {
        Element element = document.createElement(tagName);
        element.setAttribute("name", this.name.toString());
        element.setAttribute("algorithm", Integer.toString(this.algorithm.value()));
        element.setAttribute("transpose", Integer.toString(this.transpose.value()));
        element.setAttribute("feedback", Integer.toString(this.feedback.value()));
        element.setAttribute("oscillatorSync", Boolean.toString(this.oscSync));
        element.setAttribute("pitchModulationSensitivity", Integer.toString(this.pitchModulationSensitivity.value()));

        element.appendChild(this.pitchEnvelope.toXMLNamed(document, "peg"));
        element.appendChild(this.lfo.toXMLNamed(document, "lfo"));

        Element operatorsElement = document.createElement("operators");
        for (Operator operator : this.operators) {
            operatorsElement.appendChild(operator.toXML(document));
        }
        element.appendChild(operatorsElement);
        return element;
    }
}
