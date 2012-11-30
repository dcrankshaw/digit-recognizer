package edu.jhu.ml.predictor;

import java.io.Serializable;
import java.util.List;

import edu.jhu.ml.data.Instance;
import edu.jhu.ml.data.label.ClassificationLabel;

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
	 * Trains the predictor on the parameter Instances.
	 * @param instances The instances.
	 */
	public abstract void train(List<Instance> instances);

	/**
	 * Predicts a label for the given instance.
	 * @param instance The instance to predict.
	 * @return The label.
	 */
	public abstract ClassificationLabel predict(Instance instance);
	
	/**
	 * Gets the top probabilities for the guess of the label for the Instance.
	 * @param instance The Instance.
	 * @param number The number of results to return.
	 * @return The index of the characters according to their probabilities.
	 */
	public abstract int[] getTopProbabilities(Instance instance, int number);
}
