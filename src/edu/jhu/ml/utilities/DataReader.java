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
	 * The scanner that will read the file.
	 */
	private Scanner scanner;

	/**
	 * The constructor for the class.
	 * @param file The file where the data is.
	 */
	public DataReader(String file)
	{
		try
		{
			this.scanner = new Scanner(new FileReader(file));
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Reads in all of the data.
	 * @return A list of all of the Instances.
	 */
	public List<Instance> read()
	{
		List<Instance> instances = new ArrayList<Instance>();
		
		while (scanner.hasNextLine())
		{
			String line = scanner.nextLine();
			
			Scanner lineScanner = new Scanner(line);
			
			char label = lineScanner.next().charAt(0);
			FeatureVector featureVector = new FeatureVector();
			
			int index = 0;
			while (lineScanner.hasNext())
			{
				int feature = lineScanner.nextInt();
				if (feature != 0)
					featureVector.add(index, feature);
				index++;
			}
			
			instances.add(new Instance(featureVector, new ClassificationLabel(label - 'a')));
		}
		
		return instances;
	}
}
