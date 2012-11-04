package edu.jhu.ml.evaluator;

import java.util.List;

import edu.jhu.ml.data.Instance;
import edu.jhu.ml.data.label.ClassificationLabel;
import edu.jhu.ml.data.label.RegressionLabel;
import edu.jhu.ml.predictor.Predictor;
import edu.jhu.ml.utilities.DataReader;

public class RegressionEvaluator extends Evaluator
{
	public double evaluate(DataReader reader, Predictor predictor)
	{
		return this.evaluate(reader.readAllInstances(), predictor);
	}
	
	private double evaluate(List<Instance> instances, Predictor predictor)
	{
		double error = 0;
		for (Instance instance : instances)
		{
			double predictedLabel = ((RegressionLabel) predictor.predict(instance)).getLabel();
			double trueLabel = ((ClassificationLabel) instance.getLabel()).getLabel();
			
			error += Math.abs(predictedLabel - trueLabel);
		}
		
		error = error / instances.size();
		return error;
	}

}
