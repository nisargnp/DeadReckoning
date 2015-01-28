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
    private Sensor rotation;
    private SensorManager sm;

    private static long recordedTimeGyro;

    private double averageGyroValue;
    private double totalGyroValue;

    private TextView textGyro;
    private TextView textRotationVector;

    boolean gyroFirstRun;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orientation_test);

        averageGyroValue = 0;
        totalGyroValue = 0;
        gyroFirstRun = true;

        textRotationVector = (TextView) findViewById(R.id.textTotalRotation);
        textGyro = (TextView) findViewById(R.id.textTotalGyro);

        Button buttonStart = (Button) findViewById(R.id.buttonStart);
        Button buttonStop = (Button) findViewById(R.id.buttonStop);

        sm = (SensorManager) getSystemService(SENSOR_SERVICE);

        //motion
        gyroscope = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        rotation = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        //Other Sensor Types
        //TYPE_GYROSCOPE_UNCALIBRATED is similar to TYPE_GYROSCOPE does not automatically correct for drift
        //TYPE_ROTATION_VECTOR is a Motion Sensor
        //TYPE_GAME_ROTATION_VECTOR and TYPE_GEOMAGNETIC_ROTATION_VECTOR are Position Sensors
        //  TYPE_GAME_ROTATION_VECTOR is uncalibrated TYPE_ROTATION_VECTOR (it doesn't use geomagnetic field, so has increased drift)
        //  TYPE_GEOMAGNETIC_ROTATION_VECTOR is similar to TYPE_ROTATION_VECTOR (except it uses magnetometer instead of gyroscope)

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sm.registerListener(OrientationTestActivity.this, gyroscope, SensorManager.SENSOR_DELAY_FASTEST);
                sm.registerListener(OrientationTestActivity.this, rotation, SensorManager.SENSOR_DELAY_FASTEST);
                recordedTimeGyro = System.currentTimeMillis();
            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sm.unregisterListener(OrientationTestActivity.this, gyroscope);
                sm.unregisterListener(OrientationTestActivity.this, rotation);
            }
        });

        findViewById(R.id.buttonClear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textGyro.setText("");
                textRotationVector.setText("");
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
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {

            double currentGyroValue = (System.currentTimeMillis() - recordedTimeGyro) * event.values[2];
            Log.d("gyroData", "gyro: " + currentGyroValue);

            if (gyroFirstRun)
                averageGyroValue = currentGyroValue;

            if (currentGyroValue > averageGyroValue * 100000) {
                totalGyroValue += currentGyroValue;
                textGyro.setText(String.valueOf(totalGyroValue));
            } else {
                averageGyroValue = (averageGyroValue + currentGyroValue) / 2;
            }

            recordedTimeGyro = System.currentTimeMillis();

            gyroFirstRun = false;

        } else if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            textRotationVector.setText(String.valueOf(event.values[2]));
        }
    }


}
