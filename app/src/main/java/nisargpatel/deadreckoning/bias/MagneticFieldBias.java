package nisargpatel.deadreckoning.bias;

import org.ejml.ops.CommonOps;
import org.ejml.simple.SimpleMatrix;

import nisargpatel.deadreckoning.extra.ExtraFunctions;

public class MagneticFieldBias {

    /*
        From Pg. 14 of "Calibrating an eCompass in the Presence of Hard and Soft-iron Interference, Rev. 3"

        Example:
        float[][] data = {{167.4f, -242.4f, 91.7f},
                          {140.3f, -221.9f, 86.8f},
                          {152.4f, -230.4f, -0.6f},
                          {180.3f, -270.6f, 71.0f},
                          {190.9f, -212.4f, 62.7f},
                          {192.9f, -242.4f, 17.1f}};

        The above data will produce the following results:

        XTX
            139883.508  -196159.625  52207.910  831.300
            -196159.625  279419.645  -73880.837  -1177.700
            52207.910  -73880.837  24915.780  311.600
            831.300  -1177.700  311.600   5.000

        XTY
            74583526.000
            -105754734.000
            28544634.836
            444218.930

        XTX_Inverse
            0.001   0.000  -0.000  -0.078
            0.000   0.001   0.000   0.103
            -0.000   0.000   0.000   0.004
            -0.078   0.103   0.004  37.372

        B
            311.717
            -478.286
            91.525
            -81341.651
        */

    private double[][] XTX; //X-Transposed * X is a 4x4 matrix
    private double[][] XTY; //X_Transposed * Y is a 4x1 vector (stored in a matrix for easier manipulation)

    boolean firstRun;

    float reserveX, reserveY, reserveZ;

    public MagneticFieldBias() {
        firstRun = true;
        XTX = new double[4][4];
        XTY = new double[4][1];
    }

    public void calcBias(float[] rawMagneticValues) {

        float x, y, z;

        //TODO: figure out if reserve values are needed
        //the bias is calculated by n-l values instead of n values (according to the paper)
        //so the following if keeps the latest set of values n reserve
        if (firstRun) {
            reserveX = rawMagneticValues[0];
            reserveY = rawMagneticValues[1];
            reserveZ = rawMagneticValues[2];

            firstRun = false;
            return;
        } else {
            x = reserveX;
            y = reserveY;
            z = reserveZ;

            reserveX = rawMagneticValues[0];
            reserveY = rawMagneticValues[1];
            reserveZ = rawMagneticValues[2];
        }

        //calculating magnetic field bias
        XTX = new double[][]{{XTX[0][0] + x * x, XTX[0][1] + x * y, XTX[0][2] + x * z, XTX[0][3] + x},
                             {XTX[1][0] + x * y, XTX[1][1] + y * y, XTX[1][2] + y * z, XTX[1][3] + y},
                             {XTX[2][0] + x * z, XTX[2][1] + y * z, XTX[2][2] + z * z, XTX[2][3] + z},
                             {XTX[3][0] + x,     XTX[3][1] + y,     XTX[3][2] + z,     XTX[3][3] + 1}};

        XTY = new double[][] {{XTY[0][0] + x * (x * x + y * y + z * z)},
                              {XTY[1][0] + y * (x * x + y * y + z * z)},
                              {XTY[2][0] + z * (x * x + y * y + z * z)},
                              {XTY[3][0] + (x * x + y * y + z * z)}};

    }

    public float[] getBias() {
        SimpleMatrix M_XTX = new SimpleMatrix(XTX);
        SimpleMatrix M_XTY = new SimpleMatrix(XTY);

        SimpleMatrix M_XTX_Inverse = new SimpleMatrix(new double[4][4]);
        CommonOps.invert(M_XTX.getMatrix(), M_XTX_Inverse.getMatrix());

        SimpleMatrix M_B = M_XTX_Inverse.mult(M_XTY);

        float[][] B = ExtraFunctions.denseMatrixToArray(M_B.getMatrix());

        float xBias = B[0][0] / 2.0f;
        float yBias = B[1][0] / 2.0f;
        float zBias = B[2][0] / 2.0f;
        float magneticFieldStrength = (float)Math.sqrt(B[3][0] + Math.pow(xBias, 2) + Math.pow(yBias, 2) + Math.pow(zBias, 2));

        return new float[] {xBias, yBias, zBias, magneticFieldStrength};
    }


}
