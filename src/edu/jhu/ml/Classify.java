package edu.jhu.ml;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import edu.jhu.ml.data.Instance;
import edu.jhu.ml.data.RandomWordGenerator;
import edu.jhu.ml.evaluator.AccuracyEvaluator;
import edu.jhu.ml.hmm.HMMPredictor;
import edu.jhu.ml.predictor.NeuralNetwork;
import edu.jhu.ml.predictor.Predictor;
import edu.jhu.ml.utilities.CommandLineUtilities;
import edu.jhu.ml.utilities.DataReader;

/**
 * This class is responsible for controlling the training and testing
 * of the classes. It works the same way that the class's homework
 * assignments work. 
 * 
 * @author Daniel Deutsch
 */
public class Classify
{
    /**
     * A list of the command line arguments.
     */
    private static LinkedList<Option> options = new LinkedList<Option>();
    
    public static String ann_model_input = null;
    public static String ann_training_data = "data/all/nnTrain.txt";
    public static String hmm_letter_training_data = "data/all/hmmTrain.txt";
    public static String train_corpus = "data/corpus/train_corpus.txt";
    public static String test_corpus = "data/corpus/test_corpus.txt";
    public static String test_letter_directory = "data/letters/";
    public static String output_directory = "data/output/";
    public static String ann_model_saved = "data/models/ann.model";

    
    
    public static void main(String[] args) throws FileNotFoundException
    {
        /*
        "ann_training_data", "String", true, "The location of the training letters for the ANN.");
        "hmm_letter_training_data", "String", true, "The location of the training letters for the HMM.");
        "train_corpus", "String", true, "Location of the training corpus for the HMM.");
        "test_corpus", "String", true, "Location of the test corpus to draw random words from.");
        "test_letter_directory", "String", true, "Location of the directory containing the test letters. Assumes files contained are named $LETTER.txt");
        "output_directory", "String", true, "Output location. Will contain 3 files corresponding to the 3 word size buckets.");
         */
    	
    	String[] mandatoryArgs = {};
        Classify.createCommandLineOptions();
        CommandLineUtilities.initCommandLineParameters(args, Classify.options, mandatoryArgs);
    	
    	
    	if (CommandLineUtilities.hasArg("ann_model_input")) {
    		ann_model_input = CommandLineUtilities.getOptionValue("ann_model_input");
    	}
    	if (CommandLineUtilities.hasArg("ann_training_data")) {
    		ann_training_data = CommandLineUtilities.getOptionValue("ann_training_data");
    	}
    	if (CommandLineUtilities.hasArg("hmm_letter_training_data")) {
    		hmm_letter_training_data = CommandLineUtilities.getOptionValue("hmm_letter_training_data");
    	}
    	if (CommandLineUtilities.hasArg("train_corpus")) {
    		train_corpus = CommandLineUtilities.getOptionValue("train_corpus");
    	}
    	if (CommandLineUtilities.hasArg("test_corpus")) {
    		test_corpus = CommandLineUtilities.getOptionValue("test_corpus");
    	}
    	if (CommandLineUtilities.hasArg("test_letter_directory")) {
    		test_letter_directory = CommandLineUtilities.getOptionValue("test_letter_directory");
    	}
    	if (CommandLineUtilities.hasArg("output_directory")) {
    		output_directory = CommandLineUtilities.getOptionValue("output_directory");
    	}
    	if (CommandLineUtilities.hasArg("ann_model_saved")) {
    		ann_model_saved = CommandLineUtilities.getOptionValue("ann_model_saved");
    	}
    	
    	// Train the models
    	NeuralNetwork annPredictor = null;
    	if (ann_model_input != null) {
    		annPredictor = (NeuralNetwork) Classify.loadObject(ann_model_input);
    	} else {
    		// Train Neural Network and save
    		DataReader annTrainDataReader = new DataReader(ann_training_data);
    		List<Instance> instances = annTrainDataReader.read();
    		annPredictor = new NeuralNetwork(50, 50);
    		annPredictor.train(instances);
    		Classify.saveObject(annPredictor, ann_model_saved);
    	}
    	DataReader hmmTrainDataReader = new DataReader(hmm_letter_training_data);
    	List<Instance> letterInstances = hmmTrainDataReader.read();
    	HMMPredictor hmmPredictor = new HMMPredictor(annPredictor, train_corpus);
    	hmmPredictor.train(letterInstances);
    	
    	// Test the models
    	RandomWordGenerator smallGenerator = new RandomWordGenerator(2, 4, test_corpus, test_letter_directory);
    	AccuracyEvaluator smallEvaluator = new AccuracyEvaluator(annPredictor, hmmPredictor);
    	//for (int i = 0; i < smallGenerator.getWordCount() / 2.0; ++i) {
    	for (int i = 0; i < 10; ++i) {
    		smallEvaluator.evaluateWord(smallGenerator.randomWord());
    	}
    	
    	RandomWordGenerator mediumGenerator = new RandomWordGenerator(5, 8, test_corpus, test_letter_directory);
    	AccuracyEvaluator mediumEvaluator = new AccuracyEvaluator(annPredictor, hmmPredictor);
    	//for (int i = 0; i < mediumGenerator.getWordCount() / 2.0; ++i) {
    	for (int i = 0; i < 10; ++i) {
    		mediumEvaluator.evaluateWord(mediumGenerator.randomWord());
    	}
    	
    	RandomWordGenerator largeGenerator = new RandomWordGenerator(9, Integer.MAX_VALUE, test_corpus, test_letter_directory);
    	AccuracyEvaluator largeEvaluator = new AccuracyEvaluator(annPredictor, hmmPredictor);
    	//for (int i = 0; i < largeGenerator.getWordCount() / 2.0; ++i) {
    	for (int i = 0; i < 10; ++i) {	
    		largeEvaluator.evaluateWord(largeGenerator.randomWord());
    	}
    	
    	System.out.println("ANN Results");
    	System.out.println("Small word correctness: " + smallEvaluator.annWordCorrectness / smallEvaluator.totalWordsEvaluated);
    	System.out.println("Small words correct: " + smallEvaluator.annWordsCorrect + "/" + smallEvaluator.totalWordsEvaluated);
    	System.out.println("Small letters correct: " + smallEvaluator.annLettersCorrect + "/" + smallEvaluator.totalLettersEvaluated + "\n");
    	System.out.println("Medium word correctness: " + mediumEvaluator.annWordCorrectness / mediumEvaluator.totalWordsEvaluated);
    	System.out.println("Medium words correct: " + mediumEvaluator.annWordsCorrect + "/" + mediumEvaluator.totalWordsEvaluated);
    	System.out.println("Medium letters correct: " + mediumEvaluator.annLettersCorrect + "/" + mediumEvaluator.totalLettersEvaluated + "\n");
    	System.out.println("Large word correctness: " + largeEvaluator.annWordCorrectness / largeEvaluator.totalWordsEvaluated);
    	System.out.println("Large words correct: " + largeEvaluator.annWordsCorrect + "/" + largeEvaluator.totalWordsEvaluated);
    	System.out.println("Large letters correct: " + largeEvaluator.annLettersCorrect + "/" + largeEvaluator.totalLettersEvaluated + "\n");
    	
    	System.out.println("\n\nHMM Results");
    	System.out.println("Small word correctness: " + smallEvaluator.hmmWordCorrectness / smallEvaluator.totalWordsEvaluated);
    	System.out.println("Small words correct: " + smallEvaluator.hmmWordsCorrect + "/" + smallEvaluator.totalWordsEvaluated);
    	System.out.println("Small letters correct: " + smallEvaluator.hmmLettersCorrect + "/" + smallEvaluator.totalLettersEvaluated + "\n");
    	System.out.println("Medium word correctness: " + mediumEvaluator.hmmWordCorrectness / mediumEvaluator.totalWordsEvaluated);
    	System.out.println("Medium words correct: " + mediumEvaluator.hmmWordsCorrect + "/" + mediumEvaluator.totalWordsEvaluated);
    	System.out.println("Medium letters correct: " + mediumEvaluator.hmmLettersCorrect + "/" + mediumEvaluator.totalLettersEvaluated + "\n");
    	System.out.println("Large word correctness: " + largeEvaluator.hmmWordCorrectness / largeEvaluator.totalWordsEvaluated);
    	System.out.println("Large words correct: " + largeEvaluator.hmmWordsCorrect + "/" + largeEvaluator.totalWordsEvaluated);
    	System.out.println("Large letters correct: " + largeEvaluator.hmmLettersCorrect + "/" + largeEvaluator.totalLettersEvaluated + "\n");
    	
    	
    	
    	
    	
    	
    	
    	
    	
//
//        String mode = CommandLineUtilities.getOptionValue("mode");
//        String algorithm = CommandLineUtilities.getOptionValue("algorithm");
//        String data = CommandLineUtilities.getOptionValue("data");
//        String model = CommandLineUtilities.getOptionValue("model");
//
//        String testMethod = "letter_accuracy";
//        if (CommandLineUtilities.hasArg("test_method"))
//            testMethod = CommandLineUtilities.getOptionValue("test_method");
//
//        DataReader reader = new DataReader(data);
//        List<Instance> instances = reader.read();
//
//        System.out.println("Read in data");
//
//        if (mode.equals("train"))
//        {
//            Predictor predictor = null;
//
//            if (algorithm.equals("neural_network"))
//                predictor = new NeuralNetwork(50, 50);
//
//            predictor.train(instances);
//            Classify.saveObject(predictor, model);
//        }
//        else if (mode.equals("test"))
//        {			
//            Predictor predictor = (Predictor) Classify.loadObject(model);
//
//            AccuracyEvaluator evaluator = null;
//            if (algorithm.equals("neural_network"))
//                evaluator = new AccuracyEvaluator(predictor);
//
//            if (testMethod.equals("letter_accuracy") || testMethod.equals("all"))
//                System.out.println("Letter accuracy: " + evaluator.evaluateLetterAccuracy(instances, 1));
//            if (testMethod.equals("whole_word_accuracy") || testMethod.equals("all"))
//                System.out.println("Whole word accuracy: " + evaluator.evaluateWholeWordAccuracy(CommandLineUtilities.getOptionValue("word_file")));
//        }
    }
    

    /**
     * Adds all of the possible command line arguments.
     */
    public static void createCommandLineOptions()
    {
        //Classify.registerOption("mode", "String", true, "Operating mode: train or test.");
        //Classify.registerOption("data", "String", true, "The path to the data file.");
        //Classify.registerOption("algorithm", "String", true, "The algorithm to use.");
        Classify.registerOption("ann_model_input", "String", true, "The path to the model file.");
        Classify.registerOption("ann_model_saved", "String", true, "The path to save the ann model to.");
        //Classify.registerOption("test_method", "String", true, "The accuracy evaluation metric.");
        //Classify.registerOption("word_file", "String", true, "The file of words to test.");
        Classify.registerOption("ann_training_data", "String", true, "The location of the training letters for the ANN.");
        Classify.registerOption("hmm_letter_training_data", "String", true, "The location of the training letters for the HMM.");
        Classify.registerOption("train_corpus", "String", true, "Location of the training corpus for the HMM.");
        Classify.registerOption("test_corpus", "String", true, "Location of the test corpus to draw random words from.");
        Classify.registerOption("test_letter_directory", "String", true, "Location of the directory containing the test letters. Assumes files contained are named $LETTER.txt");
        Classify.registerOption("output_directory", "String", true, "Output location. Will contain 3 files corresponding to the 3 word size buckets.");
    }

    /**
     * Registers a command line option.
     * @param optionName The name of the option.
     * @param argName The type of the option.
     * @param hasArg Indicates whether or not it has a value associated with it.
     * @param description A description of the argument.
     */
    public static void registerOption(String optionName, String argName, boolean hasArg, String description)
    {
        OptionBuilder.withArgName(argName);
        OptionBuilder.hasArg(hasArg);
        OptionBuilder.withDescription(description);
        Option option = OptionBuilder.create(optionName);

        Classify.options.add(option);
    }

    /**
     * Writes an object to a file. 
     * @param object The object that implements serializable.
     * @param file_name The name of the file to save it.
     */
    public static void saveObject(Object object, String file_name)
    {
        try
        {
            ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(
                        new FileOutputStream(new File(file_name))));
            oos.writeObject(object);
            oos.close();
        }
        catch (IOException e)
        {
            System.err.println("Exception writing file " + file_name + ": " + e);
        }
    }

    /**
     * Loads a previously saved object from a file.
     * @param file_name The name of the file.
     * @return The object.
     */
    public static Object loadObject(String file_name)
    {
        ObjectInputStream ois;
        try
        {
            ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(new File(
                                file_name))));
            Object object = ois.readObject();
            ois.close();
            return object;
        }
        catch (IOException e)
        {
            System.err.println("Error loading: " + file_name);
        }
        catch (ClassNotFoundException e)
        {
            System.err.println("Error loading: " + file_name);
        }
        return null;
    }
}
