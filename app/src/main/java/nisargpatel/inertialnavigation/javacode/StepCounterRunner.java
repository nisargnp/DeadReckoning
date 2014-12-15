package nisargpatel.inertialnavigation.javacode;

import java.io.IOException;
import java.util.ArrayList;

class StepCounterRunner {

	private static String filePath = "E://myFile.txt";

	private static final int UPPER_THRESHOLD = 5;
	private static final int LOWER_THRESHOLD = 4;

	private static int stepCount = 0;

	private static ArrayList<Double> zAcc = new ArrayList<Double>();
	private static ArrayList<Double> time = new ArrayList<Double>();

	private static String[] fileData = null;

	public static void main(String[] args) throws IOException {

		//open the file and copy data to fileData[], and catch any errors
		try {
			ReadFile myFile = new ReadFile(filePath);
			fileData = myFile.openFile();
		}
		catch (IOException e) {
			System.out.println(e.getMessage());
		}

		//Create a StepCounter object
		ThresholdStepCounter manualCountSteps = new ThresholdStepCounter(LOWER_THRESHOLD, UPPER_THRESHOLD);
		MovingAverageStepCounter autoCountSteps = new MovingAverageStepCounter(1.0);

		//Move the data from fileDaya[] to time and zAcc ArrayLists
		setSensorData("time");
		setSensorData("zAcc");

		//Run the ManualStepCounter on the zAcc data
		for (int i = 1; i < zAcc.size(); i++) {
			if (manualCountSteps.stepFound(time.get(i), zAcc.get(i)) == true)
				stepCount++;
		}  

		//Output the number of steps counted
		System.out.println("Number of steps: " + stepCount);

		stepCount = 0;

		//Run the AutoStepCounter on the zAcc data
		for (int i = 1; i < zAcc.size(); i++) {
			if (autoCountSteps.stepFound(time.get(i), zAcc.get(i)) == true)
				stepCount++;
		}
		//Output the number of steps counted
		System.out.println("Number of steps: " + stepCount);

	}

	private static void setSensorData(String type) {

		for (int i = 0; i < fileData.length; i++) {

			//if the data begins with a number between 1 and 9, then it is saved to the appropriate ArrayList
			if ((int) fileData[i].charAt(0) >= 48 && (int) fileData[i].charAt(0) <= 57) {

				//Get the start and end positions of the desired data
				int startIndex = getDataStartIndex(type, i);
				int endIndex = getDataEndIndex(type, i);

				if (type.equals("time"))
					time.add(Double.parseDouble(fileData[i].substring(startIndex, endIndex)));
				else
					zAcc.add(Double.parseDouble(fileData[i].substring(startIndex, endIndex)));
			}
		}
	}

	//sets the appropriate START position for the selected data
	private static int getDataStartIndex(String type, int i) {
		int startIndex = 0;

		if (type.equals("time")) {
			startIndex = 0;
		}
		else {
			startIndex = fileData[i].lastIndexOf(";", fileData[i].lastIndexOf(";") - 1) + 1;
		}

		return startIndex;
	}

	//sets the appropriate END position for the selected data
	private static int getDataEndIndex(String type, int i) {
		int endIndex = 0;

		if (type.equals("time")) {
			endIndex = fileData[i].indexOf(";");
		}
		else {
			endIndex = fileData[i].lastIndexOf(";");
		}

		return endIndex;
	}

}