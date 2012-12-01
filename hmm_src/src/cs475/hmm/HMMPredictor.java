package cs475.hmm;

import java.util.*;
import java.io.*;

public class HMMPredictor {

    // TODO Error handling for the case that we never see a bigram
    // in training, but we do in prediction
    // NOTE: Probabilities are all log probabilities
    private HashMap<String, Double> transmissionProbabilities;
    private HashMap<Character, Double> firstLetterProbabilities;
    private EmissionProbabilities emissions;
    private Predictor letterPredictor;
    private string trainingCorpus;

    public HMMPredictor(Predictor p, String corpusLocation) {
        transmissionProbabilities = new HashMap<String, Double>();
        firstLetterProbabilities = new HashMap<Character, Double>();
        emissions = new EmissionProbabilities();
        letterPredictor = p;
        trainingCorpus = corpusLocation;
    }

    // TODO right now we are learning P(x | z), but it may
    // be useful to use Bayes theorem instead. In this case,
    // we could learn probability of P(z | x) - this is the probability
    // of actual label given what the ANN predicted, a perhaps better model
    // for the ANN. We would also learn P(x) - the probability of seeing the
    // letter that the ANN predicted (which would be learned from the corpus).
    private void learnEmissionProbabilities(List<Instance> letterInstances) {
        for(Instance instance: letterInstances) {
            Label x = letterPredictor.predict(instance);
            Label z = instance.getLabel();
            emissions.addObservation(x, z);
        }
        // TODO make sure these are log probs
        emissions.calculateProbabilities();
    }

    // TODO For now we use no smoothing
    private void learnTransitionProbabilities(File corpus) {
        BufferedReader reader = new BufferedReader(new FileReader(corpus));
        double wordCount = 0;
        double bigramCount = 0;
        HashMap<String, Double> bigramCounts = new HashMap<String, Double>();
        HashMap<Character, Double> firstLetterCounts = new HashMap<Character, Double>();
        try {
            boolean done = false;
            while (!done) {
                String line = reader.readLine();
                if (line == null) {
                    done = true;
                } else {
                    String[] words = line.split(" ");
                    for (int i = 0; i < words.length; ++i) {
                        bigramCount += updateTransmissionProbabilities(words[i], bigramCounts, firstLetterCounts);
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
        for (String bigram : bigramCounts.keySet()) {
            Double count = transmissionProbabilities.get(bigram);
            Double prob = Math.log(count / bigramCount);
            transmissionProbabilities.put(bigram, prob);
        }
        for (Character c : firstLetterCounts) {
            Double count = firstLetterCounts.get(c);
            Double prob = Math.log(count / wordCount);
            firstLetterProbabilities.put(c, prob);
        }

    }

    // returns the number of bigrams in the word
    private double updateTransmissionProbabilities(String word, HashMap<String, Double> bigramCounts, HashMap<Character, Double> firstLetterCounts) {
        word = word.trim();
        double bigramCount = 0;
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
            ++bigramCount;
        }
        return bigramCount;
    }

    public void train(List<Instance> letterInstances) {
        learnTransitionProbabilities(new File(corpusLocation));
        learnEmissionProbabilities(letterInstances);
    }

    // TODO figure out types for letters, words, labels, etc.
    // My gut feeling is that we should create a CharacterLabel class.
    public List<Label> predictWord(List<Instance> lettersInWord) {
        // This is the list of the letters predicted by the ANN.
        List<Label> observedLabels = new ArrayList<Label>(lettersInWord.size());
        int i = 0;
        for (Instance letter : lettersInWord) {
            observedLabels.set(i, letterPredictor.predict(letter));
            i += 1;
        }
    }

    // An implementation of the Viterbi algorithm adapted from Wikipedia
    // http://en.wikipedia.org/wiki/Viterbi_algorithm
    // TODO we probably want to convert to log probabilities later
    private String predictWord(List<Character> observedCharacters) {
        int wordLength = observedCharacters.size();
        double[][] maxProbs = new double[26][wordLength];
        int[][] maxProbChars = new int[26][wordLength];
        int aVal = (int) 'a';
        for (int character = 0; character < 26; ++character) {
            Character current = new Character((char) aVal + character);
            maxProbs[character][0] = firstLetterProbabilities.get(current);
            maxProbChars[character][0] = 0;
        }
        for (int position = 0; position < wordLength; ++position) {
            for (int curChar = 0; curChar < 26; ++curChar) {
                int maxProbPrevChar = 0;
                double maxProb = 0;
                for (int prevChar = 0; prevChar < 26; ++prevChar) {
                    Character prev = (char) aVal + prevChar;
                    Character current = (char) aVal + curChar;
                    String bigram = "" + prev + current;
                    double Aij = transmissionProbabilities.get(bigram);
                    // The probability of observing observedCharacter[position]
                    // if the real letter is curChar
                    double Bjyi = emissions.getProbability(current, observedCharacters.get(position));
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
        int maxProb = 0;
        int maxProbLastChar = 0;
        for (int character = 0; character < 26; ++character) {
            if (maxProb < maxProbs[character][wordLength - 1]) {
                maxProb = maxProbs[character][wordLength - 1];
                maxProbLastChar = character;
            }
        }
        StringBuilder word = new StringBuilder();
        word.append((char) maxProbLastChar + aVal);
        int previousChar = maxProbChars[maxProbLastChar][wordLength - 1];
        word.append((char) previousChar + aVal);
        for (int position = wordLength - 2; position > 0; --position) {
            previousChar = maxProbChars[previousChar][position];
            word.append((char) previousChar + aVal);
        }
        word = word.reverse();
        return word.toString();
    }
}
