package nisargpatel.inertialnavigation.activity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import nisargpatel.inertialnavigation.R;

public class CalibrationActivity extends ActionBarActivity implements SensorEventListener {

    private Button buttonStartCalibration;
    private Button buttonStopCalibration;

    private TextView textAndroid2;
    private TextView textCalibrationDistance;
    private TextView textInstantAcc2;

    private Sensor accelerometer;
    private Sensor androidStepCounter;
    private SensorManager sensorManager;

    private static int stepCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);

        stepCount = 0;

        buttonStartCalibration = (Button) findViewById(R.id.buttonStartCalibration);
        buttonStopCalibration = (Button) findViewById(R.id.buttonStopCalibration);

        textAndroid2 = (TextView) findViewById(R.id.textAndroid2);
        textCalibrationDistance = (TextView) findViewById(R.id.textCalibrationDistance);
        textInstantAcc2 = (TextView) findViewById(R.id.textInstantAcc2);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        androidStepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        //activate sensors when start button is pressed
        buttonStartCalibration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorManager.registerListener(CalibrationActivity.this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
                sensorManager.registerListener(CalibrationActivity.this, androidStepCounter, SensorManager.SENSOR_DELAY_FASTEST);
                Toast.makeText(getApplicationContext(), "Calibration mode started.", Toast.LENGTH_SHORT).show();
            }
        });

        //deactivate sensors when stop button is pressed
        buttonStopCalibration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorManager.unregisterListener(CalibrationActivity.this, accelerometer);
                sensorManager.unregisterListener(CalibrationActivity.this, androidStepCounter);
                Toast.makeText(getApplicationContext(), "Calibration mode stopped.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_calibration, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        //if the sensor data is of step counter type, increment stepCount
        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            if (event.values[0] == 1.0) {
                textInstantAcc2.setText(String.valueOf(event.values));
                stepCount++;
                textAndroid2.setText(String.valueOf(stepCount));
            }
            //if the data is of accelerometer type, display the instantaneous acceleration
        } else {
            textInstantAcc2.setText(String.valueOf(event.values[2]).substring(0, 5));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    //when the button is pressed, determine the strideLength by dividing stepsTaken by distanceTraveled, and stored stride length in StepCounterActivity
    public void buttonSetStrideLength(View view) {
        double strideLength;
        strideLength = (double) Integer.parseInt(textCalibrationDistance.getText().toString()) / (stepCount + 1);
        StepCounterActivity.setStrideLength(strideLength);

        Toast.makeText(getApplicationContext(), "Stride length set: " + strideLength + ".", Toast.LENGTH_SHORT).show();

        finish();
    }
}
