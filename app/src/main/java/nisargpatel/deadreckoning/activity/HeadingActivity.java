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
import nisargpatel.deadreckoning.heading.EulerHeadingInference;
import nisargpatel.deadreckoning.heading.GyroscopeBias;
import nisargpatel.deadreckoning.heading.GyroscopeIntegration;
import nisargpatel.deadreckoning.heading.InitialOrientation;

public class HeadingActivity extends Activity implements SensorEventListener{

    private static final float EULER_GYROSCOPE_SENSITIVITY = 0.0025f;
    private static final float GYROSCOPE_SENSITIVITY = 0f;

    private GyroscopeBias gyroBias;
    private EulerHeadingInference eulerHeadingInference;
    private GyroscopeIntegration gyroscopeIntegration;
    private GyroscopeIntegration gyroscopeUncalibratedIntegration;

    private Sensor gyroscopeCalibrated;
    private Sensor gyroscopeUncalibrated;
    private Sensor rotationVector;
    private Sensor magneticField;
    private Sensor gravity;
    private SensorManager sensorManager;

    private TextView textDirectionCosine;
    private TextView textGyroscopeU;
    private TextView textGyroscope;
    private TextView textMagneticField;
    private TextView textRotationVector;

    private Button buttonStart;
    private Button buttonStop;

    private double gyroHeading;
    private double gyroHeadingU;

    float[] gravityData;

    private boolean wasRunning;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heading);

        gyroBias = new GyroscopeBias(300);
        eulerHeadingInference = new EulerHeadingInference();
        gyroscopeIntegration = new GyroscopeIntegration(GYROSCOPE_SENSITIVITY, new float[3]);
        gyroscopeUncalibratedIntegration = new GyroscopeIntegration(EULER_GYROSCOPE_SENSITIVITY, null);

        gyroHeading = 0;
        gyroHeading = 0;

        textDirectionCosine = (TextView) findViewById(R.id.textDirectionCosine);
        textGyroscopeU = (TextView) findViewById(R.id.textGyroscopeU);
        textGyroscope = (TextView) findViewById(R.id.textGyroscope);
        textMagneticField = (TextView) findViewById(R.id.textMagneticField);
        textRotationVector = (TextView) findViewById(R.id.textRotationVector);

        buttonStart = (Button) findViewById(R.id.buttonStart);
        buttonStop = (Button) findViewById(R.id.buttonStop);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        gyroscopeUncalibrated = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED);
        gyroscopeCalibrated = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        gravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorManager.registerListener(HeadingActivity.this, gyroscopeUncalibrated, SensorManager.SENSOR_DELAY_FASTEST);
                sensorManager.registerListener(HeadingActivity.this, gyroscopeCalibrated, SensorManager.SENSOR_DELAY_FASTEST);
                sensorManager.registerListener(HeadingActivity.this, rotationVector, SensorManager.SENSOR_DELAY_FASTEST);
                sensorManager.registerListener(HeadingActivity.this, magneticField, SensorManager.SENSOR_DELAY_FASTEST);
                sensorManager.registerListener(HeadingActivity.this, gravity, SensorManager.SENSOR_DELAY_FASTEST);

                buttonStart.setEnabled(false);
                buttonStop.setEnabled(true);

                wasRunning = true;
            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorManager.unregisterListener(HeadingActivity.this);

                buttonStart.setEnabled(true);
                buttonStop.setEnabled(false);

                wasRunning = false;
            }
        });

        findViewById(R.id.buttonClear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textDirectionCosine.setText("0");
                textGyroscopeU.setText("0");
                textGyroscope.setText("0");
                textMagneticField.setText("0");
                textRotationVector.setText("0");

                eulerHeadingInference.clearMatrix();

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
        if (wasRunning) {
            sensorManager.registerListener(HeadingActivity.this, gyroscopeUncalibrated, SensorManager.SENSOR_DELAY_FASTEST);
            sensorManager.registerListener(HeadingActivity.this, gyroscopeCalibrated, SensorManager.SENSOR_DELAY_FASTEST);
            sensorManager.registerListener(HeadingActivity.this, rotationVector, SensorManager.SENSOR_DELAY_FASTEST);
            sensorManager.registerListener(HeadingActivity.this, magneticField, SensorManager.SENSOR_DELAY_FASTEST);
            sensorManager.registerListener(HeadingActivity.this, gravity, SensorManager.SENSOR_DELAY_FASTEST);

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

        int sensorType = event.sensor.getType();

        if (sensorType == Sensor.TYPE_GYROSCOPE_UNCALIBRATED) {

            if (gyroBias.calcBias(event.values)) {
                gyroscopeUncalibratedIntegration.setBias(gyroBias.getBias());

                float[] deltaOrientation = gyroscopeUncalibratedIntegration.getIntegratedValues(event.timestamp, event.values);

                gyroHeadingU += deltaOrientation[2];
                textGyroscopeU.setText(String.valueOf(gyroHeadingU));

                float eulerHeading = eulerHeadingInference.getCurrentHeading(deltaOrientation);
                textDirectionCosine.setText(String.valueOf(eulerHeading));
            }

        } else if (sensorType == Sensor.TYPE_GYROSCOPE) {

            float[] deltaOrientation = gyroscopeIntegration.getIntegratedValues(event.timestamp, event.values);
            gyroHeading += deltaOrientation[2];
            textGyroscope.setText(String.valueOf(gyroHeading));

        } else if (sensorType == Sensor.TYPE_ROTATION_VECTOR) {
            textRotationVector.setText(String.valueOf(event.values[2]));
        } else if (sensorType == Sensor.TYPE_MAGNETIC_FIELD) {
            if (gravityData != null) {
                float[] magData = {event.values[0], event.values[1], event.values[2]};
//                double heading = InitialOrientation.calcHeading(gravityData, magData, new float[3]);

                float[][] rotationMatrix = InitialOrientation.calcOrientation(gravityData, magData, new float[3]);
                double heading = Math.atan2(rotationMatrix[2][0], rotationMatrix[2][1]);

                textMagneticField.setText(String.valueOf(String.valueOf(heading)));
            }
        } else if (sensorType == Sensor.TYPE_GRAVITY) {
            gravityData = event.values.clone();
        }
    }

}
