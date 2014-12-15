package nisargpatel.inertialnavigation.javacode;

import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;

public class ReadFile {

	private String filePath;

	public ReadFile(String path) {
		filePath = path;
	}

	public void setFileName(String path) {
		filePath = path;
	}

	public String[] openFile() throws IOException {

		//FileReader reads one char at a time
		FileReader charReader = new FileReader(filePath);
		//If FileReader is passed to BufferedReader, then all text is stored in memory by BufferedReader until it is called
		BufferedReader textReader = new BufferedReader(charReader);

		//ArrayList<Double> textData = new ArrayList<Double>();
		String[] textData = new String[getNumberOfLines()];

		for (int i = 0; i < getNumberOfLines(); i++) {
			textData[i] = textReader.readLine();
		}

		textReader.close();
		
		//returns the data from the text file via an array
		return textData;

	}
	
	//calculates the number of lines in the text file to be read
	private int getNumberOfLines() throws IOException {

		//FileReader charReader = new FileReader(filePath);
		//BufferedReader textReader = new BufferedReader(charReader);

		BufferedReader textReader = new BufferedReader(new FileReader(filePath));

		int linesCount = 0;
		
		//increments linesCount by 1 every time a line is found until no more lines are found
		while (textReader.readLine() != null) {
			linesCount++;
		}

		textReader.close();
		
		//returns the number of lines found in the text file
		return linesCount;
	}

}
