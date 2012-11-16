package edu.jhu.ml.data;

public class Pair<K extends Comparable<K>, V> implements Comparable<Pair<K, V>>
{
	private K key;
	private V value;
	
	public Pair(K key, V value)
	{
		this.key = key;
		this.value = value;
	}
	
	public K getKey()
	{
		return this.key;
	}
	
	public V getValue()
	{
		return this.value;
	}

	public int compareTo(Pair<K, V> other)
	{
		return this.key.compareTo(other.getKey());
	}
}
