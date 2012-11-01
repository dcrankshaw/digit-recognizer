package edu.jhu.ml.data.label;

/**
 * A <code>ClassificationLabel</code> represents a discrete classification. Its
 * values can only take on a set of integers, like {0, 1}.
 * 
 * @author Daniel Deutsch
 */
public class ClassificationLabel extends Label
{
	private static final long serialVersionUID = 1L;
	
	/**
	 * The value of this <code>Label</code>.
	 */
	private int label;
	
	/**
	 * The constructor for the <code>ClassificationLabel</code>.
	 * @param label The value of this <code>Label</code>
	 */
	public ClassificationLabel(int label)
	{
		this.label = label;		
	}
	
	/**
	 * Retrieves the classification of this label.
	 * @return The classification.
	 */
	public int getLabel()
	{
		return this.label;
	}

	/**
	 * A <code>String</code> representation of the <code>ClassificationLabel</code>
	 */
	public String toString()
	{
		return "" + label;
	}
	
	public boolean equals(Object other)
	{
		if (other instanceof ClassificationLabel)
			return this.label == ((ClassificationLabel) other).getLabel();
		return false;
	}
}
