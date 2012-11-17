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
 * Definitions:
 * - X = input
 * - Y = real labels
 * - z_i = the values of the nodes in the ith layer
 * - a_i = sigmoid(z_i); the activation function applied to the layer
 * - theta_i = transition weights from layer i to layer i + 1
 * - d_i = the error assigned to the nodes in layer i
 * - Delta_i = the gradient calculated from layer i to layer i + 1
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
	 * The number of nodes in the input layer of the neural network. Add
	 * 1 to account for the bias unit.
	 */
	private static final int INPUT_SIZE = 128; // 128

	/**
	 * The number of hidden nodes in the only hidden layer
	 * of the neural network. Add 1 to account for the bias unit.
	 */
	private static final int HIDDEN_SIZE = 25;

	/**
	 * The number of output nodes of the neural network.
	 */
	private static final int OUTPUT_SIZE = 26; // 26

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
		Random random = new Random(4);

		this.firstWeights = new Matrix(HIDDEN_SIZE, INPUT_SIZE);
		this.secondWeights = new Matrix(OUTPUT_SIZE, HIDDEN_SIZE);

		double epsilon = 0.025;
		
		// perform random initialization between -epsilon and epsilon
		for (int i = 0; i < HIDDEN_SIZE; i++)
		{	
			for (int j = 0; j < INPUT_SIZE; j++)
				this.firstWeights.set(i, j, random.nextDouble() * 2 * epsilon - epsilon);
		}

		for (int i = 0; i < OUTPUT_SIZE; i++)
		{
			for (int j = 0; j < HIDDEN_SIZE; j++)
				this.secondWeights.set(i, j, random.nextDouble() * 2 * epsilon - epsilon);
		}
	}

	/**
	 * Trains the neural network on the given Instances.
	 * @param instances The Instances to train on.
	 */
	public void train(List<Instance> instances)
	{
		int iterations = 33;
		double learningRate = 0.5;
		
		double lastCost = this.calculateCost(instances);
		double thisCost = 0;

		for (int i = 0; i < iterations; i++)
		{
			if (i == 13)
				System.out.println("13");
			System.out.println("Starting iteration " + i);
			thisCost = this.calculateCost(instances);
			System.out.println("This cost: " + thisCost);
			System.out.println("Cost difference: " + (thisCost - lastCost));
			lastCost = thisCost;
			
			this.backPropagation(instances);
						
			this.firstWeights = this.firstWeights.minus(this.firstGradient.times(learningRate));
			this.secondWeights = this.secondWeights.minus(this.secondGradient.times(learningRate));
		}
	}

	/**
	 * Calculates the cost of the neural network.
	 * cost of the neural network.
	 * @param instances The Instances.
	 * @return The cost.
	 */
	private double calculateCost(List<Instance> instances)
	{
		double cost = 0;
				
		for (Instance instance : instances)
		{
			// a1 = x_i
			Matrix a1 = this.convertInstanceToMatrix(instance);
			
			// y = label
			Matrix y = this.convertLabelToMatrix((ClassificationLabel) instance.getLabel());
			
			// z2 = Theta1 * a1
			Matrix z2 = this.firstWeights.times(a1);
			
			// a2 = g(z2)
			Matrix a2 = this.sigmoid(z2);
			
			// z3 = Theta2 * a2
			Matrix z3 = this.secondWeights.times(a2);
			
			// a3 = g(z3)
			Matrix a3 = this.sigmoid(z3);
			
			for (int k = 0; k < OUTPUT_SIZE; k++)
			{
				if (y.get(k, 0) == 1)
					cost += Math.log(a3.get(k, 0));
				else
					cost += Math.log(1 - a3.get(k, 0));
			}
		}
		
		return -1 * cost;
	}

	/**
	 * Performs the forward propagation algorithm on the neural network.
	 * @param instance The instance to be used as input.
	 * @return The output array.
	 */
	private double[] forwardPropagation(Instance instance)
	{
		// X
		Matrix input = this.convertInstanceToMatrix(instance);
		
		// z2 = Theta1 * X
		this.hiddenNodes = this.firstWeights.times(input);

		// z3 = Theta2 * g(z2)
		Matrix output = this.secondWeights.times(this.sigmoid(this.hiddenNodes));
		
		// a3 = g(z3)
		Matrix sigmoidOutput = this.sigmoid(output);

		double[] array = new double[OUTPUT_SIZE];
		for (int i = 0; i < OUTPUT_SIZE; i++)
			array[i] = sigmoidOutput.get(i, 0);

		return array;
	}

	/**
	 * Performs the backpropagation algorithm. 
	 * @param instances The training Instances.
	 */
	private void backPropagation(List<Instance> instances)
	{
		// Delta1 = 0
		Matrix Delta1 = new Matrix(HIDDEN_SIZE, INPUT_SIZE); 
		
		// Delta2 = 0
		Matrix Delta2 = new Matrix(OUTPUT_SIZE, HIDDEN_SIZE);
		
		// for i = 1 to m
		for (int i = 0; i < instances.size(); i++)
		{
			// a1 = x_i
			Matrix a1 = this.convertInstanceToMatrix(instances.get(i));
			
			// y = label
			Matrix y = this.convertLabelToMatrix((ClassificationLabel) instances.get(i).getLabel());
			
			// z2 = Theta1 * a1
			Matrix z2 = this.firstWeights.times(a1);
			
			// a2 = g(z2)
			Matrix a2 = this.sigmoid(z2);
			
			// z3 = Theta2 * a2
			Matrix z3 = this.secondWeights.times(a2);
			
			// a3 = g(z3)
			Matrix a3 = this.sigmoid(z3);
			
			// delta3 = a3 - y_i
			Matrix delta3 = a3.minus(y);
			
			// delta2 = Theta2^T * delta3 .* g(z2)
			Matrix delta2 = this.secondWeights.transpose().times(delta3);
			delta2 = this.elementWiseMultiplication(delta2, this.sigmoidGradient(z2));
		
			// Delta1 = Delta1 + delta2 * a1^T
			Delta1 = Delta1.plus(delta2.times(a1.transpose()));
			
			// Delta2 = Delta2 + delta3 * a2^T
			Delta2 = Delta2.plus(delta3.times(a2.transpose()));
		}
		
		this.firstGradient = new Matrix(HIDDEN_SIZE, INPUT_SIZE);
		this.secondGradient = new Matrix(OUTPUT_SIZE, HIDDEN_SIZE);
				
		// for some reason, the Matrix.times(double) method is not working!
		// I think 1 / instances.size() is too small
		// D1 = 1 / n * Delta1
		for (int i = 0; i < HIDDEN_SIZE; i++)
		{
			for (int j = 0; j < INPUT_SIZE; j++)
				this.firstGradient.set(i, j, Delta1.get(i, j) / instances.size());
		}
		
		// D2 = 1 / n * Delta2
		for (int i = 0; i < OUTPUT_SIZE; i++)
		{
			for (int j = 0; j < HIDDEN_SIZE; j++)
				this.secondGradient.set(i, j, Delta2.get(i, j) / instances.size());
		}
		
		System.out.println("done");
		
		
		
		
		
		
		
		
		
//		this.firstGradient = Delta1;
//		this.secondGradient = Delta2;
//		// Delta1
//		this.firstGradient = new Matrix(new double[HIDDEN_SIZE][INPUT_SIZE]);
//
//		// Delta2
//		this.secondGradient = new Matrix(new double[OUTPUT_SIZE][HIDDEN_SIZE]);
//
//		for (Instance instance : instances)
//		{
//			// a1 = x
//			Matrix input = this.convertInstanceToMatrix(instance);
//
//			// a2
//			// this.hiddenNodes
//			Matrix a2 = this.sigmoidGradient(this.hiddenNodes);
//
//			// a3 = output
//			Matrix prediction = this.convertArrayToMatrix(this.forwardPropagation(instance));
//
//			// y = real labels
//			Matrix actualLabel = this.convertLabelToMatrix((ClassificationLabel) instance.getLabel());
//
//			// d3 = output - real labels
//			Matrix thirdError = prediction.minus(actualLabel);
//
//			// d2 = theta_2^T * d3 .* g'(z2)
//			Matrix secondError = this.elementWiseMultiplication(this.secondWeights.transpose().times(thirdError), this.sigmoidGradient(this.hiddenNodes));
//
//			// Delta1 = Delta1 + d2*a1^T
//			this.firstGradient = this.firstGradient.plus(secondError.times(input.transpose()));
//
//			// Delta2 = Delta2 + d3*a2^T
////			this.secondGradient = this.secondGradient.plus(thirdError.times(this.hiddenNodes.transpose()));
//			this.secondGradient = this.secondGradient.plus(thirdError.times(a2.transpose()));
//		}
//		
////		this.firstGradient = this.firstGradient.times(1 / instances.size());
////		this.secondGradient = this.secondGradient.times(1 / instances.size());
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
	 * Performs the sigmoid function on the values in the Matrix.
	 * @param matrix The matrix to convert.
	 * @return The new Matrix.
	 */
	public Matrix sigmoid(Matrix matrix)
	{
		Matrix newMatrix = new Matrix(matrix.getRowDimension(), matrix.getColumnDimension());
		
		for (int i = 0; i < matrix.getRowDimension(); i++)
		{
			for (int j = 0; j < matrix.getColumnDimension(); j++)
				newMatrix.set(i, j, this.sigmoid(matrix.get(i, j)));
		}
		
		return newMatrix;
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
		
		// the +1 is to make room for the bias unit
		for (Pair<Integer, Double> pair : instance.getFeatureVector())
			matrix.set(pair.getKey(), 0, pair.getValue());
//		matrix.set(0, 0, 1);
		
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
	 * @return An array of likelihoods where the 0th index corresponds
	 * to the letter 'a'.
	 */
	public double[] predictProbabilities(Instance instance)
	{
		return this.forwardPropagation(instance);
	}

}