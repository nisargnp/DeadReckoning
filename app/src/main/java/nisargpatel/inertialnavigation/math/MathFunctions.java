package nisargpatel.inertialnavigation.math;

//final class cannot be extended by another class
public final class MathFunctions {

    //private constructor stops class from
    //being instantiated to an object
    private MathFunctions() {};

    //calculate x coordinate point given radius and angle
    public static double getXFromPolar(double radius, double angle) {
        return radius * Math.cos(Math.toRadians(angle));
    }

    //calculate y coordinate point given radius and angle
    public static double getYFromPolar(double radius, double angle) {
        return radius * Math.sin(Math.toRadians(angle));
    }

    public static float nsToSec(float time) {
        return time / 1000000000.0f;
    }

    public static float[][] multiplyMatrices(float[][] a, float[][] b) {

        //numRows = aRows
        int numRows = a[0].length;

        //numCols = bCols
        int numCols = b.length;

        //numElements = aCols == bRows
        int numElements = a.length;

        float[][] c = new float[numRows][numCols];

        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                for (int element = 0; element < numElements; element++)
                    c[row][col] += a[row][element] * b[element][col];
            }
        }

        //a[][] * b[][] = c[][]
        return c;

    }

    public static float[][] addMatrices(float[][] a, float[][] b) {

        int numRows = a[0].length;
        int numColumns = a.length;

        float[][] c = new float[numRows][numColumns];

        for (int row = 0; row < numRows; row++)
            for (int column = 0; column < numColumns; column++)
                c[row][column] = a[row][column] + b[row][column];

        //a[][] + b[][] = c[][]
        return c;
    }

    public static float[][] scaleMatrix(float a[][], float scalar) {

        int numRows = a[0].length;
        int numColumns = a.length;

        for (int row = 0; row < numRows; row++)
            for (int column = 0; column < numColumns; column++)
                a[row][column] = a[row][column] * scalar;

        //a[][] * c = a[][]
        return a;

    }

}
