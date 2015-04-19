package nisargpatel.deadreckoning.orientation;

import org.ejml.simple.SimpleMatrix;

import nisargpatel.deadreckoning.extra.ExtraFunctions;

public final class MagneticFieldOrientation {

    private MagneticFieldOrientation() {}

    private static SimpleMatrix m_rotationNED = new SimpleMatrix(new double[][]{{0,1,0},
                                                                                {1,0,0},
                                                                                {0,0,-1}});

    public static float[][] getOrientationMatrix(float[] G_values, float[] M_values, float[] M_bias) {

        //G = Gyroscope, M = Magnetic Field
        //m = matrix
        //where r = roll, p = pitch, h = heading (yaw)

//        M_values = ExtraFunctions.multiplyMatrices(rotationNED, M_values);
//        M_bias = ExtraFunctions.multiplyMatrices(rotationNED, M_bias);
//        G_values = ExtraFunctions.multiplyMatrices(rotationNED, G_values);

        //remove bias from magnetic field initial values
        double[][] M_init_unbiased = ExtraFunctions.vectorToMatrix(removeBias(M_values, M_bias));

        SimpleMatrix m_M_init_unbiased = new SimpleMatrix(M_init_unbiased);
//        SimpleMatrix m_M_values = new SimpleMatrix(ExtraFunctions.vectorToMatrix(ExtraFunctions.floatVectorToDoubleVector(M_values)));
//        SimpleMatrix m_M_bias   = new SimpleMatrix(ExtraFunctions.vectorToMatrix(ExtraFunctions.floatVectorToDoubleVector(M_bias)));
        SimpleMatrix m_G_values = new SimpleMatrix(ExtraFunctions.vectorToMatrix(ExtraFunctions.floatVectorToDoubleVector(G_values)));

        m_M_init_unbiased =  m_rotationNED.mult(m_M_init_unbiased);
//        m_M_values =  m_rotationNED.mult(m_M_values);
//        m_M_bias   =  m_rotationNED.mult(m_M_bias);
        m_G_values =  m_rotationNED.mult(m_G_values);

//        float[][] M_m_values = ExtraFunctions.denseMatrixToArray(m_M_values.getMatrix());
//        float[][] M_m_bias = ExtraFunctions.denseMatrixToArray(m_M_bias.getMatrix());
        float[][] G_m_values = ExtraFunctions.denseMatrixToArray(m_G_values.getMatrix());

        //calculate roll and pitch from gravity
        double G_r = Math.atan2(G_m_values[1][0], G_m_values[2][0]);
        double G_p = Math.atan2(-G_m_values[0][0], G_m_values[1][0] * Math.sin(G_r) + G_m_values[2][0] * Math.cos(G_r));

        //create the rotation matrix representing the roll and pitch
        double[][] R_rp = {{Math.cos(G_p), Math.sin(G_p) * Math.sin(G_r), Math.sin(G_p) * Math.cos(G_r)},
                            {0, Math.cos(G_r), -Math.sin(G_r)},
                            {-Math.sin(G_p), Math.cos(G_p) * Math.sin(G_r), Math.cos(G_p) * Math.cos(G_r)}};

//        //remove bias from magnetic field initial values
//        double[][] M_init_unbiased = ExtraFunctions.vectorToMatrix(removeBias(M_m_values, M_m_bias));

        //convert arrays to matrices to allow for multiplication
        SimpleMatrix m_R_rp = new SimpleMatrix(R_rp);
//        SimpleMatrix m_M_init_unbiased = new SimpleMatrix(M_init_unbiased);

        //rotate magnetic field values in accordance to gravity readings
        SimpleMatrix m_M_rp = m_R_rp.mult(m_M_init_unbiased);

        //calc heading (rads) from rotated magnetic field
        double h = -1*(Math.atan2(-m_M_rp.get(1), m_M_rp.get(0)) + 11.0 * Math.PI/180.0);

        //rotation matrix representing heading, is negative when moving East of North
        double[][] R_h = {{Math.cos(h), -Math.sin(h), 0},
                          {Math.sin(h),  Math.cos(h), 0},
                          {0,            0,           1}};

        //calc complete (initial) rotation matrix by multiplying roll/pitch matrix with heading matrix
        SimpleMatrix m_R_h = new SimpleMatrix(R_h);
        SimpleMatrix m_R = m_R_rp.mult(m_R_h);

        //rotate heading by minus pi/2, so that heading == 0 corresponds with North
//        double[][] R_yx = {{0.0, -1.0, 0.0},
//                           {1.0,  0.0, 0.0},
//                           {0.0,  0.0, 0.0}};
//        SimpleMatrix m_R_yx = new SimpleMatrix(R_yx);
//        m_R = m_R_yx.mult(m_R);

        return ExtraFunctions.denseMatrixToArray(m_R.getMatrix());

    }

    public static float getHeading(float[] G_values, float[] M_values, float[] M_bias) {
        float[][] orientationMatrix = getOrientationMatrix(G_values, M_values, M_bias);
        return (float) Math.atan2(orientationMatrix[1][0], orientationMatrix[0][0]);
    }

//    private static double[] removeBias(float[][] M_init, float[][] M_bias) {
//        //ignoring the last 3 values of M_init, which are the android-calculated iron biases
//        double[] M_biasRemoved = new double[3];
//        for (int i = 0; i < 3; i++)
//            M_biasRemoved[i] = M_init[i][0] - M_bias[i][0];
//        return M_biasRemoved;
//    }

    private static double[] removeBias(float[] M_init, float[] M_bias) {
        //ignoring the last 3 values of M_init, which are the android-calculated iron biases
        double[] M_biasRemoved = new double[3];
        for (int i = 0; i < 3; i++)
            M_biasRemoved[i] = M_init[i] - M_bias[i];
        return M_biasRemoved;
    }

}
