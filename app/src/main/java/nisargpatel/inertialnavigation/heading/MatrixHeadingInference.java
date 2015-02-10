package nisargpatel.inertialnavigation.heading;

import nisargpatel.inertialnavigation.math.MathFunctions;

public class MatrixHeadingInference {

    private float xBias;
    private float yBias;
    private float zBias;

    private float currentHeading;

    private float[][] c;

    private final float[][] IDENTITY_MATRIX = new float[][]{{1,0,0},
                                                     {0,1,0},
                                                     {0,0,1}};

    private MatrixHeadingInference() {}

    public MatrixHeadingInference(float[][] startingMatrix) {
        c = startingMatrix.clone();
    }

    public void setBias(float[] gyroBias) {
        this.xBias = gyroBias[0];
        this.yBias = gyroBias[1];
        this.zBias = gyroBias[2];
    }

    public float getCurrentHeading(float[] gyroValue) {
        float wX = gyroValue[0] - xBias;
        float wY = gyroValue[1] - yBias;
        float wZ = gyroValue[2] - zBias;

        float[][] b = calcMatrixB(wX, wY, wZ);
        float[][] a = calcMatrixA(b, wX, wY, wZ);

        calcMatrixC(a);

        currentHeading = (float) (Math.atan2(c[0][2], c[2][2]));
        return currentHeading;
    }

    private float[][] calcMatrixB(float wX, float wY, float wZ) {
        return (new float[][]{{0, -wZ, wY},
                             {wZ, 0, -wX},
                             {-wY, wX, 0}});
    }

    private float[][] calcMatrixA(float[][] b, float wX, float wY, float wZ) {
        float[][] a;

        float omegaMagnitude = calcOmegaMagnitude(wX, wY, wZ);
        float bScaleFactor = calcBScaleFactor(omegaMagnitude);
        float bSqScaleFactor = calcBSqScaleFactor(omegaMagnitude);

        float[][] bSq = MathFunctions.multiplyMatrices(b, b);

        b = MathFunctions.scaleMatrix(b, bScaleFactor);
        bSq = MathFunctions.scaleMatrix(bSq, bSqScaleFactor);

        a = MathFunctions.addMatrices(b, bSq);
        a = MathFunctions.addMatrices(a, IDENTITY_MATRIX);

        return a;
    }

    private float calcOmegaMagnitude(float wX, float wY, float wZ) {
        return (float) (Math.sqrt(Math.pow(wX, 2) + Math.pow(wY, 2) + Math.pow(wZ, 2)));
    }

    private float calcBScaleFactor(float omegaMagnitude) {
        return (float) ((1 - Math.cos(omegaMagnitude)) / Math.pow(omegaMagnitude, 2));
    }

    private float calcBSqScaleFactor(float omegaMagnitude) {
        return (float) (Math.sin(omegaMagnitude) / omegaMagnitude);
    }

    private void calcMatrixC(float[][] a) {
        c = MathFunctions.multiplyMatrices(c, a);
    }

    public double getXPoint(double radius) {
        double angle = currentHeading % Math.PI;
        return radius * Math.cos(angle);
    }

    public double getYPoint(double radius) {
        double angle = currentHeading % Math.PI;
        return radius * Math.sin(angle);
    }

}
