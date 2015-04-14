package nisargpatel.deadreckoning.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import nisargpatel.deadreckoning.R;
import nisargpatel.deadreckoning.bias.GyroscopeBias;
import nisargpatel.deadreckoning.extra.ExtraFunctions;
import nisargpatel.deadreckoning.orientation.GyroscopeDeltaOrientation;
import nisargpatel.deadreckoning.orientation.GyroscopeEulerOrientation;
import nisargpatel.deadreckoning.orientation.MagneticFieldOrientation;

public class HeadingActivity extends Activity implements SensorEventListener{

    private static final float EULER_GYROSCOPE_SENSITIVITY = 0.0025f;
    private static final float GYROSCOPE_SENSITIVITY = 0f;

    private GyroscopeBias gyroUBias;

    //todo: remove one of these after debugging
    private GyroscopeEulerOrientation gyroUOrientation1;
    private GyroscopeEulerOrientation gyroUOrientation2;

    private GyroscopeDeltaOrientation gyroCIntegration;
    private GyroscopeDeltaOrientation gyroUIntegration;

    private Sensor sensorGyroC; //gyroscope Android-calibrated
    private Sensor sensorGyroU; //gyroscope uncalibrated (manually calibrated)
    private Sensor sensorMagU; //magnetic field uncalibrated
    private Sensor sensorGravity;
    private SensorManager sensorManager;

    private TextView textDirectionCosine;
    private TextView textGyroU;
    private TextView textGyroC;
    private TextView textMagneticField;
    private TextView textComplimentaryFilter;

    private Button buttonStart;
    private Button buttonStop;

    private double gyroHeading;
    private double gyroHeadingU;

    //todo: remove two of these after debugging
    private double eulerHeading1;
    private double eulerHeading2;
    private double eulerHeading3;

    private double magHeading;

    private float[] gravityValues;
    private float[] magValues;

    private boolean isRunning;

    private double initialHeading;

    int gravityCount;
    int magCount;
    float[] sumGravityValues;
    float[] sumMagValues;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heading);

        gravityCount = magCount = 0;
        sumGravityValues = new float[3];
        sumMagValues = new float[3];

        gyroUBias = new GyroscopeBias(300);
        gyroCIntegration = new GyroscopeDeltaOrientation(GYROSCOPE_SENSITIVITY, new float[3]);
        gyroUIntegration = new GyroscopeDeltaOrientation(EULER_GYROSCOPE_SENSITIVITY, null);

        gyroHeading = 0;
        gyroHeadingU = 0;
        eulerHeading1 = 0;
        eulerHeading2 = 0;
        eulerHeading3 = 0;
        magHeading = 0;

        textDirectionCosine = (TextView) findViewById(R.id.textDirectionCosine);
        textGyroU = (TextView) findViewById(R.id.textGyroscopeU);
        textGyroC = (TextView) findViewById(R.id.textGyroscope);
        textMagneticField = (TextView) findViewById(R.id.textMagneticField);
        textComplimentaryFilter = (TextView) findViewById(R.id.textRotationVector);

        buttonStart = (Button) findViewById(R.id.buttonStart);
        buttonStop = (Button) findViewById(R.id.buttonStop);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        sensorGyroU = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED);
        sensorGyroC = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorMagU = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorGravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

        sensorManager.registerListener(HeadingActivity.this, sensorGyroU, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(HeadingActivity.this, sensorGyroC, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(HeadingActivity.this, sensorMagU, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(HeadingActivity.this, sensorGravity, SensorManager.SENSOR_DELAY_FASTEST);

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                for (int i = 0; i < 3; i++) {
//                    sumGravityValues[i] = sumGravityValues[i] / gravityCount;
//                    sumMagValues[i] = sumMagValues[i] / magCount;
//                }
//
//                Log.d("initial", Arrays.toString(sumGravityValues));
//                Log.d("initial", Arrays.toString(sumMagValues));
//
//                initialOrientation = MagneticFieldOrientation.getOrientationMatrix(sumGravityValues, sumMagValues, new float[3]);

                float[][] initialOrientation = MagneticFieldOrientation.getOrientationMatrix(gravityValues, magValues, new float[3]);

                initialHeading = MagneticFieldOrientation.getHeading(gravityValues, magValues, new float[3]);

                gyroUOrientation1 = new GyroscopeEulerOrientation(ExtraFunctions.IDENTITY_MATRIX);
                gyroUOrientation2 = new GyroscopeEulerOrientation(initialOrientation);

                buttonStart.setEnabled(false);
                buttonStop.setEnabled(true);

                isRunning = true;
            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonStart.setEnabled(true);
                buttonStop.setEnabled(false);

                isRunning = false;
            }
        });

        findViewById(R.id.buttonClear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textDirectionCosine.setText("0");
                textGyroU.setText("0");
                textGyroC.setText("0");
                textMagneticField.setText("0");
                textComplimentaryFilter.setText("0");

                //gyroUOrientation1.clearMatrix();

                gyroHeading = 0;
                gyroHeadingU = 0;
            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isRunning) {
            sensorManager.registerListener(HeadingActivity.this, sensorGyroU, SensorManager.SENSOR_DELAY_FASTEST);
            sensorManager.registerListener(HeadingActivity.this, sensorGyroC, SensorManager.SENSOR_DELAY_FASTEST);
            sensorManager.registerListener(HeadingActivity.this, sensorMagU, SensorManager.SENSOR_DELAY_FASTEST);
            sensorManager.registerListener(HeadingActivity.this, sensorGravity, SensorManager.SENSOR_DELAY_FASTEST);

            buttonStart.setEnabled(false);
            buttonStop.setEnabled(true);
        } else {
            buttonStart.setEnabled(true);
            buttonStop.setEnabled(false);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}



    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
            gravityValues = event.values;

            gravityCount++;
            for (int i = 0; i < 3; i++)
                sumGravityValues[i] += gravityValues[i];

        }

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            magValues = event.values;

            magCount++;
            for (int i = 0; i < 3; i++)
                sumMagValues[i] += magValues[i];

        }

        if (isRunning) {

            if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {

                float[] deltaOrientation = gyroCIntegration.calcDeltaOrientation(event.timestamp, event.values);
                gyroHeading += deltaOrientation[2];
                double gyroHeadingDegrees = ExtraFunctions.radsToDegrees(gyroHeading);
//                textGyroC.setText(String.valueOf(gyroHeadingDegrees));

            } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE_UNCALIBRATED) {

                //if biases are calculated
                if (gyroUBias.calcBias(event.values)) {

                    //set the biases
                    gyroUIntegration.setBias(gyroUBias.getBias());

                    float[] deltaOrientation = gyroUIntegration.calcDeltaOrientation(event.timestamp, event.values);

                    //rotation about z
                    gyroHeadingU += deltaOrientation[2];
                    double gyroHeadingUDegrees = ExtraFunctions.radsToDegrees(gyroHeadingU);
//                    textGyroU.setText(String.valueOf(gyroHeadingUDegrees));

                    //direction cosine matrix
                    eulerHeading1 = gyroUOrientation1.getHeading(deltaOrientation); //identity
                    eulerHeading2 = gyroUOrientation2.getHeading(deltaOrientation); //initial orientation
                    eulerHeading3 = ExtraFunctions.polarAdd(initialHeading, eulerHeading1); //initial heading + identity heading

                    textDirectionCosine.setText(String.valueOf(eulerHeading3));

                    ((TextView)findViewById(R.id.textView)).setText("Non-Initialized DCM");
                    textGyroU.setText(String.valueOf(eulerHeading1));

                    ((TextView)findViewById(R.id.textView6)).setText("Initialized DCM");
                    textGyroC.setText(String.valueOf(eulerHeading2));

                }

            } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {

                magHeading = MagneticFieldOrientation.getHeading(gravityValues, magValues, new float[3]);
                textMagneticField.setText(String.valueOf(magHeading));

            }

            double compHeading = ExtraFunctions.calcCompHeading(magHeading, eulerHeading3);
            textComplimentaryFilter.setText(String.valueOf(compHeading));

        }

    }

}
