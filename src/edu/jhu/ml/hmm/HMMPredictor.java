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
    public static final int LETTERS_IN_ALPHABET = 26;

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
        //learnEmissionProbsBayes(letterInstances);
    	learnEmissionProbsDirect(letterInstances);
    }
    
    private void learnEmissionProbsDirect(List<Instance> letterInstances) {
    	for(Instance instance: letterInstances) {
            Character observed = letterPredictor.predictCharacter(instance);
            		
            		//getANNPrediction(letterPredictor.predict(instance));
            Character actual = (char) (A_VAL + ((ClassificationLabel) instance.getLabel()).getLabel());
            EmissionProbabilities current = emissions.get(actual);
            if (current == null) {
                current = new EmissionProbabilities(actual);
            }
            current.addObservation(observed);
            emissions.put(actual, current);
        }

        for (EmissionProbabilities probs : emissions.values()) {
            probs.calculateProbabilities();
        }
    }
    
    private void learnEmissionProbsBayes(List<Instance> letterInstances) {
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

        // Add-lambda smoothing (just add 1)
        for (int f = 0; f < LETTERS_IN_ALPHABET; ++f) {
        	char fChar = (char) (f + A_VAL);
        	Double fCharTotalCount = totalLetterCounts.get(fChar);
        	if (fCharTotalCount == null) {
        		fCharTotalCount = new Double(0);
        	}
        	totalLetterCounts.put(fChar, fCharTotalCount + 1);
        	
        	++letterCount;
        	Double fCharFirstCount = firstLetterCounts.get(fChar);
        	if (fCharFirstCount == null) {
        		fCharFirstCount = new Double(0);
        	}
        	firstLetterCounts.put(fChar, fCharFirstCount + 1);
        	++wordCount;
        	
        	for (int s = 0; s < LETTERS_IN_ALPHABET; ++s) {
        		
        		char sChar = (char) (s + A_VAL);
        		char[] bigramArray = {fChar, sChar};
        		String bigram = new String(bigramArray);
                Double prob = bigramCounts.get(bigram);
                if (prob == null) {
                    prob = new Double(0);
                }
                bigramCounts.put(bigram, prob + 1);
                ++bigramCount;
        	}
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
        
        double[][] probabilities = new double[LETTERS_IN_ALPHABET][wordLength];
        int[][] characters = new int[LETTERS_IN_ALPHABET][wordLength];
        for (int letter = 0; letter < LETTERS_IN_ALPHABET; ++letter) {
        	Character current = (char) (A_VAL + letter);
        	// First letter probs are probability that the current letter is at the beginning of the word * probability
        	// that we observed the first observation given that it was actually this letter
        	probabilities[letter][0] = firstLetterProbabilities.get(current) + emissions.get(current).getProbability(observedCharacters.get(0));
        	characters[letter][0] = -1;
        }
        
        for (int letterPosition = 1; letterPosition < wordLength; ++letterPosition) {
        	for (int currentLetter = 0; currentLetter < LETTERS_IN_ALPHABET; ++currentLetter) {
        		int mostProbablePreviousLetter = -1;
        		double maxProbabilityPreviousLetter = -1*Double.MAX_VALUE;
        		for (int previousLetter = 0; previousLetter < LETTERS_IN_ALPHABET; ++previousLetter) {
        			Character previousChar = (char) (A_VAL + previousLetter);
                    Character currentChar = (char) (A_VAL + currentLetter);
                    char[] bigramArray = {previousChar, currentChar};
                    String bigram = new String(bigramArray);
                    Double bigramTransmissionProbability = transmissionProbabilities.get(bigram);
                    if (bigramTransmissionProbability == null) {
                    	throw new IllegalStateException("Found a null transmission probability");
                    }
                    Character observed = observedCharacters.get(letterPosition);
                    Double emissionProbability = emissions.get(currentChar).getProbability(observed);
                    if (emissionProbability == null) {
                    	throw new IllegalStateException("Found a null emission probability");
                    }
                    // probability of reaching the previous character times probability of transitioning to this character times
                    // the probability of observing the observed character conditioned on the actual character being this character
                    Double currentLetterProbability = probabilities[previousLetter][letterPosition - 1] + bigramTransmissionProbability + emissionProbability;
                    if (currentLetterProbability > maxProbabilityPreviousLetter) {
                    	mostProbablePreviousLetter = previousLetter;
                    	maxProbabilityPreviousLetter = currentLetterProbability;
                    }
        		}
        		probabilities[currentLetter][letterPosition] = maxProbabilityPreviousLetter;
        		characters[currentLetter][letterPosition] = mostProbablePreviousLetter;
        	}
        	
        }
        
        int mostProbableLastLetter = -1;
        double maxProbabilityLastLetter = -1*Double.MAX_VALUE;
        for (int lastLetter = 0; lastLetter < LETTERS_IN_ALPHABET; ++lastLetter) {
        	if (probabilities[lastLetter][wordLength - 1] > maxProbabilityLastLetter) {
        		mostProbableLastLetter = lastLetter;
        		maxProbabilityLastLetter = probabilities[lastLetter][wordLength - 1];
        	}
        }
        StringBuilder word = new StringBuilder();
        if (mostProbableLastLetter == -1) {
        	System.out.println("Error finding most probable last letter");
        }
        word.append((char) (mostProbableLastLetter + A_VAL));
        int previousChar = characters[mostProbableLastLetter][wordLength - 1];
        word.append((char) (previousChar + A_VAL));
        for (int position = wordLength - 2; position > 0; --position) {
        	previousChar = characters[previousChar][position];
        	word.append((char) (previousChar + A_VAL));
        }
        word = word.reverse();
        return word.toString();
        

//        double[][] maxProbs = new double[LETTERS_IN_ALPHABET][wordLength];
//        int[][] maxProbChars = new int[LETTERS_IN_ALPHABET][wordLength];
//        for (int character = 0; character < LETTERS_IN_ALPHABET; ++character) {
//            Character current = (char) (A_VAL + character);
//            maxProbs[character][0] = firstLetterProbabilities.get(current);
//            maxProbChars[character][0] = -1;
//        }
//        for (int position = 1; position < wordLength; ++position) {
//            for (int curChar = 0; curChar < LETTERS_IN_ALPHABET; ++curChar) {
//            	int maxProbPrevChar = -1;
//                double maxProb = Double.MIN_VALUE;
//                for (int prevChar = 0; prevChar < LETTERS_IN_ALPHABET; ++prevChar) {
//                    Character prev = (char) (A_VAL + prevChar);
//                    Character current = (char) (A_VAL + curChar);
//                    String bigram = "" + prev + current;
//                    Double Aij = transmissionProbabilities.get(bigram);
//                    if (Aij == null) {
//                    	throw new IllegalStateException("Found a null probability");
//                    }
//                    Character observed = observedCharacters.get(position);
//                    // The probability of observing observedCharacter[position]
//                    // if the real letter is curChar P(x|z) = (P(z|x)*P(x)/P(z)
//                    // BAYES THEOREM
//                    
//                    /*Double Px = totalLetterProbabilities.get(observed);
//                    if (Px == null) {
//                    	Px = new Double(0.0);
//                    }
//                    Double Pz = totalLetterProbabilities.get(current);
//                    if (Pz == null) {
//                    	Pz = new Double(0.0);
//                    }
//                    Double PzGivenX = emissions.get(observed).getProbability(current);
//                    if (PzGivenX == null) {
//                    	PzGivenX = new Double(0.0);
//                    }
//                    // They are log probabilities so we add and subtract
//                    double Bjyi = PzGivenX + Px - Pz;
//                    */
//                    // Direct probability estimate
//                    Double Bjyi = emissions.get(current).getProbability(observed);
//                    if (Bjyi == null) {
//                    	throw new IllegalStateException("Found a null probability");
//                    }
//                    // We add because these are log probabilities
//                    double prob = maxProbs[prevChar][position - 1] + Aij + Bjyi;
//                    if (prob > maxProb) {
//                        maxProb = prob;
//                        maxProbPrevChar = prevChar;
//                    }
//                }
//                maxProbs[curChar][position] = maxProb;
//                maxProbChars[curChar][position] = maxProbPrevChar;
//            }
//        }
//        double maxProb = 0;
//        int maxProbLastChar = 0;
//        for (int character = 0; character < LETTERS_IN_ALPHABET; ++character) {
//            if (maxProb < maxProbs[character][wordLength - 1]) {
//                maxProb = maxProbs[character][wordLength - 1];
//                maxProbLastChar = character;
//            }
//        }
//        StringBuilder word = new StringBuilder();
//        word.append((char) (maxProbLastChar + A_VAL));
//        int previousChar = maxProbChars[maxProbLastChar][wordLength - 1];
//        word.append((char) (previousChar + A_VAL));
//        for (int position = wordLength - 2; position > 0; --position) {
//            previousChar = maxProbChars[previousChar][position];
//            word.append((char) (previousChar + A_VAL));
//        }
//        word = word.reverse();
//        return word.toString();
    }
}
