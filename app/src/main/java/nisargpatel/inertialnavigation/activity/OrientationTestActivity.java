package nisargpatel.inertialnavigation.activity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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

    private final float[][] IDENTITY_MATRIX = new float[][]{{1,0,0},
                                                            {0,1,0},
                                                            {0,0,1}};
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

    private static double recordedGyroTime;
    double recordedGyroUTime;

    private double averageGyroValue;
    private double totalGyroValue;

    int runCountGyro;
    int runCountGyroU;

//    float gyroUBiasX;
//    float gyroUBiasY;
//    float gyroUBiasZ;
    float[] gyroUBias = new float[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orientation_test);

        matrixHeadingInference = new MatrixHeadingInference(IDENTITY_MATRIX.clone());

        averageGyroValue = 0;
        totalGyroValue = 0;
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

                totalGyroValue = 0;
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
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        int sensorType = event.sensor.getType();

        if (sensorType == Sensor.TYPE_GYROSCOPE_UNCALIBRATED) {

            runCountGyroU++;

            //setting the initial timestamp on the first run
            if (runCountGyroU == 1) {
                recordedGyroUTime = MathFunctions.nsToSec(event.timestamp);
                return;
            }



            double currentGyroUTime = MathFunctions.nsToSec(event.timestamp);
            double deltaGyroUTime = currentGyroUTime - recordedGyroUTime;

            //setting the initial bias on the second run
            if (runCountGyroU == 2) {
                gyroUBias[0] = (float) deltaGyroUTime * event.values[0];
                gyroUBias[1] = (float) deltaGyroUTime * event.values[1];
                gyroUBias[2] = (float) deltaGyroUTime * event.values[2];
                return;
            }

            //averaging bias for the first few hundred data points
            if (runCountGyroU <= 300) {
                float[] currGyroUValue = new float[3];
                currGyroUValue[0] = (float) deltaGyroUTime * event.values[0];
                currGyroUValue[1] = (float) deltaGyroUTime * event.values[1];
                currGyroUValue[2] = (float) deltaGyroUTime * event.values[2];

                gyroUBias[0] = (gyroUBias[0] * ((runCountGyroU - 1) / runCountGyroU)) + (currGyroUValue[0] / runCountGyroU);
                gyroUBias[1] = (gyroUBias[1] * ((runCountGyroU - 1) / runCountGyroU)) + (currGyroUValue[1] / runCountGyroU);
                gyroUBias[2] = (gyroUBias[2] * ((runCountGyroU - 1) / runCountGyroU)) + (currGyroUValue[2] / runCountGyroU);
                return;
            } else {
                matrixHeadingInference.setBias(gyroUBias);
            }

            float[] currGyroUValue = new float[3];
            currGyroUValue[0] = (float) deltaGyroUTime * event.values[0];
            currGyroUValue[1] = (float) deltaGyroUTime * event.values[1];
            currGyroUValue[2] = (float) deltaGyroUTime * event.values[2];

            float heading = matrixHeadingInference.getCurrentHeading(currGyroUValue);
            textGyroscopeMatrix.setText(String.valueOf(heading));

            recordedGyroUTime = currentGyroUTime;

        } else if (sensorType == Sensor.TYPE_GYROSCOPE) {//on the first run, the timestamp of the first point needs to be set

            runCountGyro++;

            //so that delta-t can be calculated for the next point
            if (runCountGyro <= 1) {
                recordedGyroTime = MathFunctions.nsToSec(event.timestamp);
                return;
            }

            double currentGyroTime = MathFunctions.nsToSec(event.timestamp);
            double currentGyroValue = (currentGyroTime - recordedGyroTime) * event.values[2];

            totalGyroValue += currentGyroValue;
            textGyroscope.setText(String.valueOf(totalGyroValue));

            recordedGyroTime = currentGyroTime;


        } else if (sensorType == Sensor.TYPE_ROTATION_VECTOR) {
            textRotation.setText(String.valueOf(event.values[2]));

        } else if (sensorType == Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR) {
            textGeoRotation.setText(String.valueOf(event.values[2]));

        } else if (sensorType == Sensor.TYPE_GAME_ROTATION_VECTOR) {
            textGameRotation.setText(String.valueOf(event.values[2]));

        }
    }



}
