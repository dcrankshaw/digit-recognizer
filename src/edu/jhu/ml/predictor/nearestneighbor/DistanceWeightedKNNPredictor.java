package edu.jhu.ml.predictor.nearestneighbor;

import java.util.List;

import edu.jhu.ml.data.Instance;
import edu.jhu.ml.data.Pair;
import edu.jhu.ml.data.label.Label;
import edu.jhu.ml.data.label.RegressionLabel;

/**
 * This class represents a k-nearest neighbor algorithm that weights the
 * vote of each of the k neighbors based on their distance to the
 * instance to predict.
 * 
 * @author Daniel Deutsch
 */
public class DistanceWeightedKNNPredictor extends SimpleKNNPredictor
{
	/**
	 * Required to serialize the object.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The constructor for the class.
	 * @param fileName The path to the data file.
	 * @param k The number of neighbors to consider.
	 */
	public DistanceWeightedKNNPredictor(String fileName, int k)
	{
		super(fileName, k);
	}

	/**
	 * Predicts a label for the given instance based on a weighting of the 
	 * neighbors based on their distance to the instance.
	 */
	public Label predictLabel(Instance instance, List<Pair<Double, Instance>> nearest)
	{
		double label = 0;
		double[] lambdas = this.calculateLambdas(nearest);

		// TODO Fix this. I think the labels will have to be -1 here.
		for (int i = 0; i < nearest.size(); i++)
			label += lambdas[i] * ((RegressionLabel) nearest.get(i).getValue().getLabel()).getLabel();

		return new RegressionLabel(label);
	}
	
	/**
	 * Calculates the expression 1 / (1 + x)
	 * @param pair The pair of the distance squared and the Instance.
	 * @return The resulting value.
	 */
	private double sim(Pair<Double, Instance> pair)
	{
		return 1 / (1 + pair.getKey());
	}
	
	/**
	 * Calculates all of the lambdas for each of the k neighbors where
	 * lambda_i = (sim(x, x_i)) / (sum over k neighbors(sim(x, x_j)))
	 * @param nearest The list of the nearest neighbors.
	 * @return The lambda values.
	 */
	private double[] calculateLambdas(List<Pair<Double, Instance>> nearest)
	{
		double[] lambdas = new double[nearest.size()];
		double sum = 0;
		
		for (int i = 0; i < nearest.size(); i++)
		{
			double value = sim(nearest.get(i));
			sum += value;
			lambdas[i] = value;
		}
		
		for (int i = 0; i < lambdas.length; i++)
			lambdas[i] = lambdas[i] / sum;
		
		return lambdas;
	}
}
