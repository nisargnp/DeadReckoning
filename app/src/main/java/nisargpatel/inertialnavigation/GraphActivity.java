package nisargpatel.inertialnavigation;

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
import android.widget.LinearLayout;

import nisargpatel.inertialnavigation.graph.ScatterPlot;

public class GraphActivity extends ActionBarActivity implements SensorEventListener{

    private double orientation;
    private int stepCount;

    private Sensor stepSensor;
    private Sensor orientationSensor;
    private SensorManager sensorManager;

    private Button buttonStart;
    private Button buttonStop;

    private LinearLayout linearLayout;

    private ScatterPlot sPlot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        //orientation is just an arbitrary value for now
        orientation = 0.75;

        stepCount = 0;

        sPlot = new ScatterPlot("Position");

        buttonStart = (Button) findViewById(R.id.buttonGraphStart);
        buttonStop = (Button) findViewById(R.id.buttonGraphStop);
        linearLayout = (LinearLayout) findViewById(R.id.linearLayoutGraph);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorManager.registerListener(GraphActivity.this, stepSensor, SensorManager.SENSOR_DELAY_FASTEST);
            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorManager.unregisterListener(GraphActivity.this, stepSensor);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_graph, menu);
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

        boolean stepFound = false;

        if (event.values[0] == 1.0)
            stepFound = true;

        if (stepFound) {
            stepCount++;
            orientation += 0.75;
            sPlot.addPoint(stepCount, orientation);
        }

        linearLayout.removeAllViews();
        linearLayout.addView(sPlot.getGraphView(getApplicationContext()));

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
