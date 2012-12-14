package edu.jhu.ml.hmm;

import java.util.HashMap;

public class EmissionProbabilities {

    private HashMap<Character, Integer> emissionCounts;
    private HashMap<Character, Double> emissionProbs;
    Character observed;
    private double totalPairs;

    public EmissionProbabilities(Character c) {
        emissionCounts = new HashMap<Character, Integer>();
        emissionProbs = new HashMap<Character, Double>();
        totalPairs = 0;
        observed = c;
    }

    public double getTotalPairs() { return totalPairs; }

    public void addObservation(char actual){
        //LatentObservedPair pair = new LatentObservedPair(observed, actual);
        Integer prob = emissionCounts.get(actual);
        if (prob == null) {
            prob = new Integer(0);
        }
        emissionCounts.put(actual, prob + 1);
        totalPairs += 1;
    }

    // Really these are log probabilities
    public void calculateProbabilities() {
        emissionProbs.clear();
        for (Character c : emissionCounts.keySet()) {
            Double count = new Double(emissionCounts.get(c));
            Double logProb = Math.log(count / totalPairs);
            emissionProbs.put(c, logProb);
        }
    }

    public Double getProbability(char actual) {
    	Double p = emissionProbs.get(actual);
    	if (p == null) {
    		return 0.0;
    	} else {
    		return p;
    	}
    }
    
    public static void main(String[] args) {
    	EmissionProbabilities aprobs  = new EmissionProbabilities('a');
    	EmissionProbabilities bprobs  = new EmissionProbabilities('b');
    	aprobs.addObservation('a');
    	aprobs.addObservation('a');
    	aprobs.addObservation('a');
    	aprobs.addObservation('a');
    	aprobs.addObservation('x');
    	aprobs.addObservation('x');
    	aprobs.addObservation('x');
    	aprobs.addObservation('x');
    	aprobs.calculateProbabilities();
    	
    	bprobs.addObservation('b');
    	bprobs.addObservation('b');
    	bprobs.addObservation('b');
    	bprobs.addObservation('b');
    	bprobs.addObservation('b');
    	bprobs.addObservation('b');
    	bprobs.addObservation('x');
    	bprobs.addObservation('x');
    	bprobs.addObservation('x');
    	bprobs.addObservation('x');
    	bprobs.calculateProbabilities();
    	System.out.println("P(a | a): " + aprobs.getProbability('a'));
    	System.out.println("P(a | b): " + bprobs.getProbability('a'));
    	System.out.println("P(x | a): " + aprobs.getProbability('x'));
    	System.out.println("P(x | b): " + bprobs.getProbability('x'));
    	System.out.println("P(b | a): " + aprobs.getProbability('b'));
    	System.out.println("P(b | b): " + bprobs.getProbability('b'));
    	
    }

    /*private final class LatentObservedPair {

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

    }*/
}
