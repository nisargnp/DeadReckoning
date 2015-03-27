package nisargpatel.deadreckoning.heading;

public class MagneticFieldBias {

    private int trials;
    private int runCount;

    private float[] magneticBias;

    public MagneticFieldBias() {
        trials = 300;
        runCount = 0;
        magneticBias = new float[3];
    }

    public MagneticFieldBias(int trials) {
        this();
        this.trials = trials;
    }

    public boolean calcBias(float[] rawMagneticValues) {
        runCount++;

        if (runCount >= trials)
            return true;

        if (runCount == 1) {
            magneticBias[0] = rawMagneticValues[0];
            magneticBias[1] = rawMagneticValues[1];
            magneticBias[2] = rawMagneticValues[2];
            return false;
        }

        //averaging magneticBias for the first few hundred data points
        //movingAverage = movingAverage * ((n-1)/n) + newValue * (1/n)
        float n = (float) runCount;
        magneticBias[0] = magneticBias[0] * ((n-1)/n) + rawMagneticValues[0] * (1/n);
        magneticBias[1] = magneticBias[1] * ((n-1)/n) + rawMagneticValues[1] * (1/n);
        magneticBias[2] = magneticBias[2] * ((n-1)/n) + rawMagneticValues[2] * (1/n);

        return false;
    }

    public float[] getBias() {
        return magneticBias.clone();
    }

}
