package nisargpatel.inertialnavigation.activity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import nisargpatel.inertialnavigation.R;

public class OrientationTestActivity extends ActionBarActivity implements SensorEventListener{

    private Sensor gyroscope;
    private Sensor rotationVector;
    private Sensor geoRotationVector;
    private Sensor gameRotationVector;
    private SensorManager sm;

    private static long recordedTimeGyro;

    private double averageGyroValue;
    private double totalGyroValue;

    private TextView textGyroscope;
    private TextView textRotation;
    private TextView textGeoRotation;
    private TextView textGameRotation;

    boolean gyroFirstRun;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orientation_test);

        averageGyroValue = 0;
        totalGyroValue = 0;
        gyroFirstRun = true;

        textGyroscope = (TextView) findViewById(R.id.textGyroscope);
        textRotation = (TextView) findViewById(R.id.textRotation);
        textGeoRotation = (TextView) findViewById(R.id.textGeoRotation);
        textGameRotation = (TextView) findViewById(R.id.textGameRotation);

        Button buttonStart = (Button) findViewById(R.id.buttonStart);
        Button buttonStop = (Button) findViewById(R.id.buttonStop);

        sm = (SensorManager) getSystemService(SENSOR_SERVICE);

        gyroscope = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        rotationVector = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        geoRotationVector = sm.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR);
        gameRotationVector = sm.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sm.registerListener(OrientationTestActivity.this, gyroscope, SensorManager.SENSOR_DELAY_FASTEST);
                sm.registerListener(OrientationTestActivity.this, rotationVector, SensorManager.SENSOR_DELAY_FASTEST);
                sm.registerListener(OrientationTestActivity.this, geoRotationVector, SensorManager.SENSOR_DELAY_FASTEST);
                sm.registerListener(OrientationTestActivity.this, gameRotationVector, SensorManager.SENSOR_DELAY_FASTEST);
                recordedTimeGyro = System.currentTimeMillis();
            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sm.unregisterListener(OrientationTestActivity.this, gyroscope);
                sm.unregisterListener(OrientationTestActivity.this, rotationVector);
                sm.unregisterListener(OrientationTestActivity.this, geoRotationVector);
                sm.unregisterListener(OrientationTestActivity.this, gameRotationVector);
            }
        });

        findViewById(R.id.buttonClear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

        if (sensorType == Sensor.TYPE_GYROSCOPE) {

            double currentGyroValue = (System.currentTimeMillis() - recordedTimeGyro) * event.values[2];

            if (gyroFirstRun)
                averageGyroValue = currentGyroValue;

            if (currentGyroValue > averageGyroValue * 100000) {
                totalGyroValue += currentGyroValue;
                textGyroscope.setText(String.valueOf(totalGyroValue));
            } else {
                averageGyroValue = (averageGyroValue + currentGyroValue) / 2;
            }

            recordedTimeGyro = System.currentTimeMillis();

            gyroFirstRun = false;

        } else if (sensorType == Sensor.TYPE_ROTATION_VECTOR) {
            textRotation.setText(String.valueOf(event.values[2]));
        } else if (sensorType == Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR) {
            textGeoRotation.setText(String.valueOf(event.values[2]));
        } else if (sensorType == Sensor.TYPE_GAME_ROTATION_VECTOR) {
            textGameRotation.setText(String.valueOf(event.values[2]));
        }
    }


}
