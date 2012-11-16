package edu.jhu.ml.data.label;

import java.io.Serializable;

/**
 * A <code>Label</code> that can be applied to an instance of data
 * to represent a category or value that it belongs to.
 * 
 * @author Daniel Deutsch
 */
public abstract class Label implements Serializable
{
	private static final long serialVersionUID = 1L;

	public abstract String toString();
	
	/**
	 * Checks to see if two <code>Labels</code> are equal based on
	 * their <code>String</code> representations
	 * 
	 * @return true if they are equal, false otherwise.
	 */
	public boolean equals(Object other)
	{
		return this.toString().equals(other.toString());
	}
	
	/**
	 * Generates a hash code for this <code>Label</code>.
	 * @return The hash code.
	 */
	public int hashCode()
	{
		return this.toString().hashCode();
	}
}
