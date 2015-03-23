
package nisargpatel.locationtrack.heading;

import nisargpatel.locationtrack.extra.ExtraFunctions;

//heading inference determine using Euler angles and the Direction Cosine Matrix
public class EulerHeadingInference {

    private float[][] C;

    public EulerHeadingInference() {
        this(ExtraFunctions.IDENTITY_MATRIX);
    }

    public EulerHeadingInference(float[][] startingMatrix) {
        C = startingMatrix.clone();
    }

    public float getCurrentHeading(float[] gyroValue) {
        float wX = gyroValue[0];
        float wY = gyroValue[1];
        float wZ = gyroValue[2];

        float[][] A = calcMatrixA(wX, wY, wZ);

        calcMatrixC(A);

        //calculate and return current heading
        return (float) (Math.atan2(C[1][0], C[0][0]));
    }

    private float[][] calcMatrixB(float wX, float wY, float wZ) {
        return (new float[][]{{0, -wZ, wY},
                              {wZ, 0, -wX},
                              {-wY, wX, 0}});
    }

    private float[][] calcMatrixA(float wX, float wY, float wZ) {

        float[][] A;
        float[][] B = calcMatrixB(wX, wY, wZ);
        float[][] B_sq = ExtraFunctions.multiplyMatrices(B, B);

        float norm = calcNorm(wX, wY, wZ);
        float B_scaleFactor = calcBScaleFactor(norm);
        float B_sqScaleFactor = calcBSqScaleFactor(norm);

        B = ExtraFunctions.scaleMatrix(B, B_scaleFactor);
        B_sq = ExtraFunctions.scaleMatrix(B_sq, B_sqScaleFactor);

        A = ExtraFunctions.addMatrices(B, B_sq);
        A = ExtraFunctions.addMatrices(A, ExtraFunctions.IDENTITY_MATRIX);

        return A;
    }

    private float calcNorm(float wX, float wY, float wZ) {
        return (float) (Math.sqrt(Math.pow(wX, 2) + Math.pow(wY, 2) + Math.pow(wZ, 2)));
    }

    //(sin σ) / σ ≈ 1 - (σ^2 / 3!) + (σ^4 / 5!)
    private float calcBScaleFactor(float sigma) {
        //return (float) ((1 - Math.cos(sigma)) / Math.pow(sigma, 2));
        float sigmaSqOverThreeFactorial = (float) Math.pow(sigma, 2) / ExtraFunctions.factorial(3);
        float sigmaToForthOverFiveFactorial = (float) Math.pow(sigma, 4) / ExtraFunctions.factorial(5);
        return (float) (1.0 - sigmaSqOverThreeFactorial + sigmaToForthOverFiveFactorial);
    }

    //(1 - cos σ) / σ^2 ≈ (1/2) - (σ^2 / 4!) + (σ^4 / 6!)
    private float calcBSqScaleFactor(float sigma) {
        //return (float) (Math.sin(sigma) / sigma);
        float sigmaSqOverFourFactorial = (float) Math.pow(sigma, 2) / ExtraFunctions.factorial(4);
        float sigmaToForthOverSixFactorial = (float) Math.pow(sigma, 4) / ExtraFunctions.factorial(6);
        return (float) (0.5 - sigmaSqOverFourFactorial + sigmaToForthOverSixFactorial);
    }

    private void calcMatrixC(float[][] a) {
        C = ExtraFunctions.multiplyMatrices(C, a);
    }

    public void clearMatrix() {
        C = ExtraFunctions.IDENTITY_MATRIX;
    }

}
