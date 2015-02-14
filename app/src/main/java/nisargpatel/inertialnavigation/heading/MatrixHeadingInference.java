
package nisargpatel.inertialnavigation.heading;

import nisargpatel.inertialnavigation.math.MathFunctions;

public class MatrixHeadingInference {

    private float currentHeading;

    private float[][] c;

    private final float[][] IDENTITY_MATRIX = new float[][]{{1,0,0},
            {0,1,0},
            {0,0,1}};

    private MatrixHeadingInference() {}

    public MatrixHeadingInference(float[][] startingMatrix) {
        c = startingMatrix.clone();
    }

    public float getCurrentHeading(float[] gyroValue) {
        float wX = gyroValue[0];
        float wY = gyroValue[1];
        float wZ = gyroValue[2];

        float[][] a = calcMatrixA(wX, wY, wZ);

        calcMatrixC(a);

        currentHeading = (float) (Math.atan2(c[1][0], c[0][0]));
        return currentHeading;
    }

    private float[][] calcMatrixB(float wX, float wY, float wZ) {
        return (new float[][]{{0, -wZ, wY},
                              {wZ, 0, -wX},
                              {-wY, wX, 0}});
    }

    private float[][] calcMatrixA(float wX, float wY, float wZ) {

        float[][] a;
        float[][] b = calcMatrixB(wX, wY, wZ);
        float[][] bSq = MathFunctions.multiplyMatrices(b, b);

        float norm = calcNorm(wX, wY, wZ);
        float bScaleFactor = calcBScaleFactor(norm);
        float bSqScaleFactor = calcBSqScaleFactor(norm);

        b = MathFunctions.scaleMatrix(b, bScaleFactor);
        bSq = MathFunctions.scaleMatrix(bSq, bSqScaleFactor);

        a = MathFunctions.addMatrices(b, bSq);
        a = MathFunctions.addMatrices(a, IDENTITY_MATRIX);

        return a;
    }

    private float calcNorm(float wX, float wY, float wZ) {
        return (float) (Math.sqrt(Math.pow(wX, 2) + Math.pow(wY, 2) + Math.pow(wZ, 2)));
    }

    //(sin sigma) / sigma ~= 1 - (sigma^2 / 3!) + (sigma^4 / 5!)
    private float calcBScaleFactor(float sigma) {
        //return (float) ((1 - Math.cos(sigma)) / Math.pow(sigma, 2));
        float sigmaSqOverThreeFactorial = (float) Math.pow(sigma, 2) / MathFunctions.factorial(3);
        float sigmaToForthOverFiveFactorial = (float) Math.pow(sigma, 4) / MathFunctions.factorial(5);
        return (float) (1.0 - sigmaSqOverThreeFactorial + sigmaToForthOverFiveFactorial);
    }

    //(1 - cos sigma) / sigma^2 ~= (1/2) - (sigma^2 / 4!) + (sigma^4 / 6!)
    private float calcBSqScaleFactor(float sigma) {
        //return (float) (Math.sin(sigma) / sigma);
        float sigmaSqOverFourFactorial = (float) Math.pow(sigma, 2) / MathFunctions.factorial(4);
        float sigmaToForthOverSixFactorial = (float) Math.pow(sigma, 4) / MathFunctions.factorial(6);
        return (float) (0.5 - sigmaSqOverFourFactorial + sigmaToForthOverSixFactorial);
    }

    private void calcMatrixC(float[][] a) {
        c = MathFunctions.multiplyMatrices(c, a);
    }

    public void clearMatrix() {
        c = IDENTITY_MATRIX;
    }

    public double getXPoint(double radius) {
        return radius * Math.cos(currentHeading);
    }

    public double getYPoint(double radius) {
        return radius * Math.sin(currentHeading);
    }

}
