
package nisargpatel.inertialnavigation.heading;

import nisargpatel.inertialnavigation.extra.NPExtras;

//heading inference determine using Euler angles and the Direction Cosine Matrix
public class EulerHeadingInference {

    private float[][] c;

    public EulerHeadingInference() {
        this(NPExtras.getIdentityMatrix());
    }

    public EulerHeadingInference(float[][] startingMatrix) {
        c = startingMatrix.clone();
    }

    public float getCurrentHeading(float[] gyroValue) {
        float wX = gyroValue[0];
        float wY = gyroValue[1];
        float wZ = gyroValue[2];

        float[][] a = calcMatrixA(wX, wY, wZ);

        calcMatrixC(a);

        //calculate and return current heading
        return (float) (Math.atan2(c[1][0], c[0][0]));
    }

    private float[][] calcMatrixB(float wX, float wY, float wZ) {
        return (new float[][]{{0, -wZ, wY},
                              {wZ, 0, -wX},
                              {-wY, wX, 0}});
    }

    private float[][] calcMatrixA(float wX, float wY, float wZ) {

        float[][] a;
        float[][] b = calcMatrixB(wX, wY, wZ);
        float[][] bSq = NPExtras.multiplyMatrices(b, b);

        float norm = calcNorm(wX, wY, wZ);
        float bScaleFactor = calcBScaleFactor(norm);
        float bSqScaleFactor = calcBSqScaleFactor(norm);

        b = NPExtras.scaleMatrix(b, bScaleFactor);
        bSq = NPExtras.scaleMatrix(bSq, bSqScaleFactor);

        a = NPExtras.addMatrices(b, bSq);
        a = NPExtras.addMatrices(a, NPExtras.getIdentityMatrix());

        return a;
    }

    private float calcNorm(float wX, float wY, float wZ) {
        return (float) (Math.sqrt(Math.pow(wX, 2) + Math.pow(wY, 2) + Math.pow(wZ, 2)));
    }

    //(sin σ) / σ ≈ 1 - (σ^2 / 3!) + (σ^4 / 5!)
    private float calcBScaleFactor(float sigma) {
        //return (float) ((1 - Math.cos(sigma)) / Math.pow(sigma, 2));
        float sigmaSqOverThreeFactorial = (float) Math.pow(sigma, 2) / NPExtras.factorial(3);
        float sigmaToForthOverFiveFactorial = (float) Math.pow(sigma, 4) / NPExtras.factorial(5);
        return (float) (1.0 - sigmaSqOverThreeFactorial + sigmaToForthOverFiveFactorial);
    }

    //(1 - cos σ) / σ^2 ≈ (1/2) - (σ^2 / 4!) + (σ^4 / 6!)
    private float calcBSqScaleFactor(float sigma) {
        //return (float) (Math.sin(sigma) / sigma);
        float sigmaSqOverFourFactorial = (float) Math.pow(sigma, 2) / NPExtras.factorial(4);
        float sigmaToForthOverSixFactorial = (float) Math.pow(sigma, 4) / NPExtras.factorial(6);
        return (float) (0.5 - sigmaSqOverFourFactorial + sigmaToForthOverSixFactorial);
    }

    private void calcMatrixC(float[][] a) {
        c = NPExtras.multiplyMatrices(c, a);
    }

    public void clearMatrix() {
        c = NPExtras.getIdentityMatrix();
    }

}
