package nisargpatel.inertialnavigation.activity;

import android.annotation.TargetApi;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import nisargpatel.inertialnavigation.R;
import nisargpatel.inertialnavigation.heading.MatrixHeadingInference;
import nisargpatel.inertialnavigation.math.MathFunctions;

public class OrientationTestActivity extends ActionBarActivity implements SensorEventListener{

    private MatrixHeadingInference matrixHeadingInference;

    private Sensor gyroscopeCalibrated;
    private Sensor gyroscopeUncalibrated;
    private Sensor rotationVector;
    private Sensor geoRotationVector;
    private Sensor gameRotationVector;
    private SensorManager sm;

    private TextView textGyroscopeMatrix;
    private TextView textGyroscope;
    private TextView textRotation;
    private TextView textGeoRotation;
    private TextView textGameRotation;

    private double lastTimestampGyro;
    private double lastTimestampGyroU;

    private double gyroHeading;

    int runCountGyro;
    int runCountGyroU;

    float[] biasGyroU = new float[3];

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orientation_test);

        matrixHeadingInference = new MatrixHeadingInference();

        gyroHeading = 0;
        runCountGyro = 0;

        textGyroscopeMatrix = (TextView) findViewById(R.id.textGyroscopeMatrix);
        textGyroscope = (TextView) findViewById(R.id.textGyroscope);
        textRotation = (TextView) findViewById(R.id.textRotation);
        textGeoRotation = (TextView) findViewById(R.id.textGeoRotation);
        textGameRotation = (TextView) findViewById(R.id.textGameRotation);

        Button buttonStart = (Button) findViewById(R.id.buttonStart);
        Button buttonStop = (Button) findViewById(R.id.buttonStop);

        sm = (SensorManager) getSystemService(SENSOR_SERVICE);

        gyroscopeUncalibrated = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED);
        gyroscopeCalibrated = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        rotationVector = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        geoRotationVector = sm.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR);
        gameRotationVector = sm.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sm.registerListener(OrientationTestActivity.this, gyroscopeUncalibrated, SensorManager.SENSOR_DELAY_FASTEST);
                sm.registerListener(OrientationTestActivity.this, gyroscopeCalibrated, SensorManager.SENSOR_DELAY_FASTEST);
                sm.registerListener(OrientationTestActivity.this, rotationVector, SensorManager.SENSOR_DELAY_FASTEST);
                sm.registerListener(OrientationTestActivity.this, geoRotationVector, SensorManager.SENSOR_DELAY_FASTEST);
                sm.registerListener(OrientationTestActivity.this, gameRotationVector, SensorManager.SENSOR_DELAY_FASTEST);
                runCountGyro = runCountGyroU = 0;
            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sm.unregisterListener(OrientationTestActivity.this, gyroscopeUncalibrated);
                sm.unregisterListener(OrientationTestActivity.this, gyroscopeCalibrated);
                sm.unregisterListener(OrientationTestActivity.this, rotationVector);
                sm.unregisterListener(OrientationTestActivity.this, geoRotationVector);
                sm.unregisterListener(OrientationTestActivity.this, gameRotationVector);
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

                matrixHeadingInference.clearMatrix();

                gyroHeading = 0;
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_orientation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onSensorChanged(SensorEvent event) {

        int sensorType = event.sensor.getType();

        if (sensorType == Sensor.TYPE_GYROSCOPE_UNCALIBRATED) {

            runCountGyroU++;

            //setting the initial timestamp on the first run
            if (runCountGyroU == 1) {
                biasGyroU[0] = event.values[0];
                biasGyroU[1] = event.values[1];
                biasGyroU[2] = event.values[2];
                return;
            }

            double currentGyroUTime = MathFunctions.nsToSec(event.timestamp);
            double deltaGyroUTime = currentGyroUTime - lastTimestampGyroU;

            //averaging bias for the first few hundred data points
            if (runCountGyroU <= 300) {
                biasGyroU[0] = (biasGyroU[0] * ((runCountGyroU - 1) / runCountGyroU)) + (event.values[0] / runCountGyroU);
                biasGyroU[1] = (biasGyroU[1] * ((runCountGyroU - 1) / runCountGyroU)) + (event.values[1] / runCountGyroU);
                biasGyroU[2] = (biasGyroU[2] * ((runCountGyroU - 1) / runCountGyroU)) + (event.values[2] / runCountGyroU);
                lastTimestampGyroU = MathFunctions.nsToSec(event.timestamp);
                return;
            }

            float[] deltaOrientationGyroU = new float[3];
            deltaOrientationGyroU[0] = (float) deltaGyroUTime * (event.values[0] - biasGyroU[0]);
            deltaOrientationGyroU[1] = (float) deltaGyroUTime * (event.values[1] - biasGyroU[1]);
            deltaOrientationGyroU[2] = (float) deltaGyroUTime * (event.values[2] - biasGyroU[2]);

            float heading = matrixHeadingInference.getCurrentHeading(deltaOrientationGyroU);
            textGyroscopeMatrix.setText(String.valueOf(heading));

            lastTimestampGyroU = currentGyroUTime;

        } else if (sensorType == Sensor.TYPE_GYROSCOPE) {

            runCountGyro++;

            //on the first run, the timestamp of the first point needs to be set
            //so that delta-t can be calculated for the next point
            if (runCountGyro <= 1) {
                lastTimestampGyro = MathFunctions.nsToSec(event.timestamp);
                return;
            }

            double deltaTimeGyro = MathFunctions.nsToSec(event.timestamp) - lastTimestampGyro;
            double deltaHeading = deltaTimeGyro * event.values[2];

            gyroHeading += deltaHeading;
            textGyroscope.setText(String.valueOf(gyroHeading));

            lastTimestampGyro = MathFunctions.nsToSec(event.timestamp);

        } else if (sensorType == Sensor.TYPE_ROTATION_VECTOR) {
            textRotation.setText(String.valueOf(event.values[2]));

        } else if (sensorType == Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR) {
            textGeoRotation.setText(String.valueOf(event.values[2]));

        } else if (sensorType == Sensor.TYPE_GAME_ROTATION_VECTOR) {
            textGameRotation.setText(String.valueOf(event.values[2]));

        }
    }



}
