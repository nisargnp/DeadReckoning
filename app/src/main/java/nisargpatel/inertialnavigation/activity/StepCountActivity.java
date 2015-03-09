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
    private TextView textThresholdSteps;
    private TextView textMovingAvgSteps;
    private TextView textAndroidSteps;
    private TextView textInstantAcc;

    //declaring sensors
    private Sensor sensorAccelerometer;
    private Sensor sensorStepDetector;
    private SensorManager sensorManager;

    //declaring step detectors
    StaticStepCounter[] thresholdStepCounter;
    DynamicStepCounter[] movAvgStepCounter;

    //declaring step counts
    private int androidStepCount;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_counter);

        myDialog = new StepInfoFragment();

        //defining views
        textThresholdSteps = (TextView) findViewById(R.id.textThreshold);
        textMovingAvgSteps = (TextView) findViewById(R.id.textMovingAverage);
        textAndroidSteps = (TextView) findViewById(R.id.textAndroid);
        textInstantAcc = (TextView) findViewById(R.id.textInstantAcc);

        //defining sensors
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorStepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        //defining step detectors
        thresholdStepCounter = new StaticStepCounter[5];
        thresholdStepCounter[0] = new StaticStepCounter(upperThreshold, lowerThreshold);
        thresholdStepCounter[1] = new StaticStepCounter(10, 8);
        thresholdStepCounter[2] = new StaticStepCounter(11, 7);
        thresholdStepCounter[3] = new StaticStepCounter(12, 6);
        thresholdStepCounter[4] = new StaticStepCounter(13, 5);

        movAvgStepCounter = new DynamicStepCounter[5];
        movAvgStepCounter[0] = new DynamicStepCounter(5.0);
        movAvgStepCounter[1] = new DynamicStepCounter(3.0);
        movAvgStepCounter[2] = new DynamicStepCounter(4.0);
        movAvgStepCounter[3] = new DynamicStepCounter(6.0);
        movAvgStepCounter[4] = new DynamicStepCounter(7.0);

        clearCounters();

        //launches when the start button is pressed, and activates the sensors
        findViewById(R.id.buttonStartCounter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                thresholdStepCounter[0].setThresholds(upperThreshold, lowerThreshold);
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
                textAndroidSteps.setText(String.valueOf(androidStepCount));
            }

        } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            final Double norm = Math.sqrt(Math.pow(event.values[0], 2) +
                                    Math.pow(event.values[1], 2) +
                                    Math.pow(event.values[2], 2));
            final Double acc = (double) event.values[2];

            //display the instantaneous acceleration to let the user know the step counter is working
            textInstantAcc.setText(String.valueOf(acc).substring(0, 5));

            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (thresholdStepCounter[0].findStep(acc)) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textThresholdSteps.setText(String.valueOf(thresholdStepCounter[0].getStepCount()));
                            }
                        });

                    }
                }
            }).start();

//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    if (movAvgStepCounter[0].findStep(acc)) {
//
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                textMovingAvgSteps.setText(String.valueOf(movAvgStepCounter[0].getStepCount()));
//                            }
//                        });
//                    }
//                }
//            }).start();

            //using alternate threads for the other step counters to minimize load on UI Thread
            new Thread(new Runnable() {
                @Override
                public void run() {
                    thresholdStepCounter[1].findStep(acc);
                    thresholdStepCounter[2].findStep(acc);
                    thresholdStepCounter[3].findStep(acc);
                    thresholdStepCounter[4].findStep(acc);
                }
            }).start();
//
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    if (movAvgStepCounter[1].findStep(acc)) movingAvgStepCount[1]++;
//                    if (movAvgStepCounter[2].findStep(acc)) movingAvgStepCount[2]++;
//                    if (movAvgStepCounter[3].findStep(acc)) movingAvgStepCount[3]++;
//                    if (movAvgStepCounter[4].findStep(acc)) movingAvgStepCount[4]++;
//                }
//            }).start();

        }


    }

    //setting thresholds for the StaticStepCounter (this method is called by SetThresholdsActivity)
    public static void setThresholds(double upper, double lower) {
        upperThreshold = upper;
        lowerThreshold = lower;
    }

    private String getDialogMessage() {

        String message;

        String t1 = String.format("T(%.1f, %.1f) = %d", thresholdStepCounter[1].getUpperThreshold(), thresholdStepCounter[1].getLowerThreshold(), thresholdStepCounter[1].getStepCount());
        String t2 = String.format("T(%.1f, %.1f) = %d", thresholdStepCounter[2].getUpperThreshold(), thresholdStepCounter[2].getLowerThreshold(), thresholdStepCounter[2].getStepCount());
        String t3 = String.format("T(%.1f, %.1f) = %d", thresholdStepCounter[3].getUpperThreshold(), thresholdStepCounter[3].getLowerThreshold(), thresholdStepCounter[3].getStepCount());
        String t4 = String.format("T(%.1f, %.1f) = %d", thresholdStepCounter[4].getUpperThreshold(), thresholdStepCounter[4].getLowerThreshold(), thresholdStepCounter[4].getStepCount());

        String a1 = String.format("A(%.1f) = %d", movAvgStepCounter[1].getSensitivity(), movAvgStepCounter[1].getStepCount());
        String a2 = String.format("A(%.1f) = %d", movAvgStepCounter[2].getSensitivity(), movAvgStepCounter[2].getStepCount());
        String a3 = String.format("A(%.1f) = %d", movAvgStepCounter[3].getSensitivity(), movAvgStepCounter[3].getStepCount());
        String a4 = String.format("A(%.1f) = %d", movAvgStepCounter[4].getSensitivity(), movAvgStepCounter[4].getStepCount());

        message = t1 + "\n" + t2 + "\n" + t3 + "\n" + t4 + "\n" + "\n" + a1 + "\n" + a2 + "\n" + a3 + "\n" + a4;

        return message;
    }

    private void clearCounters() {

        textThresholdSteps.setText("0");
        textMovingAvgSteps.setText("0");
        textAndroidSteps.setText("0");
        textInstantAcc.setText("0");

        androidStepCount = 0;

        for (StaticStepCounter aThresholdStepCounter : thresholdStepCounter) {
            aThresholdStepCounter.clearStepCount();
        }

        for (DynamicStepCounter aMovAvgStepCounter : movAvgStepCounter) {
            aMovAvgStepCounter.clearStepCount();
        }

    }

}
