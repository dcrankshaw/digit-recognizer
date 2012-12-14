package edu.jhu.ml.evaluator;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import edu.jhu.ml.data.Instance;
import edu.jhu.ml.data.RandomWordGenerator;
import edu.jhu.ml.data.label.ClassificationLabel;
import edu.jhu.ml.data.label.Label;
import edu.jhu.ml.hmm.HMMPredictor;
import edu.jhu.ml.predictor.NeuralNetwork;
import edu.jhu.ml.predictor.Predictor;

/**
 * An <code>AccuracyEvaluator</code> calculates the accuracy of a classifier
 * on a set of data. It will generate the percent of correctly classified
 * examples for each <code>Label</code> as well as the overall data set.
 * 
 * @author Daniel Deutsch
 */
public class AccuracyEvaluator
{
    
	private NeuralNetwork annPredictor;
	
	private HMMPredictor hmmPredictor;
	
	public double totalLettersEvaluated;
	
	public double totalWordsEvaluated;
	
	public double annLettersCorrect;
	
	public double hmmLettersCorrect;
	
	public double annWordsCorrect;
	
	public double hmmWordsCorrect;
	
	public double annWordCorrectness;
	
	public double hmmWordCorrectness;
	
	
	
	public AccuracyEvaluator(NeuralNetwork ann, HMMPredictor hmm) {
		annPredictor = ann;
		hmmPredictor = hmm;
		totalLettersEvaluated = 0;
		totalWordsEvaluated = 0;
		annLettersCorrect = 0;
		hmmLettersCorrect = 0;
		annWordsCorrect = 0;
		hmmWordsCorrect = 0;
		annWordCorrectness = 0;
		hmmWordCorrectness = 0;
		
	}
	
	public void evaluateWord(List<Instance> letters) {
		String word = listOfInstancesToWord(letters);
		String hmmPrediction = hmmPredictor.predictWord(letters);
		StringBuilder annPartialPrediction = new StringBuilder();
		for (Instance i : letters) {
			annPartialPrediction.append(annPredictor.predictCharacter(i));
		}
		String annPrediction = annPartialPrediction.toString();
		//System.out.println("actual: " + word + " hmm: " + hmmPrediction + " ann: " + annPrediction);
		totalLettersEvaluated += word.length();
		++totalWordsEvaluated;
		if (hmmPrediction.length() != word.length() || annPrediction.length() != word.length()) {
			System.out.println("uh oh");
		}
		int annCharsCorrect = 0;
		int hmmCharsCorrect = 0;
		for (int i = 0; i < word.length(); ++i) {
			char actualChar = word.charAt(i);
			char annChar = annPrediction.charAt(i);
			char hmmChar = hmmPrediction.charAt(i);
			if (annChar == actualChar) {
				++annCharsCorrect;
			}
			if (hmmChar == actualChar) {
				++hmmCharsCorrect;
			}
		}
		annLettersCorrect += annCharsCorrect;
		hmmLettersCorrect += hmmCharsCorrect;
		if (annCharsCorrect == word.length()) {
			++annWordsCorrect;
		}
		if (hmmCharsCorrect == word.length()) {
			++hmmWordsCorrect;
		}
		annWordCorrectness += (double) annCharsCorrect / word.length();
		hmmWordCorrectness += (double) hmmCharsCorrect / word.length();
	}
	
	private static String listOfInstancesToWord(List<Instance> letters) {
		StringBuilder word = new StringBuilder();
		for (Instance i : letters) {
			char letter = (char) (((ClassificationLabel) i.getLabel()).getLabel() + 'a');
			word.append(letter);
		}
		return word.toString();
	}
	
//	
//	
//	
//	
//	/**
//     * The predictor.
//     */
//    private Predictor predictor;
//
//    /**
//     * The constructor.
//     * @param p The predictor to use.
//     */
//    public AccuracyEvaluator(Predictor p)
//    {
//        this.predictor = p;
//    }
//
//    /**
//     * Evaluates the predictor on the given set of data and outputs the accuracies on
//     * each <code>Label</code> as well as the overall accuracy for the data.
//     * 
//     * @param instances The data to classify.
//     * @param predictor The predictor to evaluate.
//     * @param number The top number that the true answer has to be in.
//     * 
//     * @return The decimal representing the accuracy of the predictor.
//     */
//    public double evaluateLetterAccuracy(List<Instance> instances, int number)
//    {
//        ArrayList<Label> labels = new ArrayList<Label>(); 
//        HashMap<Label, Integer> correctCounters = new HashMap<Label, Integer>();
//        HashMap<Label, Integer> totalCounters = new HashMap<Label, Integer>();
//
//        for (Instance instance : instances)
//        {
//            Label label = instance.getLabel();
//            if (label != null)
//            {
//                if (!labels.contains(label))
//                {
//                    labels.add(label);
//                    correctCounters.put(label, 0);
//                    totalCounters.put(label, 0);
//                }
//
//                if (this.inTopAmount(instance, number))
//                    correctCounters.put(label, correctCounters.get(label) + 1);
//
//                int value = totalCounters.get(label) + 1; 
//                totalCounters.put(label, value);
//            }
//        }
//
//        int numberCorrect = 0;
//        int total = 0; 
//
//        for (Label label : labels)
//        {
//            int labelCorrect = correctCounters.get(label);
//            int labelTotal = totalCounters.get(label);
//
//            numberCorrect += labelCorrect;
//            total += labelTotal;
//        }
//
//        double result;
//        if (total == 0)
//            result = 0;
//        else
//            result = ((double) numberCorrect / total);
//
//        return result;
//    }
//
//    /**
//     * Checks to see if the predictor got the Instance right in the top number, 
//     * like the top 5.
//     * @param instance The Instance.
//     * @param number The top number to check.
//     * @return True if it did, false otherwise.
//     */
//    public boolean inTopAmount(Instance instance, int number)
//    {
//        int[] prediction = predictor.getTopProbabilities(instance, number);
//
//        for (int i = 0; i < number; i++)
//        {
//            if (((ClassificationLabel) instance.getLabel()).getLabel() == prediction[i])
//                return true;
//        }
//
//        return false;
//    }
//
//
//    /**
//     * Evaluates how many words the predictor gets completely correct. It will randomly
//     * generate the data from the word file.
//     * @param wordFile The file with the list of words.
//     * @return The number of words it gets completely correct over the total number of words.
//     */
//    public double evaluateWholeWordAccuracy(String wordFile)
//    {
//        int total = 0;
//        int correct = 0;
//
//        try
//        {
//            Scanner scanner = new Scanner(new FileReader(wordFile));
//            while (scanner.hasNextLine())
//            {
//                List<Instance> word = RandomWordGenerator.randomWord();
//                if (this.testWord(word))
//                    correct++;
//                total++;
//            }
//        }
//        catch (FileNotFoundException e)
//        {
//            e.printStackTrace();
//        }
//
//        return (double) correct / total;
//    }
//
//    /**
//     * Tests to see if the whole word was done correctly.
//     * @param word The word.
//     * @return True if it was, false otherwise.
//     */
//    private boolean testWord(List<Instance> word)
//    {
//        for (Instance letter : word)
//        {
//            if (!this.inTopAmount(letter, 1))
//                return false;
//        }
//        return true;
//    }
}
