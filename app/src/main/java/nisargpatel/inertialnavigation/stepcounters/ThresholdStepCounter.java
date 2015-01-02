package nisargpatel.inertialnavigation.stepcounters;

import java.util.ArrayList;

public class ThresholdStepCounter {

	private boolean peakFound;
	private ArrayList<Double> peakLocation = new ArrayList<Double>();

	private double upperThreshold;
	private double lowerThreshold;

	//constructor sets the values for the upper and lower thresholds
	public ThresholdStepCounter(double upper, double lower) {
        upperThreshold = upper;
        lowerThreshold = lower;
		peakFound = false;
	}
	
	//change the thresholds after the program is instantiated
	public void setThresholds(double upper, double lower) {
		upperThreshold = upper;
		lowerThreshold = lower;
	}
	
	//determines if graph peaks (step found), and if so returns true
	public boolean stepFound(double time, double acc) {
		
		//if no new peak is found, then the program will look for a peak which is above the upperThreshold
		if (peakFound == false) {
			if (acc > upperThreshold) {
				peakFound = true;
				peakLocation.add(time);
				return true;
			}
		}

		//after a new peak is found, program will find no more peaks until graph passes under lowerThreshold
		if (peakFound == true) {
			if (acc < lowerThreshold) {
				peakFound = false;
			}
		}

		return false;
	}

}