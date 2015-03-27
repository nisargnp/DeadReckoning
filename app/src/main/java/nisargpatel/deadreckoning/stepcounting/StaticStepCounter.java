package nisargpatel.deadreckoning.stepcounting;

public class StaticStepCounter {

	private boolean peakFound;
	private double upperThreshold;
	private double lowerThreshold;
    private int stepCount;

    public StaticStepCounter() {
        upperThreshold = 10.8;
        lowerThreshold = 8.8;
        stepCount = 0;
        peakFound = false;
    }

	//constructor sets the values for the upper and lower thresholds
	public StaticStepCounter(double upper, double lower) {
        this();
        upperThreshold = upper;
        lowerThreshold = lower;
	}
	
	//change the thresholds after the program is instantiated
	public void setThresholds(double upper, double lower) {
		upperThreshold = upper;
		lowerThreshold = lower;
	}
	
	//determines if graph peaks (step found), and if so returns true
	public boolean findStep(double acc) {
		
		//if no new peak is found, then the program will look for a peak which is above the upperThreshold
		if (!peakFound) {
			if (acc > upperThreshold) {
				peakFound = true;
                stepCount++;
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

    public double getUpperThreshold() {
        return upperThreshold;
    }

    public double getLowerThreshold() {
        return lowerThreshold;
    }

    public int getStepCount() {
        return stepCount;
    }

    public void clearStepCount() {
        stepCount = 0;
    }
}