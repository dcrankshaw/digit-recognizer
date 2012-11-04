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

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import edu.jhu.ml.evaluator.AccuracyEvaluator;
import edu.jhu.ml.evaluator.Evaluator;
import edu.jhu.ml.evaluator.RegressionEvaluator;
import edu.jhu.ml.predictor.Predictor;
import edu.jhu.ml.predictor.nearestneighbor.DistanceWeightedKNNPredictor;
import edu.jhu.ml.predictor.nearestneighbor.EpsilonBallKNNPredictor;
import edu.jhu.ml.predictor.nearestneighbor.SimpleKNNPredictor;
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
		
		int k = 5;
		if (CommandLineUtilities.hasArg("k"))
			k = CommandLineUtilities.getOptionValueAsInt("k");
		
		double epsilon = 0.1;
		if (CommandLineUtilities.hasArg("epsilon"))
			epsilon = CommandLineUtilities.getOptionValueAsFloat("epsilon");
		
		if (mode.equals("train"))
		{
			Predictor predictor = null;
			
			if (algorithm.equals("neural_network"))
				predictor = null;
			else if (algorithm.equals("knn"))
				predictor = new SimpleKNNPredictor(data, k);
			else if (algorithm.equals("knn_distance"))
				predictor = new DistanceWeightedKNNPredictor(data, k);
			else if (algorithm.equals("knn_epsilon"))
				predictor = new EpsilonBallKNNPredictor(data, epsilon);
			
			predictor.train();
			Classify.saveObject(predictor, model);
		}
		else if (mode.equals("test"))
		{
			// Will pass true here because we will be testing it on CV
			DataReader reader = new DataReader(data, true);
			
			Predictor predictor = (Predictor) Classify.loadObject(model);
			
			Evaluator evaluator = null;
			if (algorithm.equals("neural_network"))
				evaluator = new AccuracyEvaluator();
			else
				evaluator = new RegressionEvaluator();
			
			evaluator.evaluate(reader, predictor);
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
		Classify.registerOption("k", "String", true, "The number of neighbors for k-Nearest Neighbors.");
		Classify.registerOption("epsilon", "String", true, "The distance to search for the k-Nearest Neighbors epsilon-ball algorithm.");
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
