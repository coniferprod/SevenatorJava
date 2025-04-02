package com.coniferproductions.sevenator.datamodel;

import com.coniferproductions.sevenator.UInt8;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

public final class Envelope {
    public static final int RATE_COUNT = 4;
    public static final int LEVEL_COUNT = 4;

    public List<Rate> rates;
    public List<Level> levels;

    public Envelope() {
        this.rates = new ArrayList<>();
        for (int i = 0; i < RATE_COUNT; i++) {
            this.rates.add(new Rate(99));
        }

        this.levels = new ArrayList<>();
        for (int i = 0; i < LEVEL_COUNT - 1; i++) {
            this.levels.add(new Level(99));
        }
        this.levels.add(new Level(0));
    }

    public Envelope(List<Rate> rates, List<Level> levels) {
        this.rates = rates;
        this.levels = levels;
    }

    /*
    From the Yamaha DX7 Operation Manual (p. 51):
    "You can simulate an ADSR if you set the envelope as follows:
    L1=99, L2=99, L4=0, and R2=99.
    With these settings, then R1 becomes Attack time, R3 is Decay
    time, L3 is Sustain level, and R4 is Release time."
    */

    /**
     * Makes a new ADSR-style envelope.
     */
    public Envelope(Rate attack, Rate decay, Level sustain, Rate release) {
        this(List.of(attack, new Rate(99), decay, release),
                List.of(new Level(99), new Level(99), sustain, new Level(0)));
    }

    public static Envelope parse(List<UInt8> data) throws ParseException {
        List<Rate> rates = List.of(
                new Rate(data.get(0).value()),
                new Rate(data.get(1).value()),
                new Rate(data.get(2).value()),
                new Rate(data.get(3).value()));
        List<Level> levels = List.of(
                new Level(data.get(4).value()),
                new Level(data.get(5).value()),
                new Level(data.get(6).value()),
                new Level(data.get(7).value()));
        return new Envelope(rates, levels);
    }

    public Element toXMLNamed(Document document, String tagName) {
        Element element = document.createElement(tagName);

        Element ratesElement = document.createElement("rates");

        StringBuffer sb = new StringBuffer();
        int count = 0;
        for (Rate rate : this.rates) {
            sb.append(rate.value());
            count += 1;
            if (count < 4) {
                sb.append(" ");
            }
        }
        Text ratesText = document.createTextNode(sb.toString());
        ratesElement.appendChild(ratesText);
        element.appendChild(ratesElement);

        Element levelsElement = document.createElement("levels");

        sb.setLength(0);
        count = 0;
        for (Level level : this.levels) {
            sb.append(level.value());
            count += 1;
            if (count < 4) {
                sb.append(" ");
            }
        }
        Text levelsText = document.createTextNode(sb.toString());
        levelsElement.appendChild(levelsText);
        element.appendChild(levelsElement);

        return element;
    }
}
