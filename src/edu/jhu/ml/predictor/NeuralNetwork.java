package edu.jhu.ml.predictor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import Jama.Matrix;
import edu.jhu.ml.data.Instance;
import edu.jhu.ml.data.Pair;
import edu.jhu.ml.data.label.ClassificationLabel;
import edu.jhu.ml.evaluator.AccuracyEvaluator;
import edu.jhu.ml.utilities.DataReader;

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
	private static final int INPUT_SIZE = 128 + 1; // 128

	/**
	 * The number of hidden nodes in the only hidden layer
	 * of the neural network. Add 1 to account for the bias unit.
	 */
	private static int HIDDEN_SIZE_1 = 100 + 1; // 50

	private static int HIDDEN_SIZE_2 = 50 + 1;
	
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

	private Matrix thirdWeights;

	/**
	 * Corresponds to Delta1. Position (i,j) contains the gradient of parameter (i,j).
	 */
	private Matrix firstGradient;

	/**
	 * Corresponds to Delta2. Position (i,j) contains the gradient of parameter (i,j).
	 */
	private Matrix secondGradient;
	
	private Matrix thirdGradient;

	public NeuralNetwork(int size1, int size2)
	{
		NeuralNetwork.HIDDEN_SIZE_1 = size1;
		NeuralNetwork.HIDDEN_SIZE_2 = size2;
		
		
		Random random = new Random();

		this.firstWeights = new Matrix(HIDDEN_SIZE_1, INPUT_SIZE);
		this.secondWeights = new Matrix(HIDDEN_SIZE_2, HIDDEN_SIZE_1);
		this.thirdWeights = new Matrix(OUTPUT_SIZE, HIDDEN_SIZE_2);

		double epsilon = 0.25; // 0.05
		
		// perform random initialization between -epsilon and epsilon
		for (int i = 0; i < HIDDEN_SIZE_1; i++)
		{	
			for (int j = 0; j < INPUT_SIZE; j++)
				this.firstWeights.set(i, j, random.nextDouble() * 2 * epsilon - epsilon);
		}
		
		for (int i = 0; i < HIDDEN_SIZE_2; i++)
		{
			for (int j = 0; j < HIDDEN_SIZE_1; j++)
				this.secondWeights.set(i, j, random.nextDouble() * 2 * epsilon - epsilon);
		}

		for (int i = 0; i < OUTPUT_SIZE; i++)
		{
			for (int j = 0; j < HIDDEN_SIZE_2; j++)
				this.thirdWeights.set(i, j, random.nextDouble() * 2 * epsilon - epsilon);
		}
	}

	/**
	 * Trains the neural network on the given Instances.
	 * @param instances The Instances to train on.
	 */
	public void train(List<Instance> instances)
	{
		double learningRate = 1.001; // 1.001
		
		double lastCost = Double.MAX_VALUE;
		double thisCost = this.calculateCost(instances);
		double difference = thisCost - lastCost;
		
		int counter = 0;
		do
		{
			this.backPropagation(instances);
		
			thisCost = this.calculateCost(instances);
			difference = thisCost - lastCost;
			lastCost = thisCost;
			
			System.out.println("iteration: " + counter++);
			System.out.println("cost: " + lastCost);
			System.out.println("difference: " + difference);
			System.out.println("learing rate: " + learningRate);
			System.out.println();
			
			
			this.firstWeights = this.firstWeights.plus(this.firstGradient.times(learningRate));
			this.secondWeights = this.secondWeights.plus(this.secondGradient.times(learningRate));
			this.thirdWeights = this.thirdWeights.plus(this.thirdGradient.times(learningRate));
			
			learningRate = learningRate * 1.01; // 1.01
			
		} while (difference <= -10);
//		} while (true);
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
			a1 = this.addBiasUnit(a1);
			
			// y = label
			Matrix y = this.convertLabelToMatrix((ClassificationLabel) instance.getLabel());
			
			// z2 = Theta1 * a1
			Matrix z2 = this.firstWeights.times(a1);
			
			// a2 = g(z2)
			Matrix a2 = this.sigmoid(z2);
			a2 = this.addBiasUnit(a2);
			
			// z3 = Theta2 * a2
			Matrix z3 = this.secondWeights.times(a2);
			
			// a3 = g(z3)
			Matrix a3 = this.sigmoid(z3);
			a3 = this.addBiasUnit(a3);
			
			// z4 = Theta3 * a3
			Matrix z4 = this.thirdWeights.times(a3);
			
			// a4 = g(z4)
			Matrix a4 = this.sigmoid(z4);
			
			for (int k = 0; k < OUTPUT_SIZE; k++)
			{
				if (y.get(k, 0) == 1)
					cost += Math.log(a4.get(k, 0));
				else
					cost += Math.log(1 - a4.get(k, 0));
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
		// a1 = x_i
		Matrix a1 = this.convertInstanceToMatrix(instance);
		a1 = this.addBiasUnit(a1);
		
		// z2 = Theta1 * a1
		Matrix z2 = this.firstWeights.times(a1);
		
		// a2 = g(z2)
		Matrix a2 = this.sigmoid(z2);
		a2 = this.addBiasUnit(a2);
		
		// z3 = Theta2 * a2
		Matrix z3 = this.secondWeights.times(a2);
		
		// a3 = g(z3)
		Matrix a3 = this.sigmoid(z3);
		a3 = this.addBiasUnit(a3);
		
		// z4 = Theta3 * a3
		Matrix z4 = this.thirdWeights.times(a3);
		
		// a4 = g(z4)
		Matrix a4 = this.sigmoid(z4);

		double[] array = new double[a4.getRowDimension()];
		for (int i = 0; i < array.length; i++)
			array[i] = a4.get(i, 0);
		
		return array;
	}

	/**
	 * Performs the backpropagation algorithm. 
	 * @param instances The training Instances.
	 */
	private void backPropagation(List<Instance> instances)
	{
		// Delta1 = 0
		Matrix Delta1 = new Matrix(HIDDEN_SIZE_1, INPUT_SIZE); 
		
		// Delta2 = 0
		Matrix Delta2 = new Matrix(HIDDEN_SIZE_2, HIDDEN_SIZE_1);
		
		// Delta3 = 0
		Matrix Delta3 = new Matrix(OUTPUT_SIZE, HIDDEN_SIZE_2);
		
		// for i = 1 to m
		for (int i = 0; i < instances.size(); i++)
		{
			// a1 = x_i
			Matrix a1 = this.convertInstanceToMatrix(instances.get(i));
			a1 = this.addBiasUnit(a1);
			
			// y = label
			Matrix y = this.convertLabelToMatrix((ClassificationLabel) instances.get(i).getLabel());
			
			// z2 = Theta1 * a1
			Matrix z2 = this.firstWeights.times(a1);
			
			// a2 = g(z2)
			Matrix a2 = this.sigmoid(z2);
			a2 = this.addBiasUnit(a2);
			
			// z3 = Theta2 * a2
			Matrix z3 = this.secondWeights.times(a2);
			
			// a3 = g(z3)
			Matrix a3 = this.sigmoid(z3);
			a3 = this.addBiasUnit(a3);
			
			// z4 = Theta3 * a3
			Matrix z4 = this.thirdWeights.times(a3);
			
			// a4 = g(z4)
			Matrix a4 = this.sigmoid(z4);
			
			// delta4 = y_i - a4
			Matrix delta4 = y.minus(a4);
			
			// delta3 = Theta3^T * delta4 .* g'(z3)
			Matrix delta3 = this.thirdWeights.transpose().times(delta4);
			delta3 = this.elementWiseMultiplication(delta3, this.sigmoidGradient(z3));
			
			// delta2 = Theta2^T * delta3 .* g'(z2)
			Matrix delta2 = this.secondWeights.transpose().times(delta3);
			delta2 = this.elementWiseMultiplication(delta2, this.sigmoidGradient(z2));
		
			// Delta1 = Delta1 + delta2 * a1^T
			Delta1 = Delta1.plus(delta2.times(a1.transpose()));
			
			// Delta2 = Delta2 + delta3 * a2^T
			Delta2 = Delta2.plus(delta3.times(a2.transpose()));
			
			// Delta3 = Delta3 + delta4 * a3^T
			Delta3 = Delta3.plus(delta4.times(a3.transpose()));
		}
		
		this.firstGradient = new Matrix(HIDDEN_SIZE_1, INPUT_SIZE);
		this.secondGradient = new Matrix(HIDDEN_SIZE_2, HIDDEN_SIZE_1);
		this.thirdGradient = new Matrix(OUTPUT_SIZE, HIDDEN_SIZE_2);
				
		// for some reason, the Matrix.times(double) method is not working!
		// I think 1 / instances.size() is too small
		// D1 = 1 / n * Delta1
		for (int i = 0; i < HIDDEN_SIZE_1; i++)
		{
			for (int j = 0; j < INPUT_SIZE; j++)
				this.firstGradient.set(i, j, Delta1.get(i, j) / instances.size());
		}
		
		// D2 = 1 / n * Delta2
		for (int i = 0; i < HIDDEN_SIZE_2; i++)
		{
			for (int j = 0; j < HIDDEN_SIZE_1; j++)
				this.secondGradient.set(i, j, Delta2.get(i, j) / instances.size());
		}
		
		// D3 = 1 / n * Delta3
		for (int i = 0; i < OUTPUT_SIZE; i++)
		{
			for (int j = 0; j < HIDDEN_SIZE_2; j++)
				this.thirdGradient.set(i, j, Delta3.get(i, j) / instances.size());
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
	 * Adds the bias +1 unit at the 0th index of the matrix. Should only be used for
	 * column vectors.
	 * @param matrix The Matrix.
	 * @return The new Matrix.
	 */
	private Matrix addBiasUnit(Matrix matrix)
	{
		Matrix output = new Matrix(matrix.getRowDimension(), 1);
		
		for (int i = 0; i < matrix.getRowDimension() - 1; i++)
			output.set(i + 1, 0, matrix.get(i, 0));
		output.set(0, 0, 1);
		
		return output;
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
			{
				double sigmoid = this.sigmoid(matrix.get(i, j));
				output.set(i, j, sigmoid * (1 - sigmoid));
			}
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
	public ClassificationLabel predict(Instance instance)
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
	
	public int[] getTopProbabilities(Instance instance, int number)
	{
		double[] prediction = this.forwardPropagation(instance);
		ArrayList<Pair<Double, Integer>> list = new ArrayList<Pair<Double, Integer>>();
		
		for (int i = 0; i < prediction.length; i++)
			list.add(new Pair<Double,Integer>(prediction[i], i));
		Collections.sort(list);
		
		int[] result = new int[number];
		for (int i = 0; i < number; i++)
			result[i] = list.get(list.size() - 1 - i).getValue();
		
		return result;
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
	
	
	public static void main(String[] args)
	{
		DataReader reader = new DataReader("data/all/nnTrain.txt");
		List<Instance> instances = reader.read();
		
		System.out.println("100, 50");
		for (int i = 0; i < 10; i++)
		{
			NeuralNetwork network = new NeuralNetwork(100, 50);
			AccuracyEvaluator evaluator = new AccuracyEvaluator(network);
			
			network.train(instances);
			System.out.println(evaluator.evaluateLetterAccuracy(instances, 1));
		}
		
		
	}

}