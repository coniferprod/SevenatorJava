package com.coniferproductions.sevenator.generators;

import com.coniferproductions.sevenator.RangedInteger;
import com.coniferproductions.sevenator.datamodel.*;

import java.util.*;

/**
 * Voice generator based on the Synthmata volca fm editor (https://synthmata.com/volca-fm/).
 * All values are 0 ... 99, so we use Level.
 */
public class Synthmata implements Generator {
    public enum Parameter {
        ATONALITY,
        COMPLEXITY,
        BRIGHTNESS,
        HARDNESS,
        HITNESS,
        TWANG,
        LONGNESS,
        WOBBLE,
        WUBBLE,
        VELOCITY
    };

    public Level atonality;
    public Level complexity;
    public Level brightness;
    public Level hardness;
    public Level hitness;
    public Level twang;
    public Level longness;
    public Level wobble;
    public Level wubble;
    public Level velocity;

    public Synthmata(Map<Parameter, Level> parameters) {
        // Default everything to 50 for now.
        this.atonality = parameters.getOrDefault(Parameter.ATONALITY, new Level(50));
        this.complexity = parameters.getOrDefault(Parameter.COMPLEXITY, new Level(50));
        this.brightness = parameters.getOrDefault(Parameter.BRIGHTNESS, new Level(50));
        this.hardness = parameters.getOrDefault(Parameter.HARDNESS, new Level(50));
        this.hitness = parameters.getOrDefault(Parameter.HITNESS, new Level(50));
        this.twang = parameters.getOrDefault(Parameter.TWANG, new Level(50));
        this.longness = parameters.getOrDefault(Parameter.LONGNESS, new Level(50));
        this.wobble = parameters.getOrDefault(Parameter.WOBBLE, new Level(50));
        this.wubble = parameters.getOrDefault(Parameter.WUBBLE, new Level(50));
        this.velocity = parameters.getOrDefault(Parameter.VELOCITY, new Level(50));

        // Construct the algorithm complexity lookup from bare integers.
        // The algorithms are listed from least to most complex.
        int[] algorithms = {
                32, 31, 25, 24, 30, 29, 23, 22,
                21, 5, 6, 28, 27, 26, 19, 20,
                1, 2, 4, 3, 9, 11, 10, 12,
                13, 8, 7, 15, 14, 17, 16, 18
        };
        this.algorithmComplexityLookup = new ArrayList<>();
        for (int i = 0; i < algorithms.length; i++) {
            this.algorithmComplexityLookup.add(new Algorithm(algorithms[i]));
        }
    }

    public Synthmata() {
        // Construct an empty parameter map.
        // The other constructor will initialize these to defaults
        this(new HashMap<Parameter, Level>());
    }

    private List<Algorithm> algorithmComplexityLookup;

    public int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

    @Override
    public Voice makeRandomVoice() {
        Voice voice = new Voice();

        int size = this.algorithmComplexityLookup.size();

        double d = size / 100.0 * this.complexity.value();
        int r = this.getRandomNumber(-size / 8, size / 8);
        int upper = (int) Math.min(
                size - 1,
                Math.floor(d) + r);
        int algorithmIndex = Math.max(0, upper);

        Algorithm algorithm = this.algorithmComplexityLookup.get(algorithmIndex);
        voice.algorithm = algorithm;

        /*
        Original comment:
        // set operator levels
        // carriers should be well audible
        // potential tweak would be to adjust things based on how many carriers there actually are?
        */
        Set<OperatorIndex> carriers = algorithm.getCarriers();
        for (OperatorIndex index : carriers) {
            Operator op = voice.getOperator(index);
            op.level = new Level(getRandomNumber(90, 99));

            /* Original comment:
            // tuning then. carriers we'll keep between 0 and 2 on coarse I think,
            // because otherwise the range gets away from you a bit
            */
            op.coarse = new Coarse(getRandomNumber(0, 3));
        }

        /* Original comment:
        // modulators are governed by a few things ideally long-term, but to begin with, it's just brightness.
        // 1st gen modulators need to be scaled up so that they have an audible effect, the others we just set
        // based on the randomness...
        */
        for (int index = OperatorIndex.TYPE.first(); index <= OperatorIndex.TYPE.last(); index++) {
            OperatorIndex operatorIndex = new OperatorIndex(index);
            if (carriers.contains(operatorIndex)) {
                continue;
            }

            Operator op = voice.getOperator(operatorIndex);
            if (algorithm.getFirstGenerationModulators().contains(operatorIndex)) {
                op.level = new Level(Math.max(0,
                                 (int) Math.min(100, (50 / 100.0 * this.brightness.value())) + getRandomNumber(-10, 10) + 50));
            } else {
                op.level = new Level(Math.max(0,
                        this.brightness.value()) + getRandomNumber(-10, 10));
            }

            /* Original comment:
            // so the atonality is going to be a little complex at the bottom end we just want detune, from there we have
            // small amounts of fine but really it's more about the relationships than the amount.
            // (also brightness will control the coarse)
             */
            op.coarse = new Coarse(RangedInteger.getRandomInteger(0, (int)(32 / 100.0 * this.brightness.value())));

            var atonalityDividers = List.of(1, 2, 4, 3, 5);
            var firstFineDividerPossibility = (int)(Math.floor((this.atonality.value() * atonalityDividers.size()) / 100) + 1);
            var fineDivider = atonalityDividers.get(RangedInteger.getRandomInteger(0, firstFineDividerPossibility));
            int fineValue = RangedInteger.getRandomInteger(0, fineDivider) * ((int)(100 / fineDivider));

            int maxDeviation = atonality.value() / 14;
            int maxDetune = (atonality.value() % 14);

            fineValue += RangedInteger.getRandomInteger(0, maxDeviation + 1);
            fineValue %= 100;

            op.fine = new Level(fineValue);
            op.detune = new Detune(RangedInteger.getRandomInteger(0, maxDetune + 1) - 7);   // bring into -7 ... +7
        }

        /* Original comment:
        // and on to the envelope
        // i'm allowing a little more randomness in here, because the envelope is less likely
        // to ruin a patch as much as the timbre stuff. The timbre was kinda "rules", this is
        // more "guidelines"
        // hardness is attack (so just R1 really), that applies to carriers primarily (but factors into twangness)
        // hitness is the drop from l1 down to l2 - how fast it is (r2) and how far you fall (l2) on a *carrier*
        // twangness is hitness and hardness for modulators wrapped into one - the journey from l4 up to l1 and down to l2.
        //     hitness and hardness will factor into these calculations, but to a lesser degree.
        // longness is l3 levels and r4
        // r3.... that can kinda just be random (with a bit of the other stuff built in I guess)
         */
        for (int index = OperatorIndex.TYPE.first(); index <= OperatorIndex.TYPE.last(); index++) {
            OperatorIndex operatorIndex = new OperatorIndex(index);

            int r1 = 0;
            int r2 = 0;
            int r3 = 0;
            int r4 = 0;
            int l1 = 99;  // always
            int l2 = 0;
            int l3 = 0;
            int l4 = 0;

            if (carriers.contains(operatorIndex)) {
                l4 = 0;  // Original comment: "always for carriers"
            } else {
                // Original comment: "hardness and twang needs a lower l4 to get that travel"
                int h = this.hardness.value();
                int t = this.twang.value();
                l4 = Math.min(0, RangedInteger.getRandomInteger(0, 100) - RangedInteger.getRandomInteger(0, h)
                        - RangedInteger.getRandomInteger(0, t));
                // this seems to go below zero all the time, let's adjust
                l4 = Math.abs(l4);
            }

            // r1 (hardness, twang)
            // Original comment: "practically, we should consider hardness as a bias around the point where soft and hard kinda live in the
            // envelopes which is about 62, not 50"
            int r1Bias = this.hardness.value() - 62;
            if (carriers.contains(operatorIndex)) {
                // Original comment: "for carriers, this is pure hardness"
                r1 = Math.min(99, Math.max(0, 62 + RangedInteger.getRandomInteger(Math.min(0, r1Bias - 20), Math.max(20, r1Bias + 20))));
            } else {
                // Original comment: "for modulators, hardness must be tempered by twang"
                r1 = Math.min(99, Math.max(0, 62 + RangedInteger.getRandomInteger(Math.min(0, r1Bias - 20), Math.max(20, r1Bias + 20))
                        - RangedInteger.getRandomInteger(0, this.twang.value() / 5)));
            }

            // r2 (hitness, twang)
            // Original comment: "we work with that bias again"
            int r2Bias = this.hitness.value() - 62;
            if (carriers.contains(operatorIndex)) {
                // Original comment: "for carriers, this is pure hitness"
                r2 = Math.min(99, Math.max(0, 62 + RangedInteger.getRandomInteger(Math.min(0, r2Bias - 20), Math.max(20, r2Bias + 20))));
            } else {
                // Original comment: "for modulators, hitness must be tempered by twang"
                r2 = Math.min(99, Math.max(0, 62 + RangedInteger.getRandomInteger(Math.min(0, r1Bias - 20), Math.max(20, r1Bias + 20))
                        - RangedInteger.getRandomInteger(0, this.twang.value() / 5)));
            }

            // l2 (hitness, twang)
            if (carriers.contains(operatorIndex)) {
                // Original comment: "for carriers, this is pure hitness"
                l2 = Math.max(0, 99 - RangedInteger.getRandomInteger(0, this.hitness.value()));
            } else {
                // Original comment: "for modulators, it's hitness and twang"
                l2 = Math.max(0, 99 - RangedInteger.getRandomInteger(0, this.hitness.value() / 2) - RangedInteger.getRandomInteger(0, this.twang.value() / 2));
            }

            // r3...probably kinda all of them
            int r3Bias = this.hardness.value() - 50;
            if (carriers.contains(operatorIndex)) {
                r3 = 50;
                r3 += RangedInteger.getRandomInteger(Math.min(0, r3Bias) + 1, Math.max(0, r3Bias) + 2);  // adjust so that randInt bound won't be zero
                r3 -= RangedInteger.getRandomInteger(0, this.longness.value() / 3);
            } else {
                r3 = 50;
                r3 += RangedInteger.getRandomInteger(Math.min(0, r3Bias) + 1, Math.max(0, r3Bias) + 2);  // adjust like above
                r3 -= RangedInteger.getRandomInteger(0, this.longness.value() / 6);
                r3 -= RangedInteger.getRandomInteger(0, this.twang.value() / 6);
            }

            // l3 - mostly likely down for carriers, either for modulators.
            if (carriers.contains(operatorIndex)) {
                l3 = RangedInteger.getRandomInteger(0, 9) == 8 ? RangedInteger.getRandomInteger(l2, 99) : RangedInteger.getRandomInteger(0, l2 + 1);  // adjust for boundsx
            } else {
                l3 = l2 + RangedInteger.getRandomInteger(-l2, 99 - l2);
            }

            // Original comment: "r4 - about dat longness"
            r4 = 0;
            for (int i = 0; i < 100; i += (this.longness.value() + 1)) {
                r4 += RangedInteger.getRandomInteger(0, 30);
            }

            Operator op = voice.getOperator(operatorIndex);
            List<Rate> rates = List.of(new Rate(r1), new Rate(r2), new Rate(r3), new Rate(r4));
            List<Level> levels = List.of(new Level(l1), new Level(l2), new Level(l3), new Level(l4));
            op.eg = new Envelope(rates, levels);
        }

        // Original comment: "movement stuff (lfos and velocity)
        // velocity - dice rolls, more likely to modulators"
        for (int index = OperatorIndex.TYPE.first(); index <= OperatorIndex.TYPE.last(); index++) {
            OperatorIndex operatorIndex = new OperatorIndex(index);

            int odds = carriers.contains(operatorIndex) ? 8 : 3;
            int kvs = 0;
            for (int i = 0 ; i < 7; i++) {
                if (RangedInteger.getRandomInteger(0, odds * 100) < this.velocity.value()) {
                    kvs += 1;
                }
            }
            Operator op = voice.getOperator(operatorIndex);
            op.keyVelocitySensitivity = new Depth(kvs);
        }

        // Original comment: "wubble (am)
        // for the operators it's dice rolls as above, but we also need to set
        // the global depth"
        for (int index = OperatorIndex.TYPE.first(); index <= OperatorIndex.TYPE.last(); index++) {
            OperatorIndex operatorIndex = new OperatorIndex(index);
            int odds = carriers.contains(operatorIndex) ? 5 : 3;
            int ams = 0;
            for (int i = 0 ; i < 7; i++) {
                if (RangedInteger.getRandomInteger(0, odds * 100) < this.wubble.value()) {
                    ams += 1;
                }
            }
            Operator op = voice.getOperator(operatorIndex);

            // Clamp the value (temporary measure)
            if (ams < Sensitivity.TYPE.first()) {
                ams = Sensitivity.TYPE.first();
            } else if (ams > Sensitivity.TYPE.last()) {
                ams = Sensitivity.TYPE.last();
            }
            op.amplitudeModulationSensitivity = new Sensitivity(ams);
        }

        voice.lfo.amd = new Level(Math.max(0, Math.min(99, this.wubble.value() + RangedInteger.getRandomInteger(-40, 40))));

        // Original comment: "wobble (pitch mod)
        // so this is 2 global parameters pitch mod depth and pitch mod sensitivity.
        // to my ears, each notch on the PMS doubles the sensitivity, so depth 99 on
        // sensitity 1 is roughly the same as depth 50 on sensitivity 2, which is the
        // same as depth 25 on sensitivity 3, and so on
        // so that basically gives us a pitch mod amount that runs from 0 to 6400 more
        // or less.
        // of course, if the user has the slider set to 0, we don't do anything"
        if (this.wobble.value() > 0) {
            // Original comment: "To make the slider even remotely useful though, we need to log scale it:"
            int minWobp = 0;
            int maxWobp = 100;

            int minWobv = (int) Math.log(1);
            int maxWobv = (int) Math.log(6400);

            int wobScale = (maxWobv - minWobv) / (maxWobp - minWobp);
            int wobbleLog = (int) Math.exp(minWobv + wobScale * (wobble.value() - minWobp));

            int wobbleVariance = 50; // Original comment: "arbitrary, tweak for best results"
            int wobbleValue = Math.max(0, Math.min(6400, wobbleLog + RangedInteger.getRandomInteger(-wobbleVariance, wobbleVariance)));

            // Original comment: "once we have that value, we work out the most precise place to put it"
            int pitchSensitivity = 1;
            int i = 100;
            while (wobbleValue > i) {
                pitchSensitivity += 1;
                i = 100 * (1 << (pitchSensitivity - 1));
            }
            int pitchModulationDepth = wobbleValue / (1 << (pitchSensitivity - 1));

            voice.pitchModulationSensitivity = new Depth(pitchSensitivity);
            voice.lfo.pmd = new Level(pitchModulationDepth);
        }

        /* Original comment:
        // feedback i'm going to give both complexity and brightness a go at. They get a roll of the dice
        // multiple times based on their values. Each roll has a chance to increase feedback.
        */
        int fb = 0;
        for (int i = 20; i < brightness.value(); i+= 20) {
            if (RangedInteger.getRandomInteger(0, 3) == 2) {
                fb += 1;
            }
        }
        for (int i = 20; i < complexity.value(); i+= 20) {
            if (RangedInteger.getRandomInteger(0, 3) == 2) {
                fb += 1;
            }
        }
        voice.feedback = new Depth(fb);

        /* Original comment:
        // lfo speed
        // so, my theory here is that there's a sweet spot in the middle which sounds
        // "right" on a lot of patches. Somewhere between 20 and 60. Outside of that it's
        // more "complex" or more "seasick" (wobbly). So i'm going to have stuff pulling in
        // different directions from that sweetspot.
        */
        int lfoSpeedValue = 40;
        lfoSpeedValue += getRandomNumber(0, (int)(this.complexity.value() * 0.6));
        lfoSpeedValue -= getRandomNumber(0, (int)(this.wobble.value() * 0.6));

        /* Original comment:
        // finally, just add some generic, arbitrary randomness, yo
        */
        lfoSpeedValue += getRandomNumber(-20, 20);
        lfoSpeedValue = Math.max(0, Math.min(99, lfoSpeedValue));
        voice.lfo.speed = new Level(lfoSpeedValue);

        /* Original comment:
        // finally the lfo shape - just down to complexity
        */
        voice.lfo.waveform = LFO.Waveform.byComplexity(this.complexity);

        return voice;
    }
}
