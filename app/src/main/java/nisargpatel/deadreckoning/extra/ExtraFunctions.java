package nisargpatel.deadreckoning.extra;

import android.content.SharedPreferences;

import org.ejml.data.DenseMatrix64F;

import java.util.ArrayList;
import java.util.Collections;

//final class cannot be extended by another class
public final class ExtraFunctions {

    //private constructor stops class from
    //being instantiated to an object
    private ExtraFunctions() {}

    public static final String PREFS_NAME = "Inertial Navigation Preferences";

    public static final float[][] IDENTITY_MATRIX = {{1,0,0},
                                                     {0,1,0},
                                                     {0,0,1}};

    //calculate x coordinate point given radius and angle
    public static float getXFromPolar(double radius, double angle) {
        return (float)(radius * Math.cos(angle));
    }

    //calculate y coordinate point given radius and angle
    public static float getYFromPolar(double radius, double angle) {
        return (float)(radius * Math.sin(angle));
    }

    public static float nsToSec(float time) {
        return time / 1000000000.0f;
    }

    public static int factorial(int num) {
        int factorial = 1;
        for (int i = 1; i <= num; i++) {
            factorial *= i;
        }
        return factorial;
    }

    public static float[][] multiplyMatrices(float[][] a, float[][] b) {

        //numRows = aRows
        int numRows = a.length;

        //numCols = bCols
        int numCols = b[0].length;

        //numElements = (aCols == bRows)
        int numElements = b.length;

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

//    public static float[] multiplyMatrices(double[][] a, double[] b) {
//
//        //numRows = aRows
//        int numRows = a.length;
//
//        //numCols = aCols
//        int numCols = a[0].length;
//
//        //numElements = (aCols == bRows)
//        //int numElements = b.length;
//
//        float[] c = new float[numRows];
//
//        for (int row = 0; row < numRows; row++) {
//            for (int col = 0; col < numCols; col++) {
//                c[row] += a[row][col] * b[col];
//            }
//        }
//
//        //a[][] * b[] = c[]
//        return c;
//
//    }

    public static float[][] addMatrices(float[][] a, float[][] b) {

        int numRows = a.length;
        int numColumns = a[0].length;

        float[][] c = new float[numRows][numColumns];

        for (int row = 0; row < numRows; row++)
            for (int column = 0; column < numColumns; column++)
                c[row][column] = a[row][column] + b[row][column];

        //a[][] + b[][] = c[][]
        return c;
    }

    public static float[][] scaleMatrix(float a[][], float scalar) {

        int numRows = a.length;
        int numColumns = a[0].length;

        float[][] b = new float[numRows][numColumns];

        for (int row = 0; row < numRows; row++)
            for (int column = 0; column < numColumns; column++)
                b[row][column] = a[row][column] * scalar;

        //a[][] * c = b[][]
        return b;

    }

    public static void addArrayToSharedPreferences(String arrayName, ArrayList<String> array, SharedPreferences.Editor editor) {
        editor.putInt(arrayName + "_size", array.size());
        for (int i = 0; i < array.size(); i++) {
            editor.putString(arrayName + "_" + i, array.get(i));
        }
        editor.apply();
    }

    public static ArrayList<String> getArrayFromSharedPreferences(String arrayName, SharedPreferences prefs) {

        int arraySize = prefs.getInt(arrayName + "_size", 0);

        ArrayList<String> newArray = new ArrayList<>();

        for (int i = 0; i < arraySize; i++) {
            newArray.add(prefs.getString(arrayName + "_" + i, null));
        }

        return newArray;

    }

    public static ArrayList<Float> arrayToList(float[] staticArray) {
        ArrayList<Float> dynamicList = new ArrayList<>();
        for (float staticArrayValue : staticArray)
            dynamicList.add(staticArrayValue);
        return dynamicList;
    }

    public static ArrayList<String> arrayToList(String[] staticArray) {
        ArrayList<String> dynamicList = new ArrayList<>();
        Collections.addAll(dynamicList, staticArray);
        return dynamicList;
    }

//    public static String[][] floatArrayToStringArray(float[][] floatArray) {
//        String[][] stringArray = new String[floatArray.length][floatArray[0].length];
//        for (int row = 0; row < floatArray.length; row++)
//            for (int col = 0; col < floatArray[0].length; col++)
//                stringArray[row][col] = String.valueOf(floatArray[row][col]);
//        return stringArray;
//    }

//    public static float[][] stringArrayToFloatArray(String[][] stringArray) {
//        float[][] floatArray = new float[stringArray.length][stringArray[0].length];
//        for (int row = 0; row < stringArray.length; row++)
//            for (int col = 0; col < stringArray[0].length; col++)
//                floatArray[row][col] = Float.parseFloat(stringArray[row][col]);
//        return floatArray;
//    }

    public static float[][] denseMatrixToArray(DenseMatrix64F matrix) {
        float array[][] = new float[matrix.getNumRows()][matrix.getNumCols()];
        for (int row = 0; row < matrix.getNumRows(); row++)
            for (int col = 0; col < matrix.getNumCols(); col++)
                array[row][col] = (float) matrix.get(row,col);
        return array;
    }

    public static double[][] vectorToMatrix(double[] array) {
        return new double[][]{{array[0]},{array[1]},{array[2]}};
    }

    public static ArrayList<Float> createList(float... args) {
        ArrayList<Float> list = new ArrayList<>();
        for (float arg : args)
            list.add(arg);
        return list;
    }

//    public static float polarShiftMinusHalfPI(double heading) {
//        if (heading < -Math.PI / 2.0 && heading > -Math.PI)
//            heading += 3 * Math.PI / 2.0;
//        else
//            heading -= Math.PI / 2.0;
//        return (float)heading;
//    }

    public static float radsToDegrees(double rads) {
        double degrees = (rads < 0) ? (2.0 * Math.PI + rads) : rads;
        degrees *= (180.0 / Math.PI);
        return (float)degrees;
    }

    public static float polarAdd(double initHeading, double deltaHeading) {

        double currHeading = initHeading + deltaHeading;

        //convert 0 < h < 2pi or -2pi < h < 0 to -pi/2 < h < pi/2
        if(currHeading < -Math.PI)
            return (float)((currHeading % Math.PI) + Math.PI);
        else if (currHeading > Math.PI)
            return (float)((currHeading % Math.PI) + -Math.PI);
        else
            return (float)currHeading;

    }

    public static float calcCompHeading(double magHeading, double gyroHeading) {
        //complimentary filter

        //convert -pi/2 < h < pi/2 to 0 < h < 2pi
        if (magHeading < 0)
            magHeading = magHeading % (2.0 * Math.PI);
        if (gyroHeading < 0)
            gyroHeading = gyroHeading % (2.0 * Math.PI);

        double compHeading = 0.02 * magHeading + 0.98 * gyroHeading;

        //convert 0 < h < 2pi to -pi/2 < h < pi/2
        if (compHeading > Math.PI)
            compHeading = (compHeading % Math.PI) + -Math.PI;

        return (float)compHeading;

    }

    public static float calcNorm(double... args) {
        double sumSq = 0;
        for (double arg : args)
            sumSq += Math.pow(arg, 2);
        return (float)Math.sqrt(sumSq);
    }

    public static double[] floatVectorToDoubleVector(float[] floatValues) {
        double[] doubleValues = new double[floatValues.length];
        for (int i = 0; i < floatValues.length; i++)
            doubleValues[i] = floatValues[i];
        return doubleValues;
    }

//    public static double[] matrixToVector(double[][] matrix) {
//        double[] vector = new double[matrix.length];
//        for (int i = 0; i < 3; i++)
//            vector[i] = matrix[i][0];
//        return vector;
//    }

}
