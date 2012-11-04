package edu.jhu.ml.predictor.nearestneighbor;

import java.util.ArrayList;
import java.util.List;

import edu.jhu.ml.data.Instance;
import edu.jhu.ml.data.Pair;

/**
 * This class represents the k-Nearest Neighbors epsilon-ball algorithm
 * implementation. It will consider all of the neighbors that are within a distance
 * of epsilon in order to predict the label 
 *  
 * @author Daniel Deutsch
 */
public class EpsilonBallKNNPredictor extends KNNPredictor
{
	/**
	 * Required to serialize the object. 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The size of the epsilon-ball.
	 */
	private double epsilon;
	
	/**
	 * The constructor for the predictor.
	 * @param fileName The location of the data file.
	 * @param epsilon The size of the epsilon-ball.
	 */
	public EpsilonBallKNNPredictor(String fileName, double epsilon)
	{
		super(fileName);
		this.epsilon = epsilon;
	}

	/**
	 * Gets the nearest neighbors that are within epsilon-distance from the
	 * argument instance.
	 * @return The nearest neighbors.
	 */
	protected List<Pair<Double, Instance>> getNearest(Instance instance)
	{
		List<Pair<Double, Instance>> nearest = new ArrayList<Pair<Double, Instance>>();
		
		for (Instance example : this.instances)
		{
			double distance = this.distance(instance, example);
			if (distance < this.epsilon)
				nearest.add(new Pair<Double, Instance>(distance, example));
		}
		
		
		return nearest;
	}

	
}
