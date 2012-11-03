package edu.jhu.ml.predictor.nearestneighbor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import edu.jhu.ml.data.Instance;
import edu.jhu.ml.data.Pair;
import edu.jhu.ml.data.label.Label;
import edu.jhu.ml.data.label.RegressionLabel;
import edu.jhu.ml.predictor.Predictor;
import edu.jhu.ml.utilities.DataReader;

/**
 * This class represents the implementation of a k-nearest neighbors
 * classification algorithm.
 * 
 * @author Daniel Deutsch
 */
public abstract class KNNPredictor extends Predictor
{
	/**
	 * Required to serialize the object.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The instances to use in prediction
	 */
	protected List<Instance> instances = new ArrayList<Instance>();
	
	/**
	 * The constructor of the class.
	 * @param instances The training instances.
	 * @param k The number of nearest neighbors to use.
	 */
	public KNNPredictor(DataReader dataReader)
	{
		super(dataReader);
	}
	
	/**
	 * Trains the instances. Here, it does nothing.
	 * @param instances The training instances.
	 */
	public void train()
	{
		// nothing
	}

	/**
	 * Predicts a RegressionLabel for the Instance.
	 * @param instance The Instance to classify.
	 */
	public Label predict(Instance instance)
	{
		List<Pair<Double, Instance>> nearest = getNearest(instance);
		return predictLabel(instance, nearest);
	}
	
	/**
	 * Gets the k nearest instances to the argument Instance.
	 * @param instance The instance.
	 * @return A list of the k nearest instances.
	 */
	protected abstract List<Pair<Double, Instance>> getNearest(Instance instance);
	
	/**
	 * Computes the distance between two instances based on their
	 * Euclidean distances.
	 * @param x1 The first instance.
	 * @param x2 The second instance.
	 * @return The Euclidean distance between them.
	 */
	protected double distance(Instance x1, Instance x2)
	{
		Set<Integer> indices = new TreeSet<Integer>();
//		for (Pair<Integer, Double> pair : x1.getFeatureVector())
//			indices.add(pair.getKey());
		
		for (Pair<Integer, Double> pair : x2.getFeatureVector())
			indices.add(pair.getKey());
	
		double distance = 0;
		for (Integer index : indices)
			distance += Math.pow(x1.getFeatureVector().get(index) - x2.getFeatureVector().get(index), 2);
			
		return Math.sqrt(distance);
	}
	
	/**
	 * Predicts a label based on the average of the labels of the 
	 * k nearest neighbors.
	 */
	protected Label predictLabel(Instance instance, List<Pair<Double, Instance>> nearest)
	{
		double label = 0;

		for (Pair<Double, Instance> pair : nearest)
			label += ((RegressionLabel) pair.getValue().getLabel()).getLabel();
		
		label = label / nearest.size();
		return new RegressionLabel(label);
	}
}
