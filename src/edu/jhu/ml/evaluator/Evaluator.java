package edu.jhu.ml.evaluator;

import edu.jhu.ml.predictor.Predictor;
import edu.jhu.ml.utilities.DataReader;

public abstract class Evaluator
{
	public abstract double evaluate(DataReader reader, Predictor predictor);
}
