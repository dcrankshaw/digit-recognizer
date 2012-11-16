package edu.jhu.ml.data;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * This class will be used to take the raw input file downloaded directly from
 * the website and clean it up. It will remove the extra features of each
 * instance that we aren't using and it will place each letter into its appropriate
 * folder, one for each letter.
 * 
 * @author Daniel Deutsch
 */
public class DataCleaner
{
	/**
	 * Cleans up the raw data and turns it into a set of 
	 * @param fileName
	 */
	public static void cleanUp(String fileName) throws IOException
	{				
		List<List<String>> characterList = DataCleaner.createCharacterList(fileName);
		DataCleaner.divideIntoThirds(characterList);
		DataCleaner.divideByLetter(characterList);
	}
	
	/**
	 * Creates a list of size 26 where each item is a list of strings which
	 * represent the data.
	 * @return The lists.
	 */
	private static List<List<String>> createCharacterList(String fileName) throws FileNotFoundException
	{
		Scanner scanner = new Scanner(new FileReader(fileName));
		
		List<List<String>> characterList = new ArrayList<List<String>>();
		for (int i = 0; i < 26; i++)
			characterList.add(new ArrayList<String>());
		
		while (scanner.hasNextLine())
		{
			String line = scanner.nextLine();
			line = line.replace("\t", " ");

			Scanner charScanner = new Scanner(line);
			
			// Read the initial input. We only care about the letter.
			charScanner.nextInt(); // the id
			char letter = charScanner.next().charAt(0);
			charScanner.nextInt(); // the next id
			charScanner.nextInt(); // the word id
			charScanner.nextInt(); // the position
			charScanner.nextInt(); // the fold
			
			String newInput = "" + letter;
			while (charScanner.hasNext())
				newInput += " " + charScanner.next();
			
			characterList.get(letter - 'a').add(newInput);
		}
		
		return characterList;
	}
	
	/**
	 * Divides the data into thirds and writes each third to its own txt file. The splitting
	 * is done by using 1/3 of the total number of each character.
	 * @param characterList
	 * @throws IOException 
	 */
	private static void divideIntoThirds(List<List<String>> characterList) throws IOException
	{
		FileWriter nnTrain = new FileWriter("data/all/nnTrain.txt");
		FileWriter hmmTrain = new FileWriter("data/all/hmmTrain.txt");
		FileWriter test = new FileWriter("data/all/test.txt");
				
		for (List<String> list : characterList)
		{
			int counter = 0;
			
			for (int i = 0; i < list.size() / 3; i++)
				nnTrain.write(list.get(counter++) + "\n");
			for (int i = 0; i < list.size() / 3; i++)
				hmmTrain.write(list.get(counter++) + "\n");
			for ( ; counter < list.size(); counter++)
				test.write(list.get(counter) + "\n");
		}
		
		nnTrain.close();
		hmmTrain.close();
		test.close();
	}
	
	/**
	 * Divides the data up into a text file per character.
	 * @param characterList The lists of input.
	 */
	private static void divideByLetter(List<List<String>> characterList) throws IOException
	{
		for (int i = 0; i < 26; i++)
		{
			char letter = (char) (i + 'a');
			FileWriter writer = new FileWriter("data/letters/" + letter + ".txt");
			
			List<String> list = characterList.get(i);
			for (String string : list)
				writer.write(string + "\n");
			
			writer.close();
		}
	}
	
	
	/**
	 * Cleans up the data.
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException
	{
		DataCleaner.cleanUp("data/raw/data.txt");
	}
}
