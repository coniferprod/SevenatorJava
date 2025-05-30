package com.coniferproductions.sevenator.datamodel;

import com.coniferproductions.sevenator.UInt8;
import com.coniferproductions.sevenator.sysex.UInt7;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.nio.charset.StandardCharsets;
import java.util.*;

public final class Voice {
    public static final int DATA_SIZE = 155;
    public static final int PACKED_DATA_SIZE = 128;
    public static final int OPERATOR_COUNT = 6;

    private Map<OperatorIndex, Operator> operators;
    public Algorithm algorithm;
    public Depth feedback;
    public boolean oscSync;
    public LFO lfo;
    public Envelope peg;
    public Depth pitchModulationSensitivity;
    public Transpose transpose;
    public VoiceName name;

    public Voice() {
        this.operators = new HashMap<>();
        for (int index = OperatorIndex.TYPE.first(); index <= OperatorIndex.TYPE.last(); index++) {
            this.operators.put(new OperatorIndex(index), new Operator());
        }

        this.algorithm = new Algorithm(32);
        this.feedback = new Depth(0);
        this.oscSync = false;
        this.lfo = new LFO();

        List<Rate> rates = List.of(new Rate(0), new Rate(0), new Rate(0), new Rate(0));
        List<Level> levels = List.of(new Level(50), new Level(50), new Level(50), new Level(50));
        this.peg = new Envelope(rates, levels);

        this.pitchModulationSensitivity = new Depth(0);
        this.transpose = new Transpose(0);
        this.name = new VoiceName("INIT VOICE");
    }

    public Map<OperatorIndex, Operator> getOperators() {
        return this.operators;
    }

    public Operator getOperator(OperatorIndex index) {
        if (this.operators.containsKey(index)) {
            Operator op = this.operators.get(index);
            return op;
        } else {
            System.err.println("WARNING: operator index " + index + " not found in map");
            return null;
        }
    }

    public void setOperators(Map<OperatorIndex, Operator> operators) {
        this.operators = operators;
    }

    public void setOperator(OperatorIndex index, Operator operator) {
        this.operators.put(index, operator);
    }

    public static List<UInt7> unpack(List<UInt7> data) {
        //System.out.print("packed voice data (" + data.size() + ") = "); UInt8.printList(data);

        List<UInt7> result = new ArrayList<>();

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
        UInt7 alg = data.get(offset);
        result.add(alg);
        offset += 1;

        UInt7 feedback = new UInt7(data.get(offset).getRange(0, 3));
        result.add(feedback); // feedback
        result.add(data.get(offset).getBit(3) ? UInt7.ONE : UInt7.ZERO); // osc sync
        offset += 1;

        result.addAll(LFO.unpack(data.subList(offset, offset + 5)));
        offset += 4;  // we'll use the last byte soon
        result.add(new UInt7(data.get(offset).getRange(4, 3)));  // pitch mod sens
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

    public static List<UInt7> pack(List<UInt7> data) {
        List<UInt7> result = new ArrayList<>();

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

        var byte111 = new UInt7(data.get(offset).value() // feedback
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
        result.add(new UInt7(byte116Value));
        offset += 3;

        result.add(data.get(offset));  // transpose
        offset += 1;

        // voice name
        result.addAll(data.subList(offset, offset + VoiceName.NAME_LENGTH));

        assert offset == PACKED_DATA_SIZE;
        return result;
    }

    public List<UInt7> toData() {
        List<UInt7> result = new ArrayList<>();

        for (int i = 5; i >= 0; i--) { // NOTE: reverse order!
            result.addAll(this.operators.get(i).toData());
        }

        result.addAll(this.peg.toData());

        result.add(new UInt7(this.algorithm.value() - 1));  // adjust to 0...31
        result.add(new UInt7(this.feedback.value()));
        result.add(this.oscSync ? UInt7.ONE : UInt7.ZERO);
        result.addAll(this.lfo.toData());
        result.add(new UInt7(this.pitchModulationSensitivity.value()));
        result.add(new UInt7(this.transpose.value() + 24));  // adjust -24...+24 to 0...48

        byte[] nameBytes = this.name.toString().getBytes(StandardCharsets.US_ASCII);
        List<UInt7> nameData = new ArrayList<>();
        for (int i = 0; i < nameBytes.length; i++) {
            nameData.add(new UInt7(nameBytes[i]));
        }
        result.addAll(nameData);

        assert result.size() == DATA_SIZE;

        return result;
    }

    public static Voice parse(List<UInt7> data) throws ParseException {
        //System.out.print(" (" + data.size() + "): "); UInt8.printList(data);

        // Note that the operator data is in reverse order:
        // OP6 is first, OP1 is last.
        Map<OperatorIndex, Operator> operators = new HashMap<>();
        operators.put(new OperatorIndex(6), Operator.parse(data.subList(0, 21)));
        operators.put(new OperatorIndex(5), Operator.parse(data.subList(21, 42)));
        operators.put(new OperatorIndex(4), Operator.parse(data.subList(42, 63)));
        operators.put(new OperatorIndex(3), Operator.parse(data.subList(63, 84)));
        operators.put(new OperatorIndex(2), Operator.parse(data.subList(84, 105)));
        operators.put(new OperatorIndex(1), Operator.parse(data.subList(105, 126)));

        Envelope peg = Envelope.parse(data.subList(126, 134));

        Algorithm alg = new Algorithm(data.get(134).value() + 1);  // 0...31 to 1...32

        Depth feedback = new Depth(data.get(135).value());

        // number of semitones to transpose (-24...+24) (12 = C2, value is 0...48 in SysEx)
        Transpose transpose = new Transpose(data.get(144).value() - 24);

        List<UInt7> nameData = data.subList(145, 155);
        byte[] nameBytes = new byte[nameData.size()];
        int i = 0;
        for (UInt7 b : nameData) {
            nameBytes[i] = (byte) b.value();
            i++;
        }
        // TODO: Put this in a VoiceName constructor
        VoiceName name = new VoiceName(new String(nameBytes, StandardCharsets.UTF_8));

        LFO lfo = LFO.parse(data.subList(137, 143));

        Voice voice = new Voice();
        voice.operators = operators;
        voice.peg = peg;
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

        Element operatorsElement = document.createElement("operators");
        List<OperatorIndex> operatorIndices = new ArrayList<>(operators.keySet());
        for (OperatorIndex index : operatorIndices) {
            operatorsElement.appendChild(this.operators.get(index).toXML(document));
        }
        element.appendChild(operatorsElement);

        element.appendChild(this.peg.toXMLNamed(document, "peg"));
        element.appendChild(this.lfo.toXMLNamed(document, "lfo"));

        return element;
    }
}
