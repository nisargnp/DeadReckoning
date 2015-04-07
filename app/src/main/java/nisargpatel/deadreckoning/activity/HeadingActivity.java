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
import nisargpatel.deadreckoning.orientation.GyroIntegration;
import nisargpatel.deadreckoning.orientation.GyroscopeEulerOrientation;
import nisargpatel.deadreckoning.orientation.MagneticFieldOrientation;

public class HeadingActivity extends Activity implements SensorEventListener{

    private static final float EULER_GYROSCOPE_SENSITIVITY = 0.0025f;
    private static final float GYROSCOPE_SENSITIVITY = 0f;

    private GyroscopeBias gyroUBias;
    private GyroscopeEulerOrientation gyroUOrientation;
    private GyroIntegration gyroCIntegration;
    private GyroIntegration gyroUIntegration;

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

    private double eulerHeading;
    private double magHeading;

    private float[] gravityValues;
    private float[] magValues;

    private boolean isRunning;

    private double initialHeading;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heading);

        gyroUBias = new GyroscopeBias(300);
        gyroCIntegration = new GyroIntegration(GYROSCOPE_SENSITIVITY, new float[3]);
        gyroUIntegration = new GyroIntegration(EULER_GYROSCOPE_SENSITIVITY, null);

        gyroHeading = 0;
        gyroHeading = 0;

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

                float[][] initialOrientation = MagneticFieldOrientation.calcOrientation(gravityValues, magValues, new float[3]);

                gyroUOrientation = new GyroscopeEulerOrientation(initialOrientation);
                //gyroUOrientation = new GyroscopeEulerOrientation(ExtraFunctions.IDENTITY_MATRIX);

                initialHeading = ExtraFunctions.polarShiftMinusHalfPI(Math.atan2(initialOrientation[1][0], initialOrientation[0][0]));
                initialHeading = -initialHeading;

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

                gyroUOrientation.clearMatrix();

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

        if (event.sensor.getType() == Sensor.TYPE_GRAVITY)
            gravityValues = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            magValues = event.values;

        if (isRunning) {

            if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {

                float[] deltaOrientation = gyroCIntegration.getIntegratedValues(event.timestamp, event.values);
                gyroHeading += deltaOrientation[2];
                double gyroHeadingDegrees = ExtraFunctions.radsToDegrees(gyroHeading);
                textGyroC.setText(String.valueOf(gyroHeadingDegrees));

            } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE_UNCALIBRATED) {

                if (gyroUBias.calcBias(event.values)) {

                    gyroUIntegration.setBias(gyroUBias.getBias());

                    float[] deltaOrientation = gyroUIntegration.getIntegratedValues(event.timestamp, event.values);

                    //rotation about z
                    gyroHeadingU += deltaOrientation[2];
                    double gyroHeadingUDegrees = ExtraFunctions.radsToDegrees(gyroHeadingU);
                    textGyroU.setText(String.valueOf(gyroHeadingUDegrees));

                    //direction cosine matrix
                    eulerHeading = gyroUOrientation.getCurrentHeading(deltaOrientation);
                    //eulerHeading = ExtraFunctions.polarAdd(initialHeading, eulerHeading);

                    textDirectionCosine.setText(String.valueOf(eulerHeading));

                }

            } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {

                float[][] rotationMatrix = MagneticFieldOrientation.calcOrientation(gravityValues, magValues, new float[3]);

                magHeading = Math.atan2(rotationMatrix[1][0], rotationMatrix[0][0]);

                magHeading = ExtraFunctions.polarShiftMinusHalfPI(magHeading);
                magHeading = -magHeading;
                textMagneticField.setText(String.valueOf(String.valueOf(magHeading)));

            }

            double compHeading = ExtraFunctions.calcCompHeading(magHeading, eulerHeading);
            textComplimentaryFilter.setText(String.valueOf(compHeading));

        }

    }

}
