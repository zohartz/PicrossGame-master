package me.groix.android.picross;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * מחלקה הקוראת את קבצי התמונה לחידה (שמקודדים ב1.0 כל המתודות כאן סטטיות
 * A class to read .pic files, representing a puzzle.
   * <br> Contains only static methods
   *
 *
 */
public class PicrossReader {

	/**
	 * A class reading a .pic file and creating the Associated Plateau.
	 * @param file The URL of a .pic file
	 * @return A Plateau, corresponding to the file
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	//returns new plateau
	public static Plateau read(InputStream file,String id) throws FileNotFoundException, IOException {
		String title; 
		String author; 

		int nbRow;
		int nbCol;
		boolean[][] pattern;
		
		//bridge from byte streams to character streams: It reads bytes and decodes them into characters using a specified charset
		InputStreamReader picrossFile = new InputStreamReader(file);
		//class reads text from a character-input stream, buffering characters so as to provide for the efficient reading of characters, arrays
		BufferedReader picrossStream = new BufferedReader(picrossFile);
		//reads from file

		String titleRow  = picrossStream.readLine();
		String authorRow = picrossStream.readLine();
		String rowsNum  = picrossStream.readLine();
		String colsNum  = picrossStream.readLine();

		if (!titleRow.substring(0, 6).equals("title=")) {
			System.err.println("Error reading title in "+file);
		}
		title = titleRow.substring(6);
		if (!authorRow.substring(0, 7).equals("author=")) {
			System.err.println("Error reading the author in "+file);
		}
		author = authorRow.substring(7);

		nbRow = Integer.parseInt(rowsNum.substring(9));
		nbCol= Integer.parseInt(colsNum.substring(11));

		pattern = new boolean[nbRow][nbCol];
		for (int lin=0;lin<nbRow;lin++) {
			String line = picrossStream.readLine();
			for (int col=0;col<nbCol;col++) {
				if (line.charAt(col)=='0') {
					pattern[lin][col] = false;
				} else {
					pattern[lin][col] = true;
				}
			}

		}
		return new Plateau(nbRow,nbCol,pattern,title,author,id);
	}
	
	/**
	 * A small func to get the title of the puzzle:
	 * Title, size
	 * @param file the Url of a .pic file
	 * @return info
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static String readTitle(InputStream file) throws FileNotFoundException, IOException {
		String title;
		int nbLine;
		int nbCol;

		InputStreamReader picrossFile = new InputStreamReader(file);
		BufferedReader picrossStream = new BufferedReader(picrossFile);

		String titleRow = picrossStream.readLine();
		String authorRow = picrossStream.readLine();
		String rowsNum  = picrossStream.readLine();
		String colsNum  = picrossStream.readLine();

		if (!titleRow.substring(0, 6).equals("title=")) {
			System.err.println("error a la lecture du title dans "+file);
		}
		title = titleRow.substring(6);
		nbLine = Integer.parseInt(rowsNum.substring(9));
		nbCol= Integer.parseInt(colsNum.substring(11));

		return title;
	}
	
	/**
	 * Returns the author of the requested puzzle
	 * @param file
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static String readAuthor(InputStream file) throws FileNotFoundException, IOException {
		String author;

		InputStreamReader picrossFile = new InputStreamReader(file);
		BufferedReader picrossStream = new BufferedReader(picrossFile);

		String titleRow  = picrossStream.readLine();
		String authorRow = picrossStream.readLine();

		if (!titleRow.substring(0, 6).equals("title=")) {
			System.err.println("Error reading the title in "+file);
		}
		author = authorRow.substring(7);

		return author;
	}
	
	/**
	 * Returns the dimensions of the puzzle
	 * @param file
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static String readDimension(InputStream file) throws FileNotFoundException, IOException {
		String dim;
		int nbline;
		int nbCol;

		InputStreamReader picrossFile = new InputStreamReader(file);
		BufferedReader picrossStream = new BufferedReader(picrossFile);

		String titleRow  = picrossStream.readLine();
		String authorRow = picrossStream.readLine();
		String rowsNum  = picrossStream.readLine();
		String colsNum  = picrossStream.readLine();

		if (!titleRow.substring(0, 6).equals("title=")) {
			System.err.println("Error reading the title in "+file);
		}
		nbline = Integer.parseInt(rowsNum.substring(9));
		nbCol= Integer.parseInt(colsNum.substring(11));
		dim = nbline +"x"+nbCol;
		return dim;
	}

	



	/**
	 * test
	 * @param args
	 */
//	public static void main(String[] args) {
//		String fichier = "puzzle10x10-1.pic";
//		try {
//			picrossLecteur.lire(fichier);
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
}
