package nisargpatel.deadreckoning.orientation;

import nisargpatel.deadreckoning.extra.ExtraFunctions;

public class GyroscopeDeltaOrientation {

    private boolean isFirstRun;
    private float sensitivity;
    private float lastTimestamp;
    private float[] gyroBias;


    public GyroscopeDeltaOrientation() {
        this.gyroBias = new float[3];
        this.sensitivity = 0.0025f;
        this.isFirstRun = true;
    }

    public GyroscopeDeltaOrientation(float sensitivity, float[] gyroBias) {
        this();
        this.sensitivity = sensitivity;
        this.gyroBias = gyroBias;
    }

    public float[] calcDeltaOrientation(long timestamp, float[] rawGyroValues) {
        //get the first timestamp
        if (isFirstRun) {
            isFirstRun = false;
            lastTimestamp = ExtraFunctions.nsToSec(timestamp);
            return new float[3];
        }

        float[] unbiasedGyroValues = removeBias(rawGyroValues);

        //return deltaOrientation[]
        return integrateValues(timestamp, unbiasedGyroValues);
    }

    public void setBias(float[] gyroBias) {
        this.gyroBias = gyroBias;
    }

    private float[] removeBias(float[] rawGyroValues) {
        //ignoring the last 3 values of TYPE_UNCALIBRATED_GYROSCOPE, since the are only the Android-calculated biases
        float[] unbiasedGyroValues = new float[3];

        for (int i = 0; i < 3; i++)
            unbiasedGyroValues[i] = rawGyroValues[i] - gyroBias[i];

        //TODO: check how big of a difference this makes
        //applying a quick high pass filter
        for (int i = 0; i < 3; i++)
            if (Math.abs(unbiasedGyroValues[i]) > sensitivity)
                unbiasedGyroValues[i] = unbiasedGyroValues[i];
            else
                unbiasedGyroValues[i] = 0;

        return unbiasedGyroValues;
    }

    private float[] integrateValues(long timestamp, float[] gyroValues) {
        double currentTime = ExtraFunctions.nsToSec(timestamp);
        double deltaTime = currentTime - lastTimestamp;

        float[] deltaOrientation = new float[3];

        //integrating angular velocity with respect to time
        for (int i = 0; i < 3; i++)
            deltaOrientation[i] = gyroValues[i] * (float)deltaTime;

        lastTimestamp = (float) currentTime;

        return deltaOrientation;
    }

}
