package edu.jhu.ml.predictor;

import java.io.Serializable;

import edu.jhu.ml.data.Instance;
import edu.jhu.ml.data.label.Label;
import edu.jhu.ml.utilities.DataReader;

/**
 * Represents an object that can perform predictions on data.
 * 
 * @author Daniel Deutsch
 */
public abstract class Predictor implements Serializable
{
	/**
	 * Required to serialize the object.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The DataReader that has access to the data.
	 */
	protected DataReader dataReader;
	
	/**
	 * The constructor of the Predictor.
	 * @param reader The DataReader that has access to the data on file.
	 */
	public Predictor(DataReader reader)
	{
		this.dataReader = reader;
	}
	
	/**
	 * Trains the algorithm on a set of data. This method should take
	 * the DataReader responsible for reading in the data because the amount
	 * of data to be read could be too large to fit into memory.
	 */
	public abstract void train();

	/**
	 * Predicts a label for the given instance.
	 * @param instance The instance to predict.
	 * @return The label.
	 */
	public abstract Label predict(Instance instance);
}
