package edu.jhu.ml.hmm;

import java.io.Serializable;
import java.util.HashMap;

public class EmissionProbabilities implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private HashMap<Character, Integer> emissionCounts;
    private HashMap<Character, Double> emissionProbs;
    Character actual;
    private double totalObservations;

    public EmissionProbabilities(Character c) {
        emissionCounts = new HashMap<Character, Integer>();
        emissionProbs = new HashMap<Character, Double>();
        totalObservations = 0;
        if (c < 'a' || c > 'z') {
    		throw new IllegalArgumentException("Constructed with illegal hidden character: " + c);
    	}
        actual = c;
        
        // Add smoothing
        for (int i = 'a'; i <= 'z'; ++i) {
        	emissionCounts.put((char) i, 1);
        	++totalObservations;
        }
    }

    public double getTotalObservations() { return totalObservations; }

    public void addObservation(char observed) {
    	if (observed < 'a' || observed > 'z') {
    		throw new IllegalArgumentException("Added illegal observation: " + observed);
    	}
        Integer prob = emissionCounts.get(observed);
        
        if (prob == null) {
            throw new IllegalStateException("Bad smoothing in emission probability");
        }
        emissionCounts.put(observed, prob + 1);
        totalObservations += 1;
    }

    // Really these are log probabilities
    public void calculateProbabilities() {
        emissionProbs.clear();
        for (Character c : emissionCounts.keySet()) {
            Double count = new Double(emissionCounts.get(c));
            Double logProb = Math.log(count / totalObservations);
            emissionProbs.put(c, logProb);
        }
    }

    public Double getProbability(char observed) {
    	Double p = emissionProbs.get(observed);
    	if (p == null) {
    		throw new IllegalStateException("Found a null probability. actual: " + actual + " observed: " + observed);
    	}
    	return p;
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
