package edu.jhu.ml.hmm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.jhu.ml.data.Instance;
import edu.jhu.ml.predictor.NeuralNetwork;
import edu.jhu.ml.data.label.*;

public class HMMPredictor implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// TODO Error handling for the case that we never see a bigram
    // in training, but we do in prediction
    // NOTE: Probabilities are all log probabilities
    private HashMap<String, Double> transmissionProbabilities;
    private HashMap<Character, Double> firstLetterProbabilities;
    private HashMap<Character, Double> totalLetterProbabilities;
    private HashMap<Character, EmissionProbabilities> emissions;
    private NeuralNetwork letterPredictor;
    private String trainingCorpus;
    public static final int A_VAL = (int) 'a';

    public HMMPredictor(NeuralNetwork p, String corpusLocation) {
        transmissionProbabilities = new HashMap<String, Double>();
        firstLetterProbabilities = new HashMap<Character, Double>();
        totalLetterProbabilities = new HashMap<Character, Double>();
        emissions = new HashMap<Character, EmissionProbabilities>();
        letterPredictor = p;
        trainingCorpus = corpusLocation;
    }

    // TODO right now we are learning P(x | z), but it may
    // be useful to use Bayes theorem instead. In this case,
    // we could learn probability of P(z | x) - this is the probability
    // of actual label given what the ANN predicted, a perhaps better model
    // for the ANN. We would also learn P(x) - the probability of seeing the
    // letter that the ANN predicted (which would be learned from the corpus).
    
    // We actually use Bayes Theorem right now
    private void learnEmissionProbabilities(List<Instance> letterInstances) {
        for(Instance instance: letterInstances) {
            Character observed = letterPredictor.predictCharacter(instance);
            		
            		//getANNPrediction(letterPredictor.predict(instance));
            Character actual = (char) (A_VAL + ((ClassificationLabel) instance.getLabel()).getLabel());
            EmissionProbabilities current = emissions.get(observed);
            if (current == null) {
                current = new EmissionProbabilities(observed);
            }
            current.addObservation(actual);
            emissions.put(observed, current);
        }

        for (EmissionProbabilities probs : emissions.values()) {
            probs.calculateProbabilities();
        }
    }
    
    // pretends like we have a 100% accurate ANN
    private void learnEmissionProbsTest(List<Instance> letterInstances) {
    	for(Instance instance: letterInstances) {
            Character observed = letterPredictor.predictCharacter(instance);
            		
            		//getANNPrediction(letterPredictor.predict(instance));
            Character actual = observed;
            EmissionProbabilities current = emissions.get(observed);
            if (current == null) {
                current = new EmissionProbabilities(observed);
            }
            current.addObservation(actual);
            emissions.put(observed, current);
        }

        for (EmissionProbabilities probs : emissions.values()) {
            probs.calculateProbabilities();
        }
    }

    // TODO Add smoothing
    private void learnCorpusParams(File corpus) {
        BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(corpus));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        double wordCount = 0;
        double bigramCount = 0;
        double letterCount = 0;
        HashMap<String, Double> bigramCounts = new HashMap<String, Double>();
        HashMap<Character, Double> firstLetterCounts = new HashMap<Character, Double>();
        HashMap<Character, Double> totalLetterCounts = new HashMap<Character, Double>();
        try {
            boolean done = false;
            while (!done) {
                String line = reader.readLine();
                if (line == null) {
                    done = true;
                } else {
                    String[] words = line.split(" ");
                    for (int i = 0; i < words.length; ++i) {
                    	String word = words[i].trim();
                    	if (word.length() > 0) {
                    		updateTransmissionProbabilities(word, bigramCounts, firstLetterCounts, totalLetterCounts);
                    		bigramCount += word.length() - 1;
                    		letterCount += word.length();
                    		++wordCount;
                    	}
                    }

                }
            }
            reader.close();

        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        for (String bigram : bigramCounts.keySet()) {
            Double count = bigramCounts.get(bigram);
            Double prob = Math.log(count / bigramCount);
            transmissionProbabilities.put(bigram, prob);
        }
        for (Character c : firstLetterCounts.keySet()) {
            Double count = firstLetterCounts.get(c);
            Double prob = Math.log(count / wordCount);
            firstLetterProbabilities.put(c, prob);
        }
        for (Character c : totalLetterCounts.keySet()) {
            Double count = totalLetterCounts.get(c);
            Double prob = Math.log(count / letterCount);
            totalLetterProbabilities.put(c, prob);
        }
    }

    private void updateTransmissionProbabilities(String word, HashMap<String, Double> bigramCounts, HashMap<Character, Double> firstLetterCounts, HashMap<Character, Double> totalLetterCounts) {
        Double firstLetterCount = firstLetterCounts.get(word.charAt(0));
        if (firstLetterCount == null) {
            firstLetterCount = new Double(0);
        }
        firstLetterCounts.put(word.charAt(0), firstLetterCount + 1);
        for (int i = 0; i < word.length() - 1; ++i) {
            String bigram = word.substring(i, i + 2);
            Double prob = bigramCounts.get(bigram);
            if (prob == null) {
                prob = new Double(0);
            }
            bigramCounts.put(bigram, prob + 1);
        }
        // Count letter probabilities
        for (int i = 0; i < word.length(); ++i) {
            Double letterCount = totalLetterCounts.get(word.charAt(i));
            if (letterCount == null) {
                letterCount = new Double(0);
            }
            totalLetterCounts.put(word.charAt(i), letterCount + 1);
        }
    }

    public void train(List<Instance> letterInstances) {
        learnCorpusParams(new File(trainingCorpus));
        System.out.println("Learned transmission probs.");
        learnEmissionProbabilities(letterInstances);
        //learnEmissionProbsTest(letterInstances);
        System.out.println("Learned emission probs.");
    }
    
    

    public String predictWord(List<Instance> lettersInWord) {
        // This is the list of the letters predicted by the ANN.
        List<Character> observedLetters = new ArrayList<Character>(lettersInWord.size());
        //int i = 0;
        for (Instance letter : lettersInWord) {
            observedLetters.add(letterPredictor.predictCharacter(letter));
            //i += 1;
        }
        return HMMPredictWord(observedLetters);
    }


    // An implementation of the Viterbi algorithm adapted from Wikipedia
    // http://en.wikipedia.org/wiki/Viterbi_algorithm
    private String HMMPredictWord(List<Character> observedCharacters) {
        int wordLength = observedCharacters.size();
        double[][] maxProbs = new double[26][wordLength];
        int[][] maxProbChars = new int[26][wordLength];
        for (int character = 0; character < 26; ++character) {
            Character current = (char) (A_VAL + character);
            maxProbs[character][0] = firstLetterProbabilities.get(current);
            maxProbChars[character][0] = 0;
        }
        for (int position = 1; position < wordLength; ++position) {
            for (int curChar = 0; curChar < 26; ++curChar) {
                int maxProbPrevChar = 0;
                double maxProb = 0;
                for (int prevChar = 0; prevChar < 26; ++prevChar) {
                    Character prev = (char) (A_VAL + prevChar);
                    Character current = (char) (A_VAL + curChar);
                    String bigram = "" + prev + current;
                    Double Aij = transmissionProbabilities.get(bigram);
                    if (Aij == null) {
                    	Aij = new Double(0.0);
                    }
                    // The probability of observing observedCharacter[position]
                    // if the real letter is curChar P(x|z) = (P(z|x)*P(x)/P(z)
                    // TODO BAYES THEOREM
                    Character observed = observedCharacters.get(position);
                    Double Px = totalLetterProbabilities.get(observed);
                    if (Px == null) {
                    	Px = new Double(0.0);
                    }
                    Double Pz = totalLetterProbabilities.get(current);
                    if (Pz == null) {
                    	Pz = new Double(0.0);
                    }
                    Double PzGivenX = emissions.get(observed).getProbability(current);
                    if (PzGivenX == null) {
                    	PzGivenX = new Double(0.0);
                    }
                    // They are log probabilities so we add and subtract
                    double Bjyi = PzGivenX + Px - Pz;
                    // We add because these are log probabilities
                    double prob = maxProbs[prevChar][position - 1] + Aij + Bjyi;
                    if (prob > maxProb) {
                        maxProb = prob;
                        maxProbPrevChar = prevChar;
                    }
                }
                maxProbs[curChar][position] = maxProb;
                maxProbChars[curChar][position] = maxProbPrevChar;
            }
        }
        double maxProb = 0;
        int maxProbLastChar = 0;
        for (int character = 0; character < 26; ++character) {
            if (maxProb < maxProbs[character][wordLength - 1]) {
                maxProb = maxProbs[character][wordLength - 1];
                maxProbLastChar = character;
            }
        }
        StringBuilder word = new StringBuilder();
        word.append((char) (maxProbLastChar + A_VAL));
        int previousChar = maxProbChars[maxProbLastChar][wordLength - 1];
        word.append((char) (previousChar + A_VAL));
        for (int position = wordLength - 2; position > 0; --position) {
            previousChar = maxProbChars[previousChar][position];
            word.append((char) (previousChar + A_VAL));
        }
        word = word.reverse();
        return word.toString();
    }
}
