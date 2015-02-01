package nisargpatel.inertialnavigation.stepcounter;

public class MovingAverageStepCounter {
	private double upperThreshold, lowerThreshold;

	private double sumUpperAcc, sumLowerAcc;
	private int numUpper, numLower;
	private double sumAcc, avgAcc;
	private int runCount;
	private boolean peakFound;
    private double margin;
    private boolean hzCheck;

	//Set the default values for variables
	public MovingAverageStepCounter(double margin) {
		upperThreshold = 0;
		lowerThreshold = 0;
		avgAcc = 0;
		peakFound = false;
		hzCheck = false;

        this.margin = margin;
	}
	
	//determines if graph peaks (step found), and if so returns true
	public boolean stepFound(double acc) {
		
		//run setThresholds() to determine what the peak detection thresholds will be
		setThresholds(acc);

		//no peaks are found during the first 500 points while the thresholds are set
		if (hzCheck) {
			
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

		}

		return false;
	}
	
	//sets the thresholds that are used to find the peaks
	public void setThresholds(double acc) {

		runCount++;
		
		//the first time that runCount reaches half of the REQUIRED_HZ, hzCheck becomes true
        int REQUIRED_HZ = 500;
        if (runCount >= REQUIRED_HZ / 2)
			hzCheck = true;
		
		//ONLY until runCount reaches half of REQUIRED_HZ for the FIRST TIME, only the avgAcc is desired
		if (!hzCheck) {
			sumAcc += acc;
		}
		//after the first time runCount reaches half of REQUIRED_HZ, the program starts the process calculating thresholds
		else {
			sumAcc += acc;

			if (acc > avgAcc) {
				sumUpperAcc += acc;
				numUpper++;
			}
			else if (acc < avgAcc) {
				sumLowerAcc += acc;
				numLower++;
			}
		}

		//After REQUIRED_HZ number of points, the program calculates a new set of thresholds
		if (runCount == REQUIRED_HZ) {
			avgAcc = sumAcc / REQUIRED_HZ;

			upperThreshold = (sumUpperAcc / numUpper) - margin;
			lowerThreshold = (sumLowerAcc / numLower) - margin;

            sumAcc = 0;

			sumUpperAcc = 0;
            numUpper = 0;

			sumLowerAcc = 0;
            numLower = 0;
			
			runCount = 0;
		}
	}

}
