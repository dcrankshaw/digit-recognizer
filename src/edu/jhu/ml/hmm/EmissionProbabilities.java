package edu.jhu.ml.hmm;

import java.util.*;

public class EmissionProbabilities {

    private HashMap<LatentObservedPair, Integer> emissionCounts;
    private HashMap<LatentObservedPair, Double> emissionProbs;
    private double totalPairs;

    public EmissionProbabilities() {
        emissionCounts = new HashMap<LatentObservedPair, Integer>();
        emissionProbs = new HashMap<LatentObservedPair, Double>();
        totalPairs = 0;
    }

    public double getTotalPairs() { return totalPairs; }

    public void addObservation(char observed, char actual){
        LatentObservedPair pair = new LatentObservedPair(observed, actual);
        Double prob = emissionCounts.get(pair);
        if (prob == null) {
            prob = new Double(0);
        }
        emissionCounts.put(pair, prob + 1);
        totalPairs += 1;
    }

    // Really these are log probabilities
    public void calculateProbabilities() {
        emissionProbs.clear();
        for (LatentObservedPair pair : emissionCounts.keySet()) {
            Double count = (Double) emissionCounts.get(pair);
            Double logProb = Math.log(count / totalPairs);
            emissionProbs.put(pair, logProb);
        }
    }

    public Double getProbability(char observed, char actual) {
        return emissionProbs.get(new LatentObservedPair(observed, actual));
    }

    private final class LatentObservedPair {

        private final char observed;
        private final char latent;

        public LatentObservedPair(char obs, char lat) {
            observed = obs;
            latent = lat;
        }

        public boolean equals(Object obj) {
            if (obj == null) return false;
            if (obj == this) return true;
            if (obj.getClass() != this.getClass()) return false;
            LatentObservedPair other = (LatentObservedPair) obj;
            return (other.getObserved() == this.observed && other.getlatent() == this.latent);
        }

        // hashCode method taken from Joshua Bloch's book Effective Java
        public int hashCode() {
            int result = 17;
            result = 37*result + (int) observed;
            result = 37*result + (int) latent;
            return result;
        }

    }
}
