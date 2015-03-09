package nisargpatel.inertialnavigation.heading;

import nisargpatel.inertialnavigation.extra.NPExtras;

public class GyroscopeIntegration {

    int numTrialsBias;
    int runCount;
    float sensitivity;

    float[] gyroBias;

    float lastTimestamp;

    public GyroscopeIntegration() {
        this.gyroBias = new float[3];
        this.sensitivity = 0.0025f;
        this.runCount = 0;
        this.numTrialsBias = 300;
    }

    public GyroscopeIntegration(int numTrialsBias, float sensitivity) {
        this();
        this.sensitivity = sensitivity;
        this.numTrialsBias = numTrialsBias;
    }

    public float[] getIntegratedValues(long timestamp, float[] rawGyroValues) {
        runCount++;

        //if no bias is to be calculated
        //get the timestamp, and set the bias to the 0
        if (numTrialsBias == 0 && runCount == 1) {
            lastTimestamp = NPExtras.nsToSec(timestamp);
            calcBias(runCount, new float[3]);
        }

        //if bias is to be calculated
        //get timestamp and calculate bias using mov. avg. for the given number of data points
        if (runCount <= numTrialsBias) {
            lastTimestamp = NPExtras.nsToSec(timestamp);
            calcBias(runCount, rawGyroValues);
            return new float[3];
        }

        float[] unbiasedGyroValues = removeBias(rawGyroValues);

        //return deltaOrientation[]
        return calcIntegratedValues(timestamp, unbiasedGyroValues);
    }

    private void calcBias(int runCount, float[] rawGyroValues) {
        if (runCount == 1) {
            gyroBias[0] = rawGyroValues[0];
            gyroBias[1] = rawGyroValues[1];
            gyroBias[2] = rawGyroValues[2];
            return;
        }

        //averaging bias for the first few hundred data points
        //movingAverage = movingAverage * ((n-1)/n) + newValue * (1/n)
        float n = (float) runCount;
        gyroBias[0] = gyroBias[0] * ((n-1)/n) + rawGyroValues[0] * (1/n);
        gyroBias[1] = gyroBias[1] * ((n-1)/n) + rawGyroValues[1] * (1/n);
        gyroBias[2] = gyroBias[2] * ((n-1)/n) + rawGyroValues[2] * (1/n);
    }

    private float[] removeBias(float[] rawGyroValues) {
        float[] unbiasedGyroValues = new float[3];

        unbiasedGyroValues[0] = rawGyroValues[0] - gyroBias[0];
        unbiasedGyroValues[1] = rawGyroValues[1] - gyroBias[1];
        unbiasedGyroValues[2] = rawGyroValues[2] - gyroBias[2];

        unbiasedGyroValues[0] = (Math.abs(unbiasedGyroValues[0]) > sensitivity) ? unbiasedGyroValues[0] : 0;
        unbiasedGyroValues[1] = (Math.abs(unbiasedGyroValues[1]) > sensitivity) ? unbiasedGyroValues[1] : 0;
        unbiasedGyroValues[2] = (Math.abs(unbiasedGyroValues[2]) > sensitivity) ? unbiasedGyroValues[2] : 0;

        return unbiasedGyroValues;
    }

    private float[] calcIntegratedValues(long timestamp, float[] gyroValues) {
        double currentTime = NPExtras.nsToSec(timestamp);
        double deltaTime = currentTime - lastTimestamp;

        float[] deltaOrientation = new float[3];
        deltaOrientation[0] = (float) deltaTime * gyroValues[0];
        deltaOrientation[1] = (float) deltaTime * gyroValues[1];
        deltaOrientation[2] = (float) deltaTime * gyroValues[2];

        lastTimestamp = (float) currentTime;

        return deltaOrientation;
    }

}
