package nisargpatel.inertialnavigation.heading;

import nisargpatel.inertialnavigation.math.MathFunctions;

public class NewHeadingInference {

    //where w is calibrated
    float wX;
    float wY;
    float wZ;

    float xBias;
    float yBias;
    float zBias;


    float omegaMagnitude;
    float scaleFactor;

    float[][] c;
    float[][] b;
    float[][] a;
    float[][] identity;

    private NewHeadingInference() {}

    public NewHeadingInference(float[][] startingMatrix) {
        c = startingMatrix.clone();
    }

    public void setBias(float xBias, float yBias, float zBias) {
        this.xBias = xBias;
        this.yBias = yBias;
        this.zBias = zBias;
    }

    public void setValues(float axisXVelocity, float axisYVelocity, float axisZVelocity) {
        this.wX = axisXVelocity;
        this.wY = axisYVelocity;
        this.wZ = axisZVelocity;

        calcMatrixB();

        calcOmegaMagnitude();
        calcScaleFactor();
        calcMatrixA();

        calcMatrixC();
    }

    public void calcMatrixB() {
        b = new float[][]{{0, -wZ, wY},
                          {wZ, 0, -wX},
                          {-wY, wX, 0}};
    }

    public void calcMatrixA() {
        identity = new float[][]{{1,0,0},
                          {0,1,0},
                          {0,0,1}};

        a = MathFunctions.multiplyMatrices(b, b);
        a = MathFunctions.scaleMatrix(a, scaleFactor);
        a = MathFunctions.addMatrices(a, b);
        a = MathFunctions.addMatrices(a, identity);
    }

    public void calcOmegaMagnitude() {
        omegaMagnitude = (float) Math.sqrt(Math.pow(wX, 2) + Math.pow(wY, 2) + Math.pow(wZ, 2));
    }

    public void calcScaleFactor() {
        scaleFactor = (float) (1 - Math.cos(omegaMagnitude)) / (float) Math.pow(omegaMagnitude, 2);
    }

    public void calcMatrixC() {
        c = MathFunctions.multiplyMatrices(a, c);
    }

    public float getHeading() {
        return (float) Math.atan2(c[1][2], c[2][2]);
    }

    public double getXPoint(double radius, double angle) {
        angle = angle % Math.PI;
        //return radius * Math.cos(angle);
        return radius * Math.cos(getHeading());
    }

    public double getYPoint(double radius, double angle) {
        angle = angle % Math.PI;
        //return radius * Math.sin(angle);
        return radius * Math.cos(getHeading());
    }

}
