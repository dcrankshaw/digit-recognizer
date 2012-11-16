package edu.jhu.ml.predictor;

import java.util.List;
import java.util.Random;

import Jama.Matrix;
import edu.jhu.ml.data.Instance;
import edu.jhu.ml.data.Pair;
import edu.jhu.ml.data.label.ClassificationLabel;
import edu.jhu.ml.data.label.Label;

/**
 * The implementation of an artificial neural network which will be used
 * to perform handwritten character recognition.
 * 
 * @author Daniel Deutsch
 */
public class NeuralNetwork extends Predictor
{
	/**
	 * Required to serialize the object.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The number of nodes in the input layer of the neural network.
	 */
	private static final int INPUT_SIZE = 128;
	
	/**
	 * The number of hidden nodes in the only hidden layer
	 * of the neural network.
	 */
	private static final int HIDDEN_SIZE = 25;
	
	/**
	 * The number of output nodes of the neural network.
	 */
	private static final int OUTPUT_SIZE = 26;
	
	/**
	 * The transition weights from the input layer to the first hidden layer.
	 */
	private Matrix firstWeights;
	
	/**
	 * The transition weights from the hidden layer to the output layer.
	 */
	private Matrix secondWeights;
	
	/**
	 * The column vector which represents the values of the hidden nodes.
	 */
	private Matrix hiddenNodes;
	
	/**
	 * Corresponds to Delta1. Position (i,j) contains the gradient of parameter (i,j).
	 */
	private Matrix firstGradient;
	
	/**
	 * Corresponds to Delta2. Position (i,j) contains the gradient of parameter (i,j).
	 */
	private Matrix secondGradient;
	
	public NeuralNetwork()
	{
		Random random = new Random();
		
		this.firstWeights = new Matrix(HIDDEN_SIZE, INPUT_SIZE);
		this.secondWeights = new Matrix(OUTPUT_SIZE, HIDDEN_SIZE);
		
		// perform random initialization between -1 and 1
		for (int i = 0; i < HIDDEN_SIZE; i++)
		{
			for (int j = 0; j < INPUT_SIZE; j++)
				this.firstWeights.set(i, j, random.nextDouble() * 2 - 1);
		}
		
		for (int i = 0; i < OUTPUT_SIZE; i++)
		{
			for (int j = 0; j < HIDDEN_SIZE; j++)
				this.secondWeights.set(i, j, random.nextDouble() * 2 - 1);
		}
	}
	
	/**
	 * Trains the neural network on the given Instances.
	 * @param instances The Instances to train on.
	 */
	public void train(List<Instance> instances)
	{
		int iterations = 25;
		double learningRate = 0.01;
		
		for (int i = 0; i < iterations; i++)
		{
			this.backPropagation(instances);
			this.firstWeights = this.firstWeights.plus(this.firstGradient.times(learningRate));
			this.secondWeights = this.secondWeights.plus(this.secondGradient.times(learningRate));
			
			System.out.println("Done iteration: " + i);
//			System.out.println(this.calculateTotalCost(instances));
		}
	}
	
	/**
	 * Calculates the current cost function for the neural network.
	 * @param instances All of the Instances to use.
	 * @return The cost.
	 */
	private double calculateTotalCost(List<Instance> instances)
	{
		double cost = 0;
		for (Instance instance : instances)
			cost += this.calculateCost(instance);
		
		return (-1 / instances.size()) * cost;
	}
	
	/**
	 * Calculates the cost that this instances contributes to the overall
	 * cost of the neural network.
	 * @param instance The instance.
	 * @return The cost.
	 */
	private double calculateCost(Instance instance)
	{
		double[] prediction = this.forwardPropagation(instance);
		int label = ((ClassificationLabel) instance.getLabel()).getLabel();
		
		double cost = 0;
		for (int i = 0; i < OUTPUT_SIZE; i++)
		{
			if (i == label)
				cost += Math.log(prediction[i]);
			else
				cost += Math.log(1 - prediction[i]);
		}
		
		return cost;
	}
	
	/**
	 * Performs the forward propagation algorithm on the neural network.
	 * @param instance The instance to be used as input.
	 * @return The output array.
	 */
	private double[] forwardPropagation(Instance instance)
	{
		Matrix input = this.convertInstanceToMatrix(instance);
		this.hiddenNodes = this.firstWeights.times(input);
		this.sigmoid(this.hiddenNodes);
		
		Matrix output = this.secondWeights.times(this.hiddenNodes);
		this.sigmoid(output);
		
		double[] array = new double[OUTPUT_SIZE];
		for (int i = 0; i < OUTPUT_SIZE; i++)
			array[i] = output.get(i, 0);
		
		return array;
	}
	
	/**
	 * Performs the backpropagation algorithm. 
	 * @param instances The training Instances.
	 */
	private void backPropagation(List<Instance> instances)
	{
		// Delta1
		// the delta for the transition from the input layer to the hidden layer
		this.firstGradient = new Matrix(new double[HIDDEN_SIZE][INPUT_SIZE]);
		
		// Delta2
		// the delta for the transition from the hidden layer to the output layer
		this.secondGradient = new Matrix(new double[OUTPUT_SIZE][HIDDEN_SIZE]);
		
		for (Instance instance : instances)
		{
			// a1
			Matrix input = this.convertInstanceToMatrix(instance);
			
			// a2
			// this.hiddenNodes
			
			// a3
			Matrix prediction = this.convertArrayToMatrix(this.forwardPropagation(instance));
			
			// y
			Matrix actualLabel = this.convertLabelToMatrix((ClassificationLabel) instance.getLabel());
			
			// d3
			Matrix thirdError = prediction.minus(actualLabel);
			
			// d2
			Matrix secondError = this.elementWiseMultiplication(this.secondWeights.transpose().times(thirdError), this.sigmoidGradient(this.hiddenNodes));
		
			// update deltas
			// Delta1 = Delta1 + d2*a1^T
			this.firstGradient = this.firstGradient.plus(secondError.times(input.transpose()));
			
			// Delta2 = Delta2 + d3*a2^T
			this.secondGradient = this.secondGradient.plus(thirdError.times(this.hiddenNodes.transpose()));			
		}
	}
	
	/**
	 * Performs element-wise multiplication of two matrices, where the resulting
	 * matrix M(i,j) = m1(i,j) * m2(i,j).
	 * @param m1 The first Matrix.
	 * @param m2 The second Matrix.
	 * @return The resulting Matrix.
	 */
	private Matrix elementWiseMultiplication(Matrix m1, Matrix m2)
	{
		if (m1.getColumnDimension() != m2.getColumnDimension() || m1.getRowDimension() != m2.getRowDimension())
			return null;
		
		Matrix matrix = new Matrix(m1.getRowDimension(), m1.getColumnDimension());
		
		for (int i = 0; i < m1.getRowDimension(); i++)
		{
			for (int j = 0; j < m1.getColumnDimension(); j++)
				matrix.set(i, j, m1.get(i, j) * m2.get(i, j));
		}
		
		return matrix;
	}
	
	/**
	 * Converts a label of an Instance to a column vector where the index
	 * of the character is set to 0. So if the label is 'b' or '1', the
	 * 1st index of the Matrix will be set to 1.
	 * @param label The label.
	 * @return The column vector.
	 */
	private Matrix convertLabelToMatrix(ClassificationLabel label)
	{
		Matrix matrix = new Matrix(OUTPUT_SIZE, 1);
		matrix.set(label.getLabel(), 0, 1);
		
		return matrix;
	}
	
	/**
	 * Converts a 1-dimensional array to a column Matrix by creating a new
	 * 2-dimensional array where the column size is 1.
	 * @param array The array to convert.
	 * @return The Matrix.
	 */
	private Matrix convertArrayToMatrix(double[] array)
	{
		Matrix matrix = new Matrix(array.length, 1);
		
		for (int i = 0; i < array.length; i++)
			matrix.set(i, 0, array[i]);
		
		return matrix;
	}
	
	/**
	 * Performs the sigmoid function on the values in the matrix.
	 * @param matrix The matrix to convert.
	 */
	public void sigmoid(Matrix matrix)
	{
		for (int i = 0; i < matrix.getRowDimension(); i++)
		{
			for (int j = 0; j < matrix.getColumnDimension(); j++)
				matrix.set(i, j, this.sigmoid(matrix.get(i, j)));
		}
	}
	
	/**
	 * Computes the sigmoid gradient function and places the values in a new Matrix.
	 * @param matrix The Matrix.
	 * @return The new Matrix.
	 */
	public Matrix sigmoidGradient(Matrix matrix)
	{
		Matrix output = new Matrix(matrix.getRowDimension(), matrix.getColumnDimension());
		
		for (int i = 0; i < matrix.getRowDimension(); i++)
		{
			for (int j = 0; j < matrix.getColumnDimension(); j++)
				output.set(i, j, matrix.get(i, j) * (1- matrix.get(i, j)));
		}
		
		return output;
	}
	
	/**
	 * Converts an Instance's FeatureVector into a Matrix.
	 * @param instance The Instance to convert.
	 * @return The Matrix.
	 */
	private Matrix convertInstanceToMatrix(Instance instance)
	{
		Matrix matrix = new Matrix(INPUT_SIZE, 1);
		for (Pair<Integer, Double> pair : instance.getFeatureVector())
			matrix.set(pair.getKey(), 0, pair.getValue());
		
		return matrix;
	}
	
	/**
	 * Calculates the sigmoid function for the given argument.
	 * @param value The value to calculate for.
	 * @return The value of the sigmoid function.
	 */
	private double sigmoid(double value)
	{
		return 1 / (1 + Math.exp(-1 * value));
	}

	/**
	 * Selects the most probable label for the given Instance.
	 * @param instance The Instance to classify.
	 * @return The most probable label.
	 */
	public Label predict(Instance instance)
	{
		double[] probabilities = this.predictProbabilities(instance);
		
		double max = -1;
		int label = -1;
		for (int i = 0; i < probabilities.length; i++)
		{
			if (probabilities[i] > max)
			{
				max = probabilities[i];
				label = i;
			}
		}
		
		return new ClassificationLabel(label);
	}
	
	/**
	 * Gets the likelihood that an Instance is a specific letter.
	 * @param instance The Instance to classify.
	 * @return An array of likelihoods where the 0th index coressponds
	 * to the letter 'a'.
	 */
	public double[] predictProbabilities(Instance instance)
	{
		return this.forwardPropagation(instance);
	}

}
