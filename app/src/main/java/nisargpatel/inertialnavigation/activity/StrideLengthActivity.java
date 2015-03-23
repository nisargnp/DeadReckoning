package nisargpatel.inertialnavigation.activity;

import android.annotation.TargetApi;
import android.content.Intent;
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
import android.widget.Toast;

import java.util.Arrays;

import nisargpatel.inertialnavigation.R;

public class StrideLengthActivity extends ActionBarActivity implements SensorEventListener {

    private TextView textAndroidSteps;
    private TextView textCalibrationDistance;
    private TextView textInstantAcc;

    private Button buttonStartCalibration;
    private Button buttonStopCalibraton;
    private Button buttonSetStrideLength;

    private Sensor accelerometer;
    private Sensor androidStepCounter;
    private SensorManager sensorManager;

    private static int stepCount;

    private boolean wasRunning;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);

        stepCount = 0;

        textAndroidSteps = (TextView) findViewById(R.id.textCalibrateSteps);
        textCalibrationDistance = (TextView) findViewById(R.id.textCalibrationDistance);
        textInstantAcc = (TextView) findViewById(R.id.textCalibrateInstantAcc);

        buttonStartCalibration = (Button) findViewById(R.id.buttonStartCalibration);
        buttonStopCalibraton = (Button) findViewById(R.id.buttonStopCalibration);
        buttonSetStrideLength = (Button) findViewById(R.id.buttonSetStrideLength);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        androidStepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        //activate sensors when start button is pressed
        buttonStartCalibration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorManager.registerListener(StrideLengthActivity.this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
                sensorManager.registerListener(StrideLengthActivity.this, androidStepCounter, SensorManager.SENSOR_DELAY_FASTEST);

                buttonStartCalibration.setEnabled(false);
                buttonSetStrideLength.setEnabled(false);
                buttonStopCalibraton.setEnabled(true);

                wasRunning = true;
            }
        });

        //deactivate sensors when stop button is pressed
        buttonStopCalibraton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorManager.unregisterListener(StrideLengthActivity.this);

                buttonStartCalibration.setEnabled(true);
                buttonSetStrideLength.setEnabled(true);
                buttonStopCalibraton.setEnabled(false);

                wasRunning = false;
            }
        });

        //when the button is pressed, determine the strideLength by dividing stepsTaken
        //by distanceTraveled, and stored stride length in StepCountActivity
        buttonSetStrideLength.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double strideLength;
                if (stepCount != 0) {
                    strideLength = (double) Integer.parseInt(textCalibrationDistance.getText().toString()) / stepCount;
                } else {
                    Toast.makeText(getApplication(), "Take a few steps first!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(getApplicationContext(), "Stride length set: " + strideLength + " ft/sec.", Toast.LENGTH_SHORT).show();

                Intent myIntent = getIntent();
                myIntent.putExtra("stride_length", strideLength);
                setResult(RESULT_OK, myIntent);
                finish();

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
            sensorManager.registerListener(StrideLengthActivity.this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
            sensorManager.registerListener(StrideLengthActivity.this, androidStepCounter, SensorManager.SENSOR_DELAY_FASTEST);

            buttonStartCalibration.setEnabled(false);
            buttonSetStrideLength.setEnabled(false);
            buttonStopCalibraton.setEnabled(true);
        } else {
            buttonStartCalibration.setEnabled(true);
            buttonSetStrideLength.setEnabled(true);
            buttonStopCalibraton.setEnabled(false);
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onSensorChanged(SensorEvent event) {

        //if the sensor data is of step counter type, increment stepCount
        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            if (event.values[0] == 1.0) {
                textInstantAcc.setText(Arrays.toString(event.values));
                stepCount++;
                textAndroidSteps.setText(String.valueOf(stepCount));
            }
            //if the data is of accelerometer type, display the instantaneous acceleration
        } else {
            textInstantAcc.setText(String.valueOf(event.values[2]).substring(0, 5));
        }
    }

}
