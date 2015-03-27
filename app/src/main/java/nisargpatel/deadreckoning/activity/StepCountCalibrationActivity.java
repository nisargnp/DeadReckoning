package nisargpatel.deadreckoning.activity;

import android.app.Service;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import nisargpatel.deadreckoning.R;
import nisargpatel.deadreckoning.stepcounting.DynamicStepCounter;

public class StepCountCalibrationActivity extends ActionBarActivity implements SensorEventListener{

    Sensor sensorLinearAcceleration;
    SensorManager sensorManager;

    DynamicStepCounter[] dynamicStepCounters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_count_calibration);

        dynamicStepCounters = new DynamicStepCounter[20];

        double sensitivity = 0.05;
        for (DynamicStepCounter dynamicStepCounter : dynamicStepCounters) {
            dynamicStepCounter = new DynamicStepCounter(sensitivity);
            sensitivity += 0.05;
        }

        sensorManager = (SensorManager) getSystemService(Service.SENSOR_SERVICE);
        sensorLinearAcceleration = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        findViewById(R.id.buttonStepCStart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorManager.registerListener(StepCountCalibrationActivity.this, sensorLinearAcceleration, SensorManager.SENSOR_DELAY_FASTEST);
            }
        });

        findViewById(R.id.buttonStepCStop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorManager.unregisterListener(StepCountCalibrationActivity.this);
            }
        });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_step_count_calibration, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onSensorChanged(SensorEvent event) {

        double norm = Math.sqrt(Math.pow(event.values[0], 2) + Math.pow(event.values[1], 2) + Math.pow(event.values[2], 2));

        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {

            for(DynamicStepCounter dynamicStepCounter: dynamicStepCounters)
                dynamicStepCounter.findStep(norm);

        }

    }


}
