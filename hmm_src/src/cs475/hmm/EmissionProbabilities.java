package cs475.hmm;

public class EmissionProbabilities {

    private HashMap<LatentObservedPair, Double> emissionProbs;

    public EmissionProbabilities() {
        emissionProbs = new HashMap<LatentObservedPair, Double>();
    
    }

    public void addObservation(char observed, char actual){
    
    }

    // Really these are log probabilities
    public void calculateProbabilities() {
    
    }

    public Double getProbability(char observed, char actual) {
    
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
