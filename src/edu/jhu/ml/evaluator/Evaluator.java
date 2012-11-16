package edu.jhu.ml.evaluator;

import java.util.List;

import edu.jhu.ml.data.Instance;
import edu.jhu.ml.predictor.Predictor;

public abstract class Evaluator
{
	public abstract double evaluate(List<Instance> instances, Predictor predictor);
}
