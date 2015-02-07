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
import nisargpatel.inertialnavigation.heading.NewHeadingInference;
import nisargpatel.inertialnavigation.math.MathFunctions;

public class OrientationTestActivity extends ActionBarActivity implements SensorEventListener{

    private NewHeadingInference newHeadingInference;

    private Sensor gyroscopeCalibrated;
    private Sensor gyroscopeUncalibrated;
    private Sensor rotationVector;
    private Sensor geoRotationVector;
    private Sensor gameRotationVector;
    private SensorManager sm;

    private static double recordedGyroTime;

    private double averageGyroValue;
    private double totalGyroValue;

    private TextView textGyroscopeMatrix;
    private TextView textGyroscope;
    private TextView textRotation;
    private TextView textGeoRotation;
    private TextView textGameRotation;

    boolean isFirstGyroRun;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orientation_test);

        newHeadingInference = new NewHeadingInference(new float[][] {{1,0,0},
                                                                     {0,1,0},
                                                                     {0,0,1}});

        averageGyroValue = 0;
        totalGyroValue = 0;
        isFirstGyroRun = true;

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
                isFirstGyroRun = true;
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

    int runCountGyroU = 0;
    double recordedGyroUTime;

    float gyroUBiasX;
    float gyroUBiasY;
    float gyroUBiasZ;



    @Override
    public void onSensorChanged(SensorEvent event) {

        int sensorType = event.sensor.getType();

        if (sensorType == Sensor.TYPE_GYROSCOPE_UNCALIBRATED) {

            runCountGyroU++;

            if (runCountGyroU == 1) {
                recordedGyroUTime = MathFunctions.nsToSec(event.timestamp);
                return;
            }

            double currentGyroUTime = MathFunctions.nsToSec(event.timestamp);

            if (runCountGyroU == 2) {
                gyroUBiasX = (float) (currentGyroUTime - recordedGyroUTime) * event.values[0];
                gyroUBiasY = (float) (currentGyroUTime - recordedGyroUTime) * event.values[1];
                gyroUBiasZ = (float) (currentGyroUTime - recordedGyroUTime) * event.values[2];
                return;
            }

            if (runCountGyroU <= 500) {
                float currGyroUX = (float) (currentGyroUTime - recordedGyroUTime) * event.values[0];
                float currGyroUY = (float) (currentGyroUTime - recordedGyroUTime) * event.values[1];
                float currGyroUZ = (float) (currentGyroUTime - recordedGyroUTime) * event.values[2];

                gyroUBiasX = (gyroUBiasX * ((runCountGyroU - 1) / runCountGyroU)) + (currGyroUX / runCountGyroU);
                gyroUBiasY = (gyroUBiasY * ((runCountGyroU - 1) / runCountGyroU)) + (currGyroUY / runCountGyroU);
                gyroUBiasZ = (gyroUBiasZ * ((runCountGyroU - 1) / runCountGyroU)) + (currGyroUZ / runCountGyroU);
                return;
            }

            float currGyroUX = (float) (currentGyroUTime - recordedGyroUTime) * event.values[0];
            float currGyroUY = (float) (currentGyroUTime - recordedGyroUTime) * event.values[1];
            float currGyroUZ = (float) (currentGyroUTime - recordedGyroUTime) * event.values[2];

            newHeadingInference.setBias(gyroUBiasX, gyroUBiasY, gyroUBiasZ);
            newHeadingInference.setValues(currGyroUX, currGyroUY, currGyroUZ);
            float heading = newHeadingInference.getHeading();

            textGyroscopeMatrix.setText(String.valueOf(heading));


        } else if (sensorType == Sensor.TYPE_GYROSCOPE) {//on the first run, the timestamp of the first point needs to be set
            //so that delta-t can be calculated for the next point
            if (isFirstGyroRun) {
                recordedGyroTime = MathFunctions.nsToSec(event.timestamp);
                isFirstGyroRun = false;
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
