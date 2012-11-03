package edu.jhu.ml.evaluator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.jhu.ml.data.Instance;
import edu.jhu.ml.data.label.Label;
import edu.jhu.ml.predictor.Predictor;
import edu.jhu.ml.utilities.DataReader;

/**
 * An <code>AccuracyEvaluator</code> calculates the accuracy of a classifier
 * on a set of data. It will generate the percent of correctly classified
 * examples for each <code>Label</code> as well as the overall data set.
 * 
 * @author Daniel Deutsch
 */
public class AccuracyEvaluator extends Evaluator
{
	/**
	 * Evaluates the accuracy of the predictor on the data.
	 * @param reader The DataReader that has access to the data on file.
	 * @param predictor The predictor to evaluate.
	 * @return The decimal representing the accuracy of the predictor.
	 */
	public double evaluate(DataReader reader, Predictor predictor)
	{
		return this.evaluate(reader.readAllInstances(), predictor);
	}
	
	/**
	 * Evaluates the predictor on the given set of data and outputs the accuracies on
	 * each <code>Label</code> as well as the overall accuracy for the data.
	 * 
	 * @param instances The data to classify.
	 * @param predictor The predictor to evaluate.
	 * 
	 * @return The decimal representing the accuracy of the predictor.
	 */
	private double evaluate(List<Instance> instances, Predictor predictor)
	{
		ArrayList<Label> labels = new ArrayList<Label>(); 
		HashMap<Label, Integer> correctCounters = new HashMap<Label, Integer>();
		HashMap<Label, Integer> totalCounters = new HashMap<Label, Integer>();
					
		for (Instance instance : instances)
		{	
			Label label = instance.getLabel();
			if (label != null)
			{
				if (!labels.contains(label))
				{
					labels.add(label);
					correctCounters.put(label, 0);
					totalCounters.put(label, 0);
				}
				if (label.equals(predictor.predict(instance)))
					correctCounters.put(label, correctCounters.get(label) + 1);
				
				int value = totalCounters.get(label) + 1; 
				totalCounters.put(label, value);
			}
		}
		
		int numberCorrect = 0;
		int total = 0; 
		
		for (Label label : labels)
		{
			int labelCorrect = correctCounters.get(label);
			int labelTotal = totalCounters.get(label);
			
			numberCorrect += labelCorrect;
			total += labelTotal;
		}
			
		double result;
		if (total == 0)
			result = 0;
		else
			result = ((double) numberCorrect / total);

		return result;
	}

}
