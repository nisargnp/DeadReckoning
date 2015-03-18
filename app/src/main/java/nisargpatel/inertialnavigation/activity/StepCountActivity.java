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
import android.widget.TextView;

import nisargpatel.inertialnavigation.R;
import nisargpatel.inertialnavigation.dialog.StepInfoFragment;
import nisargpatel.inertialnavigation.stepcounting.DynamicStepCounter;
import nisargpatel.inertialnavigation.stepcounting.StaticStepCounter;

public class StepCountActivity extends ActionBarActivity implements SensorEventListener{

    private static double upperThreshold = 11.5;
    private static double lowerThreshold = 6.5;

    private StepInfoFragment myDialog;

    //declaring views
    private TextView textStaticCounter;
    private TextView textDynamicCounter;
    private TextView textAndroidCounter;
    private TextView textInstantAcc;

    //declaring sensors
    private Sensor sensorAccelerometer;
    private Sensor sensorStepDetector;
    private SensorManager sensorManager;

    //declaring step detectors
    StaticStepCounter[] staticStepCounters;
    DynamicStepCounter[] dynamicStepCounters;

    //declaring step counts
    private int androidStepCount;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_counter);

        myDialog = new StepInfoFragment();

        //defining views
        textStaticCounter = (TextView) findViewById(R.id.textThreshold);
        textDynamicCounter = (TextView) findViewById(R.id.textMovingAverage);
        textAndroidCounter = (TextView) findViewById(R.id.textAndroid);
        textInstantAcc = (TextView) findViewById(R.id.textInstantAcc);

        //defining sensors
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorStepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        //defining step detectors
        staticStepCounters = new StaticStepCounter[5];
        staticStepCounters[0] = new StaticStepCounter(upperThreshold, lowerThreshold);
        staticStepCounters[1] = new StaticStepCounter(10, 8);
        staticStepCounters[2] = new StaticStepCounter(11, 7);
        staticStepCounters[3] = new StaticStepCounter(12, 6);
        staticStepCounters[4] = new StaticStepCounter(13, 5);

        dynamicStepCounters = new DynamicStepCounter[5];
        dynamicStepCounters[0] = new DynamicStepCounter(5.0);
        dynamicStepCounters[1] = new DynamicStepCounter(3.0);
        dynamicStepCounters[2] = new DynamicStepCounter(4.0);
        dynamicStepCounters[3] = new DynamicStepCounter(6.0);
        dynamicStepCounters[4] = new DynamicStepCounter(7.0);

        clearCounters();

        //launches when the start button is pressed, and activates the sensors
        findViewById(R.id.buttonStartCounter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                staticStepCounters[0].setThresholds(upperThreshold, lowerThreshold);
                sensorManager.registerListener(StepCountActivity.this, sensorAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
                sensorManager.registerListener(StepCountActivity.this, sensorStepDetector, SensorManager.SENSOR_DELAY_FASTEST);
            }
        });

        //launches when the stop button is pressed, and deactivates the sensors
        findViewById(R.id.buttonStopCounter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorManager.unregisterListener(StepCountActivity.this, sensorAccelerometer);
                sensorManager.unregisterListener(StepCountActivity.this, sensorStepDetector);
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
                myDialog.show(getSupportFragmentManager(), "Step Info");
            }
        });

        findViewById(R.id.txtViewThresholds).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(getApplicationContext(), SetThresholdsActivity.class);
                startActivity(myIntent);
            }
        });

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

        } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            final Double norm = Math.sqrt(Math.pow(event.values[0], 2) +
                    Math.pow(event.values[1], 2) +
                    Math.pow(event.values[2], 2));
            final Double acc = (double) event.values[2];

            //display the instantaneous acceleration to let the user know the step counter is working
            textInstantAcc.setText(String.valueOf(acc).substring(0, 5));

            //using separate thread for step counters to minimize load on UI Thread
            new Thread(new Runnable() {
                @Override
                public void run() {

                    for (StaticStepCounter staticStepCounter : staticStepCounters)
                        staticStepCounter.findStep(acc);

                    for (DynamicStepCounter dynamicStepCounter : dynamicStepCounters)
                        dynamicStepCounter.findStep(acc);

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

    //setting thresholds for the StaticStepCounter (this method is called by SetThresholdsActivity)
    public static void setThresholds(double upper, double lower) {
        upperThreshold = upper;
        lowerThreshold = lower;
    }

    private String getDialogMessage() {

        String message;

        String t1 = String.format("T(%.1f, %.1f) = %d", staticStepCounters[1].getUpperThreshold(), staticStepCounters[1].getLowerThreshold(), staticStepCounters[1].getStepCount());
        String t2 = String.format("T(%.1f, %.1f) = %d", staticStepCounters[2].getUpperThreshold(), staticStepCounters[2].getLowerThreshold(), staticStepCounters[2].getStepCount());
        String t3 = String.format("T(%.1f, %.1f) = %d", staticStepCounters[3].getUpperThreshold(), staticStepCounters[3].getLowerThreshold(), staticStepCounters[3].getStepCount());
        String t4 = String.format("T(%.1f, %.1f) = %d", staticStepCounters[4].getUpperThreshold(), staticStepCounters[4].getLowerThreshold(), staticStepCounters[4].getStepCount());

        String a1 = String.format("A(%.1f) = %d", dynamicStepCounters[1].getSensitivity(), dynamicStepCounters[1].getStepCount());
        String a2 = String.format("A(%.1f) = %d", dynamicStepCounters[2].getSensitivity(), dynamicStepCounters[2].getStepCount());
        String a3 = String.format("A(%.1f) = %d", dynamicStepCounters[3].getSensitivity(), dynamicStepCounters[3].getStepCount());
        String a4 = String.format("A(%.1f) = %d", dynamicStepCounters[4].getSensitivity(), dynamicStepCounters[4].getStepCount());

        message = t1 + "\n" + t2 + "\n" + t3 + "\n" + t4 + "\n" + "\n" + a1 + "\n" + a2 + "\n" + a3 + "\n" + a4;

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
