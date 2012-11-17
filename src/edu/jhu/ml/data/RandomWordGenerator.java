package edu.jhu.ml.data;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import edu.jhu.ml.data.label.ClassificationLabel;

public class RandomWordGenerator
{
	private static int[] letterCounts;
	
	private static final String filePath = "data/letters/";
	
	public static List<Instance> generateWord(String word) throws FileNotFoundException
	{
		if (RandomWordGenerator.letterCounts == null)
			RandomWordGenerator.countNumberOfLetters();
		
		List<Instance> instances = new ArrayList<Instance>();
		Random random = new Random();
		
		char[] letters = word.toCharArray();
		for (int i = 0; i < letters.length; i++)
		{
			int index = random.nextInt(RandomWordGenerator.letterCounts[letters[i] - 'a']);
			String line = RandomWordGenerator.readInstance(letters[i], index);
			
			instances.add(RandomWordGenerator.createInstance(line));
		}
		
		return instances;
	}
	
	private static String readInstance(char letter, int index) throws FileNotFoundException
	{
		Scanner scanner = new Scanner(new FileReader(RandomWordGenerator.filePath + letter + ".txt"));
		
		int count = 0;
		while (scanner.hasNextLine())
		{
			if (count == index)
				return scanner.nextLine();
			else
				scanner.nextLine();
			count++;
		}
		
		return null;
	}
	
	private static Instance createInstance(String line)
	{
		Scanner lineScanner = new Scanner(line);
		
		char label = lineScanner.next().charAt(0);
		FeatureVector featureVector = new FeatureVector();
		
		int index = 0;
		while (lineScanner.hasNext())
		{
			int feature = lineScanner.nextInt();
			if (feature != 0)
				featureVector.add(index, feature);
			index++;
		}
		
		return new Instance(featureVector, new ClassificationLabel(label - 'a'));
	}
	
	private static void countNumberOfLetters() throws FileNotFoundException
	{
		RandomWordGenerator.letterCounts = new int[26];
		
		for (int i = 'a'; i <= 'z'; i++)
		{
			Scanner scanner = new Scanner(new FileReader(RandomWordGenerator.filePath + ((char) i) + ".txt"));
			while (scanner.hasNextLine())
			{
				RandomWordGenerator.letterCounts[i - 'a']++;
				scanner.nextLine();
			}
		}
	}
	
	public static void main(String[] args) throws FileNotFoundException
	{
		List<Instance> instances = RandomWordGenerator.generateWord("hello");
	}
}
