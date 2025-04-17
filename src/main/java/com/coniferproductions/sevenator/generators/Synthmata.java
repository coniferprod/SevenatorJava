package com.coniferproductions.sevenator.generators;

import com.coniferproductions.sevenator.datamodel.*;

import java.util.*;

/**
 * Voice generator based on the Synthmata volca fm editor (https://synthmata.com/volca-fm/).
 * All values are 0 ... 99, so we use Level.
 */
public class Synthmata implements Generator {
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

    public Synthmata() {
        // Default everything to 50 for now.
        this.atonality = new Level(50);
        this.complexity = new Level(50);
        this.brightness = new Level(50);
        this.hardness = new Level(50);
        this.hitness = new Level(50);
        this.twang = new Level(50);
        this.longness = new Level(50);
        this.wobble = new Level(50);
        this.wubble = new Level(50);
        this.velocity = new Level(50);

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

    private List<Algorithm> algorithmComplexityLookup;

    public int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

    @Override
    public Voice makeRandomVoice() {
        Voice voice = new Voice();

        int size = this.algorithmComplexityLookup.size();

        double d = size / 100.0 * this.complexity.value();
        int r = this.getRandomNumber(-size / 8, size /8);
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
        Map<OperatorIndex, Operator> operators = voice.getOperators();
        for (OperatorIndex index : carriers) {
            Operator op = operators.get(index);  // FIXME: why is this null?
            op.level = new Level(getRandomNumber(90, 99));
        }
        voice.setOperators(operators);

        return voice;
    }
}
