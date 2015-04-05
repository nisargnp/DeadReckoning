package nisargpatel.deadreckoning.orientation;

import nisargpatel.deadreckoning.extra.ExtraFunctions;

public class GyroIntegration {

    private boolean isFirstRun;
    private float sensitivity;
    private float lastTimestamp;
    private float[] gyroBias;


    public GyroIntegration() {
        this.gyroBias = new float[3];
        this.sensitivity = 0.0025f;
        this.isFirstRun = true;
    }

    public GyroIntegration(float sensitivity, float[] gyroBias) {
        this();
        this.sensitivity = sensitivity;
        this.gyroBias = gyroBias;
    }

    public float[] getIntegratedValues(long timestamp, float[] rawGyroValues) {
        //get the first timestamp
        if (isFirstRun) {
            lastTimestamp = ExtraFunctions.nsToSec(timestamp);
            isFirstRun = false;
        }

        float[] unbiasedGyroValues = removeBias(rawGyroValues);

        //return deltaOrientation[]
        return calcIntegratedValues(timestamp, unbiasedGyroValues);
    }

    public void setBias(float[] gyroBias) {
        this.gyroBias = gyroBias;
    }

    private float[] removeBias(float[] rawGyroValues) {
        float[] unbiasedGyroValues = new float[3];

        unbiasedGyroValues[0] = rawGyroValues[0] - gyroBias[0];
        unbiasedGyroValues[1] = rawGyroValues[1] - gyroBias[1];
        unbiasedGyroValues[2] = rawGyroValues[2] - gyroBias[2];

        //applying a quick high pass filter
        unbiasedGyroValues[0] = (Math.abs(unbiasedGyroValues[0]) > sensitivity) ? unbiasedGyroValues[0] : 0;
        unbiasedGyroValues[1] = (Math.abs(unbiasedGyroValues[1]) > sensitivity) ? unbiasedGyroValues[1] : 0;
        unbiasedGyroValues[2] = (Math.abs(unbiasedGyroValues[2]) > sensitivity) ? unbiasedGyroValues[2] : 0;

        return unbiasedGyroValues;
    }

    private float[] calcIntegratedValues(long timestamp, float[] gyroValues) {
        double currentTime = ExtraFunctions.nsToSec(timestamp);
        double deltaTime = currentTime - lastTimestamp;

        float[] deltaOrientation = new float[3];
        deltaOrientation[0] = (float) deltaTime * gyroValues[0];
        deltaOrientation[1] = (float) deltaTime * gyroValues[1];
        deltaOrientation[2] = (float) deltaTime * gyroValues[2];

        lastTimestamp = (float) currentTime;

        return deltaOrientation;
    }

}
