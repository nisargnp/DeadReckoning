package nisargpatel.inertialnavigation;

import android.content.Intent;
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

import nisargpatel.inertialnavigation.javacode.MovingAverageStepCounter;
import nisargpatel.inertialnavigation.javacode.ThresholdStepCounter;

public class MainActivity extends ActionBarActivity implements SensorEventListener{

    private Sensor accelerometer;
    private Sensor androidStepCounter;
    private SensorManager sensorManager;

    private TextView textThresholdSteps1;
    private TextView textThresholdSteps2;
    private TextView textThresholdSteps3;
    private TextView textThresholdSteps4;
    private TextView textMovingAverageSteps1;
    private TextView textMovingAverageSteps2;
    private TextView textMovingAverageSteps3;
    private TextView textMovingAverageSteps4;

    private TextView textThresholdSteps;
    private TextView textMovingAverageSteps;
    private TextView textAndroidSteps;
    private TextView textInstantAcc;

    //private static double upperThreshold;
    //private static double lowerThreshold;

    private Button buttonStartCounter;
    private Button buttonStopCounter;

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

    ThresholdStepCounter thresholdCountSteps1 = new ThresholdStepCounter(10, 8);
    ThresholdStepCounter thresholdCountSteps2 = new ThresholdStepCounter(11, 7);
    ThresholdStepCounter thresholdCountSteps3 = new ThresholdStepCounter(12, 6);
    ThresholdStepCounter thresholdCountSteps4 = new ThresholdStepCounter(13, 5);
    MovingAverageStepCounter movingAverageCountSteps1 = new MovingAverageStepCounter(0.5);
    MovingAverageStepCounter movingAverageCountSteps2 = new MovingAverageStepCounter(1.0);
    MovingAverageStepCounter movingAverageCountSteps3 = new MovingAverageStepCounter(1.5);
    MovingAverageStepCounter movingAverageCountSteps4 = new MovingAverageStepCounter(2.0);

    ThresholdStepCounter thresholdCountSteps = new ThresholdStepCounter(11.5, 6.5);
    MovingAverageStepCounter movingAverageCountSteps = new MovingAverageStepCounter(.75);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textThresholdSteps1 = (TextView) findViewById(R.id.textT1);
        textThresholdSteps2 = (TextView) findViewById(R.id.textT2);
        textThresholdSteps3 = (TextView) findViewById(R.id.textT3);
        textThresholdSteps4 = (TextView) findViewById(R.id.textT4);
        textMovingAverageSteps1 = (TextView) findViewById(R.id.textA1);
        textMovingAverageSteps2 = (TextView) findViewById(R.id.textA2);
        textMovingAverageSteps3 = (TextView) findViewById(R.id.textA3);
        textMovingAverageSteps4 = (TextView) findViewById(R.id.textA4);

        textThresholdSteps = (TextView) findViewById(R.id.textThreshold);
        textMovingAverageSteps = (TextView) findViewById(R.id.textMovingAverage);
        textAndroidSteps = (TextView) findViewById(R.id.textAndroid);
        textInstantAcc = (TextView) findViewById(R.id.textInstantAcc);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        androidStepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);



        buttonStartCounter = (Button) findViewById(R.id.buttonStartCounter);
        buttonStopCounter = (Button) findViewById(R.id.buttonStopCounter);

        buttonStartCounter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorManager.registerListener(MainActivity.this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
                sensorManager.registerListener(MainActivity.this, androidStepCounter, SensorManager.SENSOR_DELAY_UI);
            }
        });

        buttonStopCounter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorManager.unregisterListener(MainActivity.this, accelerometer);
                sensorManager.unregisterListener(MainActivity.this, androidStepCounter);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //if (id == R.id.action_settings) {
        //    return true;
        //}

        if (id == R.id.set_thresholds) {
            Intent intent = new Intent(this, SetThresholds.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    //This only works as long as the sensor is registered
    //Registration of the sensor is controlled by the OnClick methods in OnCreate
    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            Double zAcc = (double) event.values[2];

            textInstantAcc.setText(String.valueOf(event.values[2]).substring(0, 5));

            if (thresholdCountSteps.stepFound(System.currentTimeMillis(), zAcc)) {
                thresholdStepCount++;
                textThresholdSteps.setText(String.valueOf(thresholdStepCount));
            }
            if (thresholdCountSteps1.stepFound(System.currentTimeMillis(), zAcc)) {
                thresholdStepCount1++;
                textThresholdSteps1.setText(String.valueOf(thresholdStepCount1));
            }
            if (thresholdCountSteps2.stepFound(System.currentTimeMillis(), zAcc)) {
                thresholdStepCount2++;
                textThresholdSteps2.setText(String.valueOf(thresholdStepCount2));
            }
            if (thresholdCountSteps3.stepFound(System.currentTimeMillis(), zAcc)) {
                thresholdStepCount3++;
                textThresholdSteps3.setText(String.valueOf(thresholdStepCount3));
            }
            if (thresholdCountSteps4.stepFound(System.currentTimeMillis(), zAcc)) {
                thresholdStepCount4++;
                textThresholdSteps4.setText(String.valueOf(thresholdStepCount4));
            }

            if (movingAverageCountSteps.stepFound(System.currentTimeMillis(), zAcc)) {
                movingAverageStepCount++;
                textMovingAverageSteps.setText(String.valueOf(movingAverageStepCount));
            }
            if (movingAverageCountSteps1.stepFound(System.currentTimeMillis(), zAcc)) {
                movingAverageStepCount1++;
                textMovingAverageSteps1.setText(String.valueOf(movingAverageStepCount1));
            }
            if (movingAverageCountSteps2.stepFound(System.currentTimeMillis(), zAcc)) {
                movingAverageStepCount2++;
                textMovingAverageSteps2.setText(String.valueOf(movingAverageStepCount2));
            }
            if (movingAverageCountSteps3.stepFound(System.currentTimeMillis(), zAcc)) {
                movingAverageStepCount3++;
                textMovingAverageSteps3.setText(String.valueOf(movingAverageStepCount3));
            }
            if (movingAverageCountSteps4.stepFound(System.currentTimeMillis(), zAcc)) {
                movingAverageStepCount4++;
                textMovingAverageSteps4.setText(String.valueOf(movingAverageStepCount4));
            }
        } else {
            if (event.values[0] == 1.0) {
                androidStepCount++;
                textAndroidSteps.setText(String.valueOf(androidStepCount));
            }
        }

    }
    public static void setThresholds(double upper, double lower) {
        //upperThreshold = upper;
        //lowerThreshold = lower;
    }

    /*
    **decided to implement the buttons in Java code instead of XML
    *
    public void buttonStartCounter (View view) {
        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sm.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
    }

    public void buttonStopCounter (View view) {
        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sm.unregisterListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
    }
    */
}
