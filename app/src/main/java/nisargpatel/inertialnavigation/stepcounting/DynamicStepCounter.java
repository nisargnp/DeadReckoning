package nisargpatel.inertialnavigation.stepcounting;

import java.io.IOException;
import java.util.ArrayList;

import nisargpatel.inertialnavigation.extra.NPExtras;
import nisargpatel.inertialnavigation.filewriting.DataFileWriter;

public class DynamicStepCounter {

    private static final String folderName = "Inertial_Navigation_Data/Moving_Average_Step_Counter";
    private static final String[] DATA_FILE_NAME = {"StepCount"};
    private static final String[] DATA_FILE_HEADING = {"lowerThreshold;upperThreshold;acc;findStep"};

    private DataFileWriter dataFileWriter;

    private static final int REQUIRED_HZ = 500;

    private double upperThreshold, lowerThreshold;
    private int stepCount;

    private double sumUpperAcc, sumLowerAcc;
    private int upperCount, lowerCount;
    private double sumAcc, avgAcc;
    private int runCount;
    private boolean peakFound;
    private double sensitivity;
    private boolean hzCheck;

    public DynamicStepCounter() {

        try {
            dataFileWriter = new DataFileWriter(folderName, NPExtras.arrayToList(DATA_FILE_NAME), NPExtras.arrayToList(DATA_FILE_HEADING));
        } catch (IOException e) {
            e.printStackTrace();
        }

        upperThreshold = 10.8;
        lowerThreshold = 8.8;
        avgAcc = 0;
        sensitivity = 0;
        stepCount = 0;
        peakFound = false;
        hzCheck = false;
    }

    //Set the default values for variables
    public DynamicStepCounter(double sensitivity) {
        this();
        this.sensitivity = sensitivity;
    }

    //determines if graph peaks (step found), and if so returns true
    public boolean findStep(double acc) {

        //run setThresholds() to determine what the peak detection thresholds will be
        setThresholdsDiscreet(acc);
        //setThresholdsDiscreet2(acc);
        //setThresholdsContinuous(acc);

//        Log.d("values", "lowerThreshold: " + lowerThreshold + "; acc: " + acc + "; upperThreshold: " + upperThreshold);

        //no peaks are found during the first 500 points while the thresholds are set
        if (hzCheck) {

            //if no new peak is found, then the program will look for a peak which is above the upperThreshold
            if (acc > upperThreshold) {

//                Log.d("above_upper", "above upper threshold");

                if (!peakFound) {

//                    Log.d("step_found", "step found");

                    ArrayList<Float> values = new ArrayList<>();
                    values.add((float)lowerThreshold);
                    values.add((float)upperThreshold);
                    values.add((float)acc);
                    values.add((float)1);
                    dataFileWriter.writeToFile("StepCount", values);

                    stepCount++;
                    peakFound = true;
                    return true;
                }
            }

            //after a new peak is found, program will find no more peaks until graph passes under lowerThreshold
            else if (acc < lowerThreshold) {

//                Log.d("below_lower", "below lower threshold");

                if (peakFound) {
                    peakFound = false;
                }

            }

        }

        ArrayList<Float> values = new ArrayList<>();
        values.add((float)lowerThreshold);
        values.add((float)upperThreshold);
        values.add((float)acc);
        values.add((float) 0);

        dataFileWriter.writeToFile("StepCount", values);

        return false;
    }

    //sets the thresholds that are used to find the peaks
    public void setThresholdsDiscreet(double acc) {

        runCount++;

        //the first time that runCount reaches half of the REQUIRED_HZ, hzCheck becomes true
        if (runCount >= REQUIRED_HZ / 2)
            hzCheck = true;

        //ONLY until runCount reaches half of REQUIRED_HZ for the FIRST TIME, only the avgAcc is desired
        if (!hzCheck) {
            sumAcc += acc;
        }
        //after the first time runCount reaches half of REQUIRED_HZ, the program starts the process of calculating thresholds
        else {
            sumAcc += acc;

            if (acc > avgAcc) {
                sumUpperAcc += acc;
                upperCount++;
            }
            else if (acc < avgAcc) {
                sumLowerAcc += acc;
                lowerCount++;
            }
        }

        //After REQUIRED_HZ number of points, the program calculates a new set of thresholds
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

    private boolean firstRun = true;

    //sets the thresholds that are used to find the peaks
    public void setThresholdsDiscreet2(double acc) {

        hzCheck = true;

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

        hzCheck = true;

        runCount++;

        if (runCount == 1) {
            upperThreshold = acc + sensitivity;
            lowerThreshold = acc - sensitivity;

            avgAcc = acc;

            return;
        }

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
