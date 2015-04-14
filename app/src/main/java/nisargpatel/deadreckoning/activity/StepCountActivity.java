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
import nisargpatel.deadreckoning.dialog.StepInfoDialogFragment;
import nisargpatel.deadreckoning.extra.ExtraFunctions;
import nisargpatel.deadreckoning.stepcounting.DynamicStepCounter;
import nisargpatel.deadreckoning.stepcounting.StaticStepCounter;

public class StepCountActivity extends Activity implements SensorEventListener{

    private StepInfoDialogFragment myDialog;

    //declaring views
    private Button buttonStartCounter;
    private Button buttonStopCounter;
    private TextView textStaticCounter;
    private TextView textDynamicCounter;
    private TextView textAndroidCounter;
    private TextView textInstantAcc;

    //declaring sensors
    private Sensor sensorAccelerometer;
    private Sensor sensorLinearAcceleration;
    private Sensor sensorStepDetector;
    private SensorManager sensorManager;

    //declaring step detectors
    private static StaticStepCounter[] staticStepCounters;
    private static DynamicStepCounter[] dynamicStepCounters;

    //declaring step counts
    private int androidStepCount;

    private boolean wasRunning;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_counter);

        myDialog = new StepInfoDialogFragment();

        //defining views
        buttonStartCounter = (Button) findViewById(R.id.buttonStartCounter);
        buttonStopCounter = (Button) findViewById(R.id.buttonStopCounter);
        textStaticCounter = (TextView) findViewById(R.id.textThreshold);
        textDynamicCounter = (TextView) findViewById(R.id.textMovingAverage);
        textAndroidCounter = (TextView) findViewById(R.id.textAndroid);
        textInstantAcc = (TextView) findViewById(R.id.textInstantAcc);

        //defining sensors
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorLinearAcceleration = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorStepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        //defining step detectors
        staticStepCounters = new StaticStepCounter[5];
        staticStepCounters[0] = new StaticStepCounter(2, 1.9);
        staticStepCounters[1] = new StaticStepCounter(3, 2.9);
        staticStepCounters[2] = new StaticStepCounter(4, 3.9);
        staticStepCounters[3] = new StaticStepCounter(5, 4.9);
        staticStepCounters[4] = new StaticStepCounter(6, 5.9);

        dynamicStepCounters = new DynamicStepCounter[5];
        dynamicStepCounters[0] = new DynamicStepCounter(0.875);
        dynamicStepCounters[1] = new DynamicStepCounter(0.80);
        dynamicStepCounters[2] = new DynamicStepCounter(0.85);
        dynamicStepCounters[3] = new DynamicStepCounter(0.90);
        dynamicStepCounters[4] = new DynamicStepCounter(0.95);

        clearCounters();

        //launches when the start button is pressed, and activates the sensors
        buttonStartCounter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //TODO: enable this
                //staticStepCounters[0].setThresholds(upperThreshold, lowerThreshold);

                sensorManager.registerListener(StepCountActivity.this, sensorAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
                sensorManager.registerListener(StepCountActivity.this, sensorLinearAcceleration, SensorManager.SENSOR_DELAY_FASTEST);
                sensorManager.registerListener(StepCountActivity.this, sensorStepDetector, SensorManager.SENSOR_DELAY_FASTEST);

                buttonStartCounter.setEnabled(false);
                buttonStopCounter.setEnabled(true);

                wasRunning = true;
            }
        });

        //launches when the stop button is pressed, and deactivates the sensors
        buttonStopCounter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorManager.unregisterListener(StepCountActivity.this, sensorAccelerometer);
                sensorManager.unregisterListener(StepCountActivity.this, sensorLinearAcceleration);
                sensorManager.unregisterListener(StepCountActivity.this, sensorStepDetector);

                buttonStartCounter.setEnabled(true);
                buttonStopCounter.setEnabled(false);

                wasRunning = false;
            }
        });

        findViewById(R.id.buttonClearCounter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearCounters();
            }
        });

        findViewById(R.id.buttonStepInfo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.setDialogMessage(getDialogMessage());
                myDialog.show(getFragmentManager(), "Step Info");
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
            sensorManager.registerListener(StepCountActivity.this, sensorAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
            sensorManager.registerListener(StepCountActivity.this, sensorStepDetector, SensorManager.SENSOR_DELAY_FASTEST);

            buttonStartCounter.setEnabled(false);
            buttonStopCounter.setEnabled(true);
        } else {
            buttonStartCounter.setEnabled(true);
            buttonStopCounter.setEnabled(false);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    //This only works as long as the sensor is registered
    //Registration of the sensor is controlled by the OnClick methods in OnCreate
    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {

            if (event.values[0] == 1.0) {
                androidStepCount++;
                textAndroidCounter.setText(String.valueOf(androidStepCount));
            }
        } else if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {

            final double norm = ExtraFunctions.calcNorm(
                    event.values[0] +
                    event.values[1] +
                    event.values[2]
            );

            //display the instantaneous acceleration to let the user know the step counter is working
            textInstantAcc.setText(String.valueOf(norm).substring(0, 5));

            //using separate thread for step counters to minimize load on UI Thread
            new Thread(new Runnable() {
                @Override
                public void run() {

                    for (StaticStepCounter staticStepCounter : staticStepCounters)
                        staticStepCounter.findStep(norm);

                    for (DynamicStepCounter dynamicStepCounter : dynamicStepCounters)
                        dynamicStepCounter.findStep(norm);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textStaticCounter.setText(String.valueOf(staticStepCounters[0].getStepCount()));
                            textDynamicCounter.setText(String.valueOf(dynamicStepCounters[0].getStepCount()));
                        }
                    });

                }
            }).start();

        }

    }

    private String getDialogMessage() {

        String message = "";

        for (int i = 1; i < staticStepCounters.length; i++)
            message += String.format("T(%.1f, %.1f) = %d\n", staticStepCounters[i].getUpperThreshold(), staticStepCounters[i].getLowerThreshold(), staticStepCounters[i].getStepCount());

        message += "\n";

        for (int i = 1; i < dynamicStepCounters.length; i++)
            message += String.format("A(%.1f) = %d\n", dynamicStepCounters[i].getSensitivity(), dynamicStepCounters[i].getStepCount());

        return message;
    }

    private void clearCounters() {

        textStaticCounter.setText("0");
        textDynamicCounter.setText("0");
        textAndroidCounter.setText("0");
        textInstantAcc.setText("0");

        androidStepCount = 0;

        for (StaticStepCounter staticStepCounter : staticStepCounters) {
            staticStepCounter.clearStepCount();
        }

        for (DynamicStepCounter dynamicStepCounter : dynamicStepCounters) {
            dynamicStepCounter.clearStepCount();
        }

    }

}
