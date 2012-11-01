package edu.jhu.ml.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * A FeatureVector that is represented as a sparse vector. It is used
 * to hold the data for an instance.
 * 
 * @author Daniel Deutsch
 */
public class FeatureVector implements Iterable<Pair<Integer, Double>>, Serializable
{
	/**
	 * Long to be able to serialize the object.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The vector representation which maps each index to its value.
	 */
	private HashMap<Integer, Double> vector;
	
	/**
	 * A collection of the indices that is kept in sorted order so the
	 * <code>HashMap</code> can be iterated over.
	 */
	private TreeSet<Integer> indices; 
	
	/**
	 * The current size of the vector which is calculated as the largest index
	 * across the entire class of feature vectors.
	 */
	private static int size = 0;
	
	/**
	 * The constructor for the <code>FeatureVector</code>.
	 */
	public FeatureVector()
	{
		this.vector = new HashMap<Integer, Double>();
		this.indices = new TreeSet<Integer>();
	}
	
	/**
	 * Adds data to the vector.
	 * @param index The index to place this value.
	 * @param value The value to be stored.
	 */
	public void add(int index, double value)
	{
		this.vector.put(index, value);
		this.indices.add(index);
		
		size = Math.max(size, index); 
	}

	/**
	 * Gets the value associated with the given index.
	 * @param index The index to check.
	 * @return The value.
	 */
	public double get(int index)
	{
		if (!this.vector.containsKey(index))
			return 0;
		return this.vector.get(index);
	}
	
	/**
	 * Retrieves the size of the vector, or the highest index seen across
	 * the entire class of feature vectors.
	 * @return the size
	 */
	public static int size()
	{
		return size;
	}
	
	/**
	 * Gets an <code>Iterator</code> for the <code>FeatureVector</code>.
	 * @return The <code>Iterator</code>.
	 */
	public Iterator<Pair<Integer, Double>> iterator()
	{
		return new FeatureVectorIterator(this.vector, this.indices);
	}
	
	/**
	 * An <code>Iterator</code> to iterate over the <code>FeatureVector</code>.
	 * The <code>remove</code> operation is not implemented.
	 * 
	 * @author Daniel Deutsch
	 */
	private class FeatureVectorIterator implements Iterator<Pair<Integer, Double>>
	{
		private HashMap<Integer, Double> vector;
		private TreeSet<Integer> indices;
		private Iterator<Integer> iterator; 
				
		public FeatureVectorIterator(HashMap<Integer, Double> vector, TreeSet<Integer> set)
		{
			this.vector = vector;
			this.indices = set;
			this.iterator = this.indices.iterator();
		}
		
		public boolean hasNext()
		{
			return this.iterator.hasNext();
		}

		public Pair<Integer, Double> next()
		{
			int key = this.iterator.next();
			double value = this.vector.get(key);
			
			return new Pair<Integer, Double>(key, value);
		}

		public void remove()
		{
			throw new UnsupportedOperationException("FeatureVectorIterator does not support the remove operation");
		}
	}
}
