package nisargpatel.deadreckoning.stepcounting;

public class DynamicStepCounter {

    public static final int REQUIRED_HZ = 500;

    private int stepCount;
    private double sensitivity;
    private double upperThreshold, lowerThreshold;

    private boolean firstRun;
    private boolean peakFound;

    private int upperCount, lowerCount;
    private double sumUpperAcc, sumLowerAcc;

    private double sumAcc, avgAcc;
    private int runCount;

    public DynamicStepCounter() {

        stepCount = 0;
        sensitivity = 1.0;
        upperThreshold = 10.8;
        lowerThreshold = 8.8;

        firstRun = true;
        peakFound = false;

        upperCount = lowerCount = 0;
        sumUpperAcc = sumLowerAcc = 0;

        sumAcc = avgAcc = 0;
        runCount = 0;

    }

    //Set the default values for variables
    public DynamicStepCounter(double sensitivity) {
        this();
        this.sensitivity = sensitivity;
    }

    //determines if graph peaks (step found), and if so returns true
    public boolean findStep(double acc) {

        //set the thresholds that are used to find the peaks
//        setThresholdsDiscreet(acc);
        setThresholdsContinuous(acc);

        //finds a point (peak) above the upperThreshold
        if (acc > upperThreshold) {
            if (!peakFound) {
                stepCount++;
                peakFound = true;
                return true;
            }
        }

        //after a new peak is found, program will find no more peaks until graph passes under lowerThreshold
        else if (acc < lowerThreshold) {
            if (peakFound) {
                peakFound = false;
            }
        }

        return false;
    }

    public void setThresholdsDiscreet(double acc) {

        runCount++;

        if (firstRun) {
            upperThreshold = acc + sensitivity;
            lowerThreshold = acc - sensitivity;
            avgAcc = acc;
            firstRun = false;
        }

        sumAcc += acc;

        if (acc > avgAcc) {
            sumUpperAcc += acc;
            upperCount++;
        }
        else if (acc < avgAcc) {
            sumLowerAcc += acc;
            lowerCount++;
        }

        if (runCount == REQUIRED_HZ) {
            avgAcc = sumAcc / REQUIRED_HZ;

            upperThreshold = (sumUpperAcc / upperCount) + sensitivity;
            lowerThreshold = (sumLowerAcc / lowerCount) - sensitivity;

            sumAcc = 0;

            sumUpperAcc = 0;
            upperCount = 0;

            sumLowerAcc = 0;
            lowerCount = 0;

            runCount = 0;
        }

    }

    public void setThresholdsContinuous(double acc) {

        runCount++;

        if (firstRun) {
            upperThreshold = acc + sensitivity;
            lowerThreshold = acc - sensitivity;

            avgAcc = acc;

            firstRun = false;
            return;
        }

        //moving average equation
        avgAcc = ((avgAcc) * (((double)runCount-1.0)/(double)runCount)) + (acc/(double)runCount);

        upperThreshold = avgAcc + sensitivity;
        lowerThreshold = avgAcc - sensitivity;

    }

    public double getSensitivity() {
        return sensitivity;
    }

    public int getStepCount() {
        return stepCount;
    }

    public void clearStepCount() {
        stepCount = 0;
    }

}
