package cs475.hmm;

import java.util.*;
import java.io.*;

public class HMMPredictor {

    // TODO Error handling for the case that we never see a bigram
    // in training, but we do in prediction
    private HashMap<String, Double> transmissionProbs;
    private HashMap<Character, Double> firstLetterProbs;
    private EmissionProbabilities emissions;

    public HMMPredictor() {
        transmissionProbs = new HashMap<String, Double>();
        firstLetterProbs = new HashMap<Character, Double>();
        emissions = new EmissionProbabilities();
    }

    private void learnEmissionProbabilities() {
    
    }

    // For now we use no smoothing
    private void learnTransitionProbs(File corpus) {
        BufferedReader reader = new BufferedReader(new FileReader(corpus));
        double wordCount = 0;
        try {
            boolean done = false;
            while (!done) {
                String line = reader.readLine();
                if (line == null) {
                    done = true;
                } else {
                    String[] words = line.split(" ");
                    for (int i = 0; i < words.length; ++i) {
                        updateTransmissionProbs(words[i]);
                        ++wordCount;
                    }

                }
            
            }
        } catch (FileNotFoundException e ) {
        
        } catch (IOException e) {
        
        } finally {
            reader.close();
        }

        //TODO Make sure to divide probs by wordCount
    
    }

    private void updateTransmissionProbs(String word) {
        word = word.trim();
        Double firstLetterCount = firstLetterProbs.get(word.charAt(0));
        if (firstLetterCount == null) {
            firstLetterCount = new Double(0);
        }
        firstLetterProbs.put(word.charAt(0), firstLetterCount + 1);
        for (int i = 0; i < word.length() - 1; ++i) {
            String bigram = word.substring(i, i + 2);
            Double prob = transmissionProbs.get(bigram);
            if (prob == null) {
                firstLetterCount = new Double(0);
            }
            transmissionProbs.put(bigram, prob + 1);
        }
    }
}
