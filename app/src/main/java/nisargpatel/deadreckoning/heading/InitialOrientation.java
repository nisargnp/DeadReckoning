package nisargpatel.deadreckoning.heading;

import android.util.Log;

import org.ejml.data.DenseMatrix64F;
import org.ejml.simple.SimpleMatrix;

public final class InitialOrientation {

    private InitialOrientation() {}

    public static float[][] calcOrientation(float[] G_init, float[] M_init, float[] M_bias) {

        Log.d("calibrate", "first M_init length = " + M_init.length);

        //G = Gyroscope, M = Magnetic Field
        //m = matrix
        //where r = roll, p = pitch, h = heading (yaw)

        //calculate roll and pitch from gravity
        double G_r = Math.atan2(G_init[1], G_init[2]);
        double G_p = Math.atan2(-G_init[0], Math.sin(G_r) + Math.cos(G_r));

        //create the rotation matrix representing the roll and pitch
        double[][] R_rp = {{Math.cos(G_p), Math.sin(G_p) * Math.sin(G_r), Math.sin(G_p) * Math.cos(G_r)},
                            {0, Math.cos(G_r), -Math.sin(G_r)},
                            {-Math.sin(G_p), Math.cos(G_p) * Math.sin(G_r), Math.cos(G_p) * Math.cos(G_r)}};

        //remove bias from magnetic field initial values
        double[][] M_init_unbiased = toMatrix(removeBias(M_init, M_bias));

        //convert arrays to matrices to allow for multiplication
        SimpleMatrix m_R_rp = new SimpleMatrix(R_rp);
        SimpleMatrix m_M_init_unbiased = new SimpleMatrix(M_init_unbiased);

        //rotate magnetic field values in accordance to gravity readings
        SimpleMatrix m_M_rp = m_R_rp.mult(m_M_init_unbiased);

        //calc heading from rotated magnetic field
        double h = Math.atan2(-m_M_rp.get(1), m_M_rp.get(0));

        double[][] R_h = {{Math.cos(h), Math.sin(h), 0},
                          {-Math.sin(h), Math.cos(h), 0},
                          {0, 0, 1}};

        //calc complete (initial) rotation matrix by multiplying roll/pitch matrix with heading matrix
        SimpleMatrix m_R_h = new SimpleMatrix(R_h);
        SimpleMatrix m_R = m_R_rp.mult(m_R_h);

        return matrixToArray(m_R.getMatrix());

    }

    private static double[] removeBias(float[] M_init, float[] M_bias) {
        double[] M_biasRemoved = new double[M_init.length];
        for (int i = 0; i < M_init.length; i++)
            M_biasRemoved[i] = M_init[i] - M_bias[i];
        return M_biasRemoved;
    }

    private static double[][] toMatrix(double[] array) {
        return new double[][]{{array[0]},{array[1]},{array[2]}};
    }

    private static float[][] matrixToArray(DenseMatrix64F matrix) {
        float array[][] = new float[matrix.getNumRows()][matrix.getNumCols()];
        for (int row = 0; row < matrix.getNumRows(); row++)
            for (int col = 0; col < matrix.getNumCols(); col++)
                array[row][col] = (float) matrix.get(row,col);
        return array;
    }

}
