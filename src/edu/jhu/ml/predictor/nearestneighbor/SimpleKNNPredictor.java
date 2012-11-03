package edu.jhu.ml.predictor.nearestneighbor;

import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

import edu.jhu.ml.data.Instance;
import edu.jhu.ml.data.Pair;
import edu.jhu.ml.utilities.DataReader;

/**
 * This class represents the k-nearest neighbors algorithm which computes
 * the label for the instance to classify based on the average labels of the
 * k nearest neighbors.
 * 
 * @author Daniel Deutsch
 */
public class SimpleKNNPredictor extends KNNPredictor
{
	/**
	 * Required to serialize the object.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The number of neighbors to use in prediction.
	 */
	private int k;

	/**
	 * The constructor for the class.
	 * @param instances The training instances.
	 * @param k The number of neighbors to use.
	 */
	public SimpleKNNPredictor(DataReader dataReader, int k)
	{
		super(dataReader);
		this.k = k;
	}
	
	/**
	 * Gets the k nearest instances to the argument Instance.
	 * @param instance The instance.
	 * @return A list of the k nearest instances.
	 */
	protected List<Pair<Double, Instance>> getNearest(Instance instance)
	{
		PriorityQueue<Pair<Double, Instance>> nearest = new PriorityQueue<Pair<Double, Instance>>(this.instances.size());
		
		for (Instance other : this.instances)
		{
			double distance = this.distance(instance, other);
			nearest.add(new Pair<Double, Instance>(distance, other));
		}
		
		List<Pair<Double, Instance>> result = new LinkedList<Pair<Double, Instance>>();
		for (int i = 0; i < k; i++)
			result.add(nearest.remove());
		
		return result;
	}
}
