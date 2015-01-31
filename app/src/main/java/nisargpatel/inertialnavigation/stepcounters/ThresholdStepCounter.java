package nisargpatel.inertialnavigation.stepcounters;

public class ThresholdStepCounter {

	private boolean peakFound;
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
	public boolean stepFound( double acc) {
		
		//if no new peak is found, then the program will look for a peak which is above the upperThreshold
		if (!peakFound) {
			if (acc > upperThreshold) {
				peakFound = true;
				return true;
			}
		}

		//after a new peak is found, program will find no more peaks until graph passes under lowerThreshold
		if (peakFound) {
			if (acc < lowerThreshold) {
				peakFound = false;
			}
		}

		return false;
	}

}