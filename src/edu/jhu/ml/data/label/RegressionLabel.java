package edu.jhu.ml.data.label;

import edu.jhu.ml.data.label.Label;

/**
 * A <code>RegressionLabel</code> represents a label that can be applied to an instance
 * that could take on a continuous value, like in a regression classifier.
 * 
 * @author Daniel Deutsch
 */
public class RegressionLabel extends Label
{
	private static final long serialVersionUID = 1L;
	
	/**
	 * The value of the label.
	 */
	private double label;
	
	/**
	 * The constructor for the <code>Label</code>.
	 * @param label the value of the label.
	 */
	public RegressionLabel(double label)
	{
		this.label = label;
	}

	/**
	 * A <code>String</code> representation of the label.
	 * 
	 * @return The string representation.
	 */
	public String toString()
	{
		return "" + this.label;
	}
	
	public double getLabel()
	{
		return this.label;
	}
}
