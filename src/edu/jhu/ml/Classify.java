package edu.jhu.ml;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import edu.jhu.ml.data.Instance;
import edu.jhu.ml.evaluator.AccuracyEvaluator;
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
	
	public static void main(String[] args)
	{
		String[] mandatoryArgs = {"mode", "algorithm", "data", "model"};
		Classify.createCommandLineOptions();
		CommandLineUtilities.initCommandLineParameters(args, Classify.options, mandatoryArgs);
		
		String mode = CommandLineUtilities.getOptionValue("mode");
		String algorithm = CommandLineUtilities.getOptionValue("algorithm");
		String data = CommandLineUtilities.getOptionValue("data");
		String model = CommandLineUtilities.getOptionValue("model");
		
		String testMethod = "letter_accuracy";
		if (CommandLineUtilities.hasArg("test_method"))
			testMethod = CommandLineUtilities.getOptionValue("test_method");
		
		DataReader reader = new DataReader(data);
		List<Instance> instances = reader.read();
		
		System.out.println("Read in data");
		
		if (mode.equals("train"))
		{
			Predictor predictor = null;
			
			if (algorithm.equals("neural_network"))
				predictor = new NeuralNetwork();
			
			predictor.train(instances);
			Classify.saveObject(predictor, model);
		}
		else if (mode.equals("test"))
		{			
			Predictor predictor = (Predictor) Classify.loadObject(model);
			
			AccuracyEvaluator evaluator = null;
			if (algorithm.equals("neural_network"))
				evaluator = new AccuracyEvaluator(predictor);
			
			if (testMethod.equals("letter_accuracy") || testMethod.equals("all"))
				System.out.println("Letter accuracy: " + evaluator.evaluateLetterAccuracy(instances, 1));
			if (testMethod.equals("whole_word_accuracy") || testMethod.equals("all"))
				System.out.println("Whole word accuracy: " + evaluator.evaluateWholeWordAccuracy(CommandLineUtilities.getOptionValue("word_file")));
		}
	}
	
	/**
	 * Adds all of the possible command line arguments.
	 */
	public static void createCommandLineOptions()
	{
		Classify.registerOption("mode", "String", true, "Operating mode: train or test.");
		Classify.registerOption("data", "String", true, "The path to the data file.");
		Classify.registerOption("algorithm", "String", true, "The algorithm to use.");
		Classify.registerOption("model", "String", true, "The path to the model file.");
		Classify.registerOption("test_method", "String", true, "The accuracy evaluation metric.");
		Classify.registerOption("word_file", "String", true, "The file of words to test.");
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
