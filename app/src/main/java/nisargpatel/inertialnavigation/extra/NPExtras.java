package nisargpatel.inertialnavigation.extra;

import android.content.SharedPreferences;

import java.util.ArrayList;

//final class cannot be extended by another class
public final class NPExtras {

    //private constructor stops class from
    //being instantiated to an object
    private NPExtras() {}

    public static final String PREFS_NAME = "Inertial Navigation Preferences";

    //calculate x coordinate point given radius and angle
    public static double getXFromPolar(double radius, double angle) {
        return radius * Math.cos(angle);
    }

    //calculate y coordinate point given radius and angle
    public static double getYFromPolar(double radius, double angle) {
        return radius * Math.sin(angle);
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

    public static float[][] getIdentityMatrix() {
        return new float[][]{{1,0,0},
                             {0,1,0},
                             {0,0,1}};
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

}

