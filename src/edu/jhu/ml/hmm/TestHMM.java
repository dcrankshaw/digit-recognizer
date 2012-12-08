package edu.jhu.ml.hmm;

import java.util.*;

public class TestHMM {

    public static void main(String[] args){
        testEmissionProbs();
    
    
    }

    public static void testEmissionProbs() {
        EmissionProbabilities testProbs = new EmissionProbabilities();

        for (int i = 0; i < 5; ++i) {
            testProbs.addObservation('a', 'b');
        }
        for (int i = 0; i < 10; ++i) {
            testProbs.addObservation('b', 'b');
        }
        for (int i = 0; i < 8; ++i) {
            testProbs.addObservation('b', 'a');
        }
        for (int i = 0; i < 8; ++i) {
            testProbs.addObservation('a', 'a');
        }


    
    
    }

}
