package nisargpatel.locationtrack.activity;

import android.annotation.TargetApi;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import nisargpatel.locationtrack.R;
import nisargpatel.locationtrack.heading.EulerHeadingInference;
import nisargpatel.locationtrack.heading.GyroscopeBias;
import nisargpatel.locationtrack.heading.GyroscopeIntegration;

public class HeadingActivity extends ActionBarActivity implements SensorEventListener{

    private static final float EULER_GYROSCOPE_SENSITIVITY = 0.0025f;
    private static final float GYROSCOPE_SENSITIVITY = 0f;

    private GyroscopeBias gyroBias;
    private EulerHeadingInference eulerHeadingInference;
    private GyroscopeIntegration gyroscopeIntegration;
    private GyroscopeIntegration gyroscopeUncalibratedIntegration;

    private Sensor gyroscopeCalibrated;
    private Sensor gyroscopeUncalibrated;
    private Sensor rotationVector;
    private Sensor geoRotationVector;
    private Sensor gameRotationVector;
    private SensorManager sensorManager;

    private TextView textGyroscopeMatrix;
    private TextView textGyroscope;
    private TextView textRotation;
    private TextView textGeoRotation;
    private TextView textGameRotation;

    private Button buttonStart;
    private Button buttonStop;

    private double gyroHeading;

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

        textGyroscopeMatrix = (TextView) findViewById(R.id.textGyroscopeMatrix);
        textGyroscope = (TextView) findViewById(R.id.textGyroscope);
        textRotation = (TextView) findViewById(R.id.textRotation);
        textGeoRotation = (TextView) findViewById(R.id.textGeoRotation);
        textGameRotation = (TextView) findViewById(R.id.textGameRotation);

        buttonStart = (Button) findViewById(R.id.buttonStart);
        buttonStop = (Button) findViewById(R.id.buttonStop);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        gyroscopeUncalibrated = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED);
        gyroscopeCalibrated = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        geoRotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR);
        gameRotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorManager.registerListener(HeadingActivity.this, gyroscopeUncalibrated, SensorManager.SENSOR_DELAY_FASTEST);
                sensorManager.registerListener(HeadingActivity.this, gyroscopeCalibrated, SensorManager.SENSOR_DELAY_FASTEST);
                sensorManager.registerListener(HeadingActivity.this, rotationVector, SensorManager.SENSOR_DELAY_FASTEST);
                sensorManager.registerListener(HeadingActivity.this, geoRotationVector, SensorManager.SENSOR_DELAY_FASTEST);
                sensorManager.registerListener(HeadingActivity.this, gameRotationVector, SensorManager.SENSOR_DELAY_FASTEST);

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
                textGyroscopeMatrix.setText("0");
                textGyroscope.setText("0");
                textRotation.setText("0");
                textGeoRotation.setText("0");
                textGameRotation.setText("0");

                eulerHeadingInference.clearMatrix();

                gyroHeading = 0;
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
            sensorManager.registerListener(HeadingActivity.this, geoRotationVector, SensorManager.SENSOR_DELAY_FASTEST);
            sensorManager.registerListener(HeadingActivity.this, gameRotationVector, SensorManager.SENSOR_DELAY_FASTEST);

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
                float eulerHeading = eulerHeadingInference.getCurrentHeading(deltaOrientation);
                textGyroscopeMatrix.setText(String.valueOf(eulerHeading));
            }

        } else if (sensorType == Sensor.TYPE_GYROSCOPE) {

            float[] deltaOrientation = gyroscopeIntegration.getIntegratedValues(event.timestamp, event.values);
            gyroHeading += deltaOrientation[2];
            textGyroscope.setText(String.valueOf(gyroHeading));

        } else if (sensorType == Sensor.TYPE_ROTATION_VECTOR) {
            textRotation.setText(String.valueOf(event.values[2]));
        } else if (sensorType == Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR) {
            textGeoRotation.setText(String.valueOf(event.values[2]));
        } else if (sensorType == Sensor.TYPE_GAME_ROTATION_VECTOR) {
            textGameRotation.setText(String.valueOf(event.values[2]));
        }
    }



}
