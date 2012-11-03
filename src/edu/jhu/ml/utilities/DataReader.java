package edu.jhu.ml.utilities;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import edu.jhu.ml.data.FeatureVector;
import edu.jhu.ml.data.Instance;
import edu.jhu.ml.data.label.ClassificationLabel;

/**
 * This class is responsible for reading the input data. Since the
 * input files we are using are so large, it needs to be read in chunks if 
 * we are planning on using all of the data.
 * 
 * @author Daniel Deutsch
 */
public class DataReader
{
	/**
	 * The total number of instances in the file to be read. Used for
	 * calculating the percent of examples to be read.
	 */
	private int numberOfInstances;
	
	/**
	 * The total number of instances that have already been read.
	 */
	private int numberInstancesRead;
	
	/**
	 * The scanner that will read the file.
	 */
	private Scanner scanner;
	
	/**
	 * Indicates whether or not the file to be read contains labels. This
	 * should be true for training data and false for test data.
	 */
	private boolean hasLabels;
	
	/**
	 * The constructor for the class.
	 * @param file The name of the file to be read.
	 * @param hasLabels Indicates whether or not the file will have labels.
	 */
	public DataReader(String file, boolean hasLabels)
	{
		this.numberOfInstances = 0;
		this.numberInstancesRead = 0;
		this.hasLabels = hasLabels;
		
		try
		{
			Scanner countScanner = new Scanner(new FileReader(file));
			while (countScanner.hasNextLine())
			{
				countScanner.nextLine();
				this.numberOfInstances++;
			}

			this.scanner = new Scanner(new FileReader(file));
		}
		catch (FileNotFoundException e)
		{
			System.err.println("Can't find the data file, " + file);
			e.printStackTrace();
		}
	}
	
	/**
	 * Reads a specific number of instances from the data file.
	 * @param numInstances The number of instances to read.
	 * @return A list of all of the instances read.
	 */
	public List<Instance> readNumberOfInstances(int numInstances)
	{
		List<Instance> instances = new ArrayList<Instance>();
		
		for (int count = 0; count < numInstances; count++)
		{
			if (!this.scanner.hasNextLine())
				break;
			
			String line = this.scanner.nextLine();
			String[] items = line.split(",");

			FeatureVector featureVector = new FeatureVector();
			
			ClassificationLabel label = null;
			if (this.hasLabels)
				label = new ClassificationLabel(Integer.parseInt(items[0]));
			else
				featureVector.add(0, Integer.parseInt(items[0]));
			
			for (int i = 1; i < items.length; i++)
			{
				if (Integer.parseInt(items[i]) != 0)
					featureVector.add(i, Integer.parseInt(items[i]));
			}
			
			instances.add(new Instance(featureVector, label));
			this.numberInstancesRead++;
		}
		
		return instances;
	}
	
	/**
	 * Reads a specific percent of instances from the data file.
	 * @param percent The percent to read.
	 * @return The list of instances read.
	 */
	public List<Instance> readPercentageOfInstances(double percent)
	{
		int number = (int) Math.round(this.numberOfInstances * percent);
		return this.readNumberOfInstances(number);
	}
	
	/**
	 * Reads in all of the remaining instances to be read.
	 * @return The instances.
	 */
	public List<Instance> readAllInstances()
	{
		return this.readNumberOfInstances(this.numberOfInstances - this.numberInstancesRead);
	}
}
