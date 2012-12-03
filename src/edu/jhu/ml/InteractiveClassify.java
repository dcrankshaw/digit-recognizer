package edu.jhu.ml;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import edu.jhu.ml.data.Instance;
import edu.jhu.ml.data.RandomWordGenerator;

/**
 * Allows the user to input words to have the algorithms classify.
 * 
 * @author Daniel Deutsch
 */
public class InteractiveClassify
{
	private static JFrame frame = new JFrame();
	
	public static void main(String[] args) throws IOException
	{
		Scanner scanner = new Scanner(System.in);
		
		System.out.println("Enter a word to test:");
		while (scanner.hasNext())
		{
			String input = scanner.nextLine();
			
			List<Instance> word = RandomWordGenerator.generateWord(input);
			InteractiveClassify.displayWord(word);
		}
	}
	
	private static void displayWord(List<Instance> word) throws IOException
	{
		int[][] image = new int[16][word.size() * 8];
		
		for (int i = 0; i < word.size(); i++)
		{
			int[][] letter = InteractiveClassify.convertInstanceToArray(word.get(i));
			for (int row = 0; row < 16; row++)
			{
				for (int col = 0; col < 8; col++)
				{
					int value = letter[row][col];
					if (value == 0)
						image[row][col + i * 8] = 0;
					else
						image[row][col + i * 8] = 255;
				}
			}
		}
		
		BufferedImage theImage = new BufferedImage(word.size() * 8, 16, BufferedImage.TYPE_INT_RGB);
		for (int y = 0; y<16; y++)
		{
		    for (int x = 0; x<word.size() * 8; x++)
		    {
		        int value = image[y][x] << 16 | image[y][x] << 8 | image[y][x];
		        theImage.setRGB(x, y, value);
		    }
		}
		ImageIO.write(theImage, "jpg", new File("word.jpg"));
		
		frame = new JFrame();
		frame.setSize(new Dimension(200, 100));
		ImageIcon icon = new ImageIcon(theImage);

		JLabel label = new JLabel(icon);

		frame.add(label);
		frame.setVisible(true);
	}
	
	private static int[][] convertInstanceToArray(Instance instance)
	{
		int[][] image = new int[16][8];
		
		int counter = 0;
		for (int i = 0; i < 16; i++)
		{
			for (int j = 0; j < 8; j++)
				image[i][j] = (int) instance.getFeatureVector().get(counter++);
		}
		
		return image;
	}
}
