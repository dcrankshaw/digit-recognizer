package edu.jhu.ml.data;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

import edu.jhu.ml.data.label.ClassificationLabel;

/**
 * Takes a word and randomly selects handwritten letters to make up that word.
 * 
 * @author Daniel Deutsch
 */
public class RandomWordGenerator
{
	/**
	 * The number of times a specific letter appears.
	 */
	private static int[] letterCounts;
	
	/**
	 * The file path to the folder with the letter text files.
	 */
	private static final String filePath = "data/letters/";
	
	/**
	 * Counts of how many times each word appears.
	 */
	private static HashMap<String, Double> wordProbabilities;
	
	/**
	 * Randomly selects a word from the corpus, then randomly selects instances
	 * to make up the handwritten word.
	 * @return A list of Instance which make up the word.
	 */
	public static List<Instance> randomWord() throws FileNotFoundException
	{
		return RandomWordGenerator.generateWord(RandomWordGenerator.pickRandomWord());
	}
	
	/**
	 * Randomly selects a word from the training corpus.
	 * @return The random word.
	 * @throws FileNotFoundException If the file is not found.
	 */
	private static String pickRandomWord() throws FileNotFoundException
	{
		if (RandomWordGenerator.wordProbabilities == null)
			RandomWordGenerator.countWords();
		
		Random random = new Random();
		double probability = random.nextDouble();
		double sum = 0;
		
		Set<String> words = RandomWordGenerator.wordProbabilities.keySet();
		
		for (String word : words)
		{
			sum += RandomWordGenerator.wordProbabilities.get(word);
			if (probability <= sum)
				return word;
		}
		
		return null;
	}
	
	/**
	 * Generates the random letters from the word.
	 * @param word The word.
	 * @return A list of Instances that represent the letters which make up that word.
	 */
	public static List<Instance> generateWord(String word) throws FileNotFoundException
	{
		if (RandomWordGenerator.letterCounts == null)
			RandomWordGenerator.countNumberOfLetters();
		
		List<Instance> instances = new ArrayList<Instance>();
		Random random = new Random();
		
		char[] letters = word.toCharArray();
		for (int i = 0; i < letters.length; i++)
		{
			if (letters[i] != ' ')
			{
				int index = random.nextInt(RandomWordGenerator.letterCounts[letters[i] - 'a']);
				String line = RandomWordGenerator.readInstance(letters[i], index);
				
				instances.add(RandomWordGenerator.createInstance(line));
			}
			else
			{
				instances.add(new Instance(new FeatureVector(), new ClassificationLabel(-1)));
			}
		}
		
		return instances;
	}
	
	/**
	 * Counts the number of times each word appears in the corpus.
	 * @throws FileNotFoundException 
	 */
	private static void countWords() throws FileNotFoundException
	{
		HashMap<String, Integer> wordCounts = new HashMap<String, Integer>();
		Scanner scanner = new Scanner(new FileReader("data/corpus/clean_moby_dick.txt"));
		
		int totalWords = 0;
		
		while (scanner.hasNext())
		{
			String word = scanner.next();
			totalWords++;
			
			if (!wordCounts.containsKey(word))
				wordCounts.put(word, 1);
			else
				wordCounts.put(word, wordCounts.get(word) + 1);
		}
		
		RandomWordGenerator.wordProbabilities = new HashMap<String, Double>();
		Set<String> words = wordCounts.keySet();
		
		for (String word : words)
			RandomWordGenerator.wordProbabilities.put(word, (double) wordCounts.get(word) / totalWords);
	}
	
	/**
	 * Reads in a specific Instance of a letter.
	 * @param letter The letter to read in.
	 * @param index The specific Index.
	 * @return The String at that line of the file.
	 */
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
	
	/**
	 * Creates an Instance from a String.
	 * @param line The String.
	 * @return The Instance.
	 */
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
	
	/**
	 * Counts how many times each letter appears and stores the counts in
	 * a static array.
	 */
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
		for (int i = 0; i < 100; i++)
		{
			System.out.println(RandomWordGenerator.randomWord());
		}
	}
}
