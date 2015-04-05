package nisargpatel.deadreckoning.bias;

public class GyroscopeBias {

    private int runCount;
    private int trials;
    private float[] gyroBias;

    GyroscopeBias() {
        runCount = 0;
        trials = 0;
        gyroBias = new float[3];
    }

    public GyroscopeBias(int trials) {
        this();
        this.trials = trials;
    }

    public boolean calcBias(float[] rawGyroValues) {
        runCount++;

        if (runCount >= trials)
            return true;

        if (runCount == 1) {
            gyroBias[0] = rawGyroValues[0];
            gyroBias[1] = rawGyroValues[1];
            gyroBias[2] = rawGyroValues[2];
            return false;
        }

        //averaging bias for the first few hundred data points
        //movingAverage = movingAverage * ((n-1)/n) + newValue * (1/n)
        float n = (float) runCount;
        gyroBias[0] = gyroBias[0] * ((n-1)/n) + rawGyroValues[0] * (1/n);
        gyroBias[1] = gyroBias[1] * ((n-1)/n) + rawGyroValues[1] * (1/n);
        gyroBias[2] = gyroBias[2] * ((n-1)/n) + rawGyroValues[2] * (1/n);

        return false;
    }

    public float[] getBias() {
        return gyroBias.clone();
    }

}
