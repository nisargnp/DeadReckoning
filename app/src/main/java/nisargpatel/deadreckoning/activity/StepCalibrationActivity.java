package nisargpatel.deadreckoning.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import nisargpatel.deadreckoning.R;
import nisargpatel.deadreckoning.dialog.StepCalibrationDialogFragment;
import nisargpatel.deadreckoning.extra.ExtraFunctions;
import nisargpatel.deadreckoning.interfaces.OnPreferredStepCounterListener;
import nisargpatel.deadreckoning.stepcounting.DynamicStepCounter;

public class StepCalibrationActivity extends Activity implements SensorEventListener, OnPreferredStepCounterListener {

    private TextView textAndroidSteps;
    private TextView textCalibrationDistance;
    private TextView textInstantAcc;

    private Button buttonStartCalibration;
    private Button buttonStopCalibration;
    private Button buttonSetStrideLength;

    private Sensor linearAcceleration;
    private Sensor androidStepCounter;
    private SensorManager sensorManager;

    private DynamicStepCounter[] dynamicStepCounters;

    private int stepCount;
    private int preferredStepCounterIndex;

    private boolean wasRunning;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_calibration);

        //defining variables
        stepCount = 0;

        dynamicStepCounters = new DynamicStepCounter[20];

        double sensitivity = 0.5;
        for (int i = 0; i < dynamicStepCounters.length; i++) {
            dynamicStepCounters[i] = new DynamicStepCounter(sensitivity);
            sensitivity += 0.05;
        }

        //defining views
        textAndroidSteps = findViewById(R.id.textCalibrateSteps);
        textCalibrationDistance = findViewById(R.id.textCalibrationDistance);
        textInstantAcc = findViewById(R.id.textCalibrateInstantAcc);

        buttonStartCalibration = findViewById(R.id.buttonStartCalibration);
        buttonStopCalibration = findViewById(R.id.buttonStopCalibration);
        buttonSetStrideLength = findViewById(R.id.buttonSetStrideLength);

        //defining sensors
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        linearAcceleration = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        androidStepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        //activate sensors when start button is pressed
        buttonStartCalibration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorManager.registerListener(StepCalibrationActivity.this, linearAcceleration, SensorManager.SENSOR_DELAY_FASTEST);
                sensorManager.registerListener(StepCalibrationActivity.this, androidStepCounter, SensorManager.SENSOR_DELAY_FASTEST);

                buttonStartCalibration.setEnabled(false);
                buttonSetStrideLength.setEnabled(false);
                buttonStopCalibration.setEnabled(true);

                wasRunning = true;
            }
        });

        //deactivate sensors when stop button is pressed and open step_counters dialog
        buttonStopCalibration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorManager.unregisterListener(StepCalibrationActivity.this);

                String[] stepCounts = new String[dynamicStepCounters.length];
                for (int i = 0; i < stepCounts.length; i++)
                    stepCounts[i] = String.format(Locale.US, "Sensitivity: %.2f :: Step Count: %d", dynamicStepCounters[i].getSensitivity(), dynamicStepCounters[i].getStepCount());

                //creating dialog, setting the stepCounts list, and setting a handler
                StepCalibrationDialogFragment stepCalibrationDialogFragment = new StepCalibrationDialogFragment();
                stepCalibrationDialogFragment.setOnPreferredStepCounterListener(StepCalibrationActivity.this);
                stepCalibrationDialogFragment.setStepList(stepCounts);
                stepCalibrationDialogFragment.show(getFragmentManager(), "step_counters");

                buttonStartCalibration.setEnabled(true);
                buttonSetStrideLength.setEnabled(true);
                buttonStopCalibration.setEnabled(false);

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

                String strideLengthStr = String.format(Locale.US, "%.2f", strideLength);
                Toast.makeText(getApplicationContext(), "Stride length set: " + strideLengthStr + " ft/step", Toast.LENGTH_SHORT).show();

                //returns the stride_length and preferred_step_counter info to the calling activity
                Intent myIntent = getIntent();
                myIntent.putExtra("stride_length", strideLength);
                myIntent.putExtra("preferred_step_counter", dynamicStepCounters[preferredStepCounterIndex].getSensitivity());
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
            sensorManager.registerListener(StepCalibrationActivity.this, linearAcceleration, SensorManager.SENSOR_DELAY_FASTEST);
            sensorManager.registerListener(StepCalibrationActivity.this, androidStepCounter, SensorManager.SENSOR_DELAY_FASTEST);

            buttonStartCalibration.setEnabled(false);
            buttonSetStrideLength.setEnabled(false);
            buttonStopCalibration.setEnabled(true);
        } else {
            buttonStartCalibration.setEnabled(true);
            buttonSetStrideLength.setEnabled(true);
            buttonStopCalibration.setEnabled(false);
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            if (event.values[0] == 1.0) {
                stepCount++;
                textAndroidSteps.setText(String.valueOf(stepCount));
            }
        } else if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {

            float norm = ExtraFunctions.calcNorm(
                    event.values[0] +
                    event.values[1] +
                    event.values[2]
            );

            String instantAcc = String.valueOf(event.values[2]);
            textInstantAcc.setText(instantAcc.length() <= 5 ? instantAcc : instantAcc.substring(0, 5));

            for(DynamicStepCounter dynamicStepCounter : dynamicStepCounters)
                dynamicStepCounter.findStep(norm);
        }

    }

    @Override
    public void onPreferredStepCounter(int preferredStepCounterIndex) {
        this.preferredStepCounterIndex = preferredStepCounterIndex;
        this.stepCount = dynamicStepCounters[preferredStepCounterIndex].getStepCount();
        this.textAndroidSteps.setText(String.valueOf(this.stepCount));
    }

}
