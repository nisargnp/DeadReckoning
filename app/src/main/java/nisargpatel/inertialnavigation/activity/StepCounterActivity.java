package nisargpatel.inertialnavigation.activity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import nisargpatel.inertialnavigation.R;
import nisargpatel.inertialnavigation.dialog.StepInfoFragment;
import nisargpatel.inertialnavigation.stepcounter.MovingAverageStepCounter;
import nisargpatel.inertialnavigation.stepcounter.ThresholdStepCounter;

public class StepCounterActivity extends ActionBarActivity implements SensorEventListener{

    private double strideLength;
    private static double upperThreshold = 11.5;
    private static double lowerThreshold = 6.5;

    //android views
    private int thresholdStepCount1 = 0;
    private int thresholdStepCount2 = 0;
    private int thresholdStepCount3 = 0;
    private int thresholdStepCount4 = 0;
    private int movingAverageStepCount1 = 0;
    private int movingAverageStepCount2 = 0;
    private int movingAverageStepCount3 = 0;
    private int movingAverageStepCount4 = 0;

    private int thresholdStepCount = 0;
    private int movingAverageStepCount = 0;
    private int androidStepCount = 0;

    //handling Android views
    private TextView textThresholdSteps;
    private TextView textMovingAverageSteps;
    private TextView textAndroidSteps;
    private TextView textInstantAcc;
    private TextView textDistance;

    private StepInfoFragment myDialog;

    //declaring sensors
    private Sensor sensorAccelerometer;
    private Sensor sensorStepDetector;
    private SensorManager sensorManager;

    //instantiating step counters
    ThresholdStepCounter thresholdCountSteps1 = new ThresholdStepCounter(10, 8);
    ThresholdStepCounter thresholdCountSteps2 = new ThresholdStepCounter(11, 7);
    ThresholdStepCounter thresholdCountSteps3 = new ThresholdStepCounter(12, 6);
    ThresholdStepCounter thresholdCountSteps4 = new ThresholdStepCounter(13, 5);
    MovingAverageStepCounter movingAverageCountSteps1 = new MovingAverageStepCounter(0.5);
    MovingAverageStepCounter movingAverageCountSteps2 = new MovingAverageStepCounter(1.0);
    MovingAverageStepCounter movingAverageCountSteps3 = new MovingAverageStepCounter(1.5);
    MovingAverageStepCounter movingAverageCountSteps4 = new MovingAverageStepCounter(2.0);

    ThresholdStepCounter thresholdCountSteps = new ThresholdStepCounter(upperThreshold, lowerThreshold);
    MovingAverageStepCounter movingAverageCountSteps = new MovingAverageStepCounter(.75);

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_counter);

        final String PREFS_NAME = "Inertial Navigation Preferences";
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, 0);
        strideLength = sharedPreferences.getFloat("stride_length", 2.0f);

        //declaring all the views
        textThresholdSteps = (TextView) findViewById(R.id.textThreshold);
        textMovingAverageSteps = (TextView) findViewById(R.id.textMovingAverage);
        textAndroidSteps = (TextView) findViewById(R.id.textAndroid);
        textInstantAcc = (TextView) findViewById(R.id.textInstantAcc);
        textDistance = (TextView) findViewById(R.id.textDistance);

        myDialog = new StepInfoFragment();

        //defining sensors
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorStepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        //launches when the start button is pressed, and activates the sensors
        findViewById(R.id.buttonStartCounter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorManager.registerListener(StepCounterActivity.this, sensorAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
                sensorManager.registerListener(StepCounterActivity.this, sensorStepDetector, SensorManager.SENSOR_DELAY_FASTEST);
                thresholdCountSteps.setThresholds(upperThreshold, lowerThreshold);
                Toast.makeText(getApplicationContext(), "Step counter started.", Toast.LENGTH_SHORT).show();
            }
        });

        //launches when the stop button is pressed, and deactivates the sensors
        findViewById(R.id.buttonStopCounter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorManager.unregisterListener(StepCounterActivity.this, sensorAccelerometer);
                sensorManager.unregisterListener(StepCounterActivity.this, sensorStepDetector);
                Toast.makeText(getApplicationContext(), "Step counter stopped.", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.buttonClearCounter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textThresholdSteps.setText("0");
                textMovingAverageSteps.setText("0");
                textAndroidSteps.setText("0");
                textInstantAcc.setText("0");
                textDistance.setText("0");

                thresholdStepCount = 0;
                movingAverageStepCount = 0;
                androidStepCount = 0;

                thresholdStepCount1 = 0;
                thresholdStepCount2 = 0;
                thresholdStepCount3 = 0;
                thresholdStepCount4 = 0;
                movingAverageStepCount1 = 0;
                movingAverageStepCount2 = 0;
                movingAverageStepCount3 = 0;
                movingAverageStepCount4 = 0;
            }
        });

        findViewById(R.id.buttonStepInfo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.setDialogMessage(getMessage());
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_step_counter, menu);
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

    //this method is required to implement SensorEventListener, but is not used
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    //This only works as long as the sensor is registered
    //Registration of the sensor is controlled by the OnClick methods in OnCreate
    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onSensorChanged(SensorEvent event) {

        final SensorEvent threadEvent = event;

        //all of the calculations happen in a separate thread, so that the UI thread is not bogged down
        new Thread (new Runnable() {
            @Override
            public void run() {

                //if the data is of accelerometer type, then run it through the step counters
                if (threadEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    Double zAcc = (double) threadEvent.values[2];

                    //display the instantaneous acceleration to let the user know the step counter is working
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textInstantAcc.setText(String.valueOf(threadEvent.values[2]).substring(0, 5));
                        }
                    });

                    if (thresholdCountSteps.stepFound(zAcc)) {
                        thresholdStepCount++;

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textThresholdSteps.setText(String.valueOf(thresholdStepCount));
                            }
                        });

                    }

                    if (thresholdCountSteps1.stepFound(zAcc)) {
                        thresholdStepCount1++;
                    }
                    if (thresholdCountSteps2.stepFound(zAcc)) {
                        thresholdStepCount2++;
                    }
                    if (thresholdCountSteps3.stepFound(zAcc)) {
                        thresholdStepCount3++;
                    }
                    if (thresholdCountSteps4.stepFound(zAcc)) {
                        thresholdStepCount4++;
                    }

                    if (movingAverageCountSteps.stepFound(zAcc)) {
                        movingAverageStepCount++;

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textMovingAverageSteps.setText(String.valueOf(movingAverageStepCount));
                            }
                        });
                    }

                    if (movingAverageCountSteps1.stepFound(zAcc)) {
                        movingAverageStepCount1++;
                    }
                    if (movingAverageCountSteps2.stepFound(zAcc)) {
                        movingAverageStepCount2++;
                    }
                    if (movingAverageCountSteps3.stepFound(zAcc)) {
                        movingAverageStepCount3++;
                    }
                    if (movingAverageCountSteps4.stepFound(zAcc)) {
                        movingAverageStepCount4++;
                    }

                    //if the data is not of accelerometer type (is step counter type) then increment the stepCount by 1
                } else {
                    if (threadEvent.values[0] == 1.0) {
                        androidStepCount++;

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textAndroidSteps.setText(String.valueOf(androidStepCount));
                                textDistance.setText(String.valueOf(strideLength * androidStepCount).substring(0,3));
                            }
                        });

                    }
                }
            }
        }).start(); //starts the thread

    }

    //setting thresholds for the ThresholdStepCounter (this method is called by SetThresholdsActivity)
    public static void setThresholds(double upper, double lower) {
        upperThreshold = upper;
        lowerThreshold = lower;
    }

    private String getMessage() {

        String message;

        String t1 = "T(10,8) = " + thresholdStepCount1;
        String t2 = "T(11,7) = " + thresholdStepCount2;
        String t3 = "T(12,6) = " + thresholdStepCount3;
        String t4 = "T(13,5) = " + thresholdStepCount4;

        String a1 = "A(0.5) = " + movingAverageStepCount1;
        String a2 = "A(1.0) = " + movingAverageStepCount2;
        String a3 = "A(1.5) = " + movingAverageStepCount3;
        String a4 = "A(2.0) = " + movingAverageStepCount4;

        String newLine = "\n";

        message = t1 + newLine + t2 + newLine + t3 + newLine + t4 + newLine + newLine + a1 + newLine + a2 + newLine + a3 + newLine + a4;

        return message;
    }

}
