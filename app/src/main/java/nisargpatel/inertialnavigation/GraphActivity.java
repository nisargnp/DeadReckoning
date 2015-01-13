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
import android.widget.Toast;

import nisargpatel.inertialnavigation.graph.ScatterPlot;
import nisargpatel.inertialnavigation.heading.HeadingInference;

public class GraphActivity extends ActionBarActivity implements SensorEventListener{

    private HeadingInference headingInference;

    private Sensor sensorStepDetector;
    //private Sensor sensorAccelerometer;
    private Sensor sensorGyroscope;
    private SensorManager sensorManager;

    private Button buttonStart;
    private Button buttonStop;

    private LinearLayout linearLayout;

    private ScatterPlot sPlot;

    private long recordedTime;

    private static final int STRIDE_LENGTH = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        double gyroInput[] = {-2900, -1450, 0, 1450, 2900};
        double radianInput[] = {-90, 0, 90, 180, 270};

        headingInference = new HeadingInference(gyroInput, radianInput);

        sPlot = new ScatterPlot("Position");

        buttonStart = (Button) findViewById(R.id.buttonGraphStart);
        buttonStop = (Button) findViewById(R.id.buttonGraphStop);
        linearLayout = (LinearLayout) findViewById(R.id.linearLayoutGraph);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorStepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        //sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorGyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        //setting up graph with origin
        sPlot.addPoint(0, 0);
        linearLayout.addView(sPlot.getGraphView(getApplicationContext()));

        //buttons
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorManager.registerListener(GraphActivity.this, sensorStepDetector, SensorManager.SENSOR_DELAY_FASTEST);
                sensorManager.registerListener(GraphActivity.this, sensorGyroscope, SensorManager.SENSOR_DELAY_FASTEST);
                recordedTime = System.currentTimeMillis();
                Toast.makeText(getApplicationContext(), "Step counter started.", Toast.LENGTH_SHORT).show();
            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorManager.unregisterListener(GraphActivity.this, sensorStepDetector);
                sensorManager.unregisterListener(GraphActivity.this, sensorGyroscope);
                Toast.makeText(getApplicationContext(), "Step counter stopped.", Toast.LENGTH_SHORT).show();
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

    private double currentGyroValue;
    private double averageGyroValue;
    private double totalGyroValue;

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {

            currentGyroValue = (System.currentTimeMillis() - recordedTime) * event.values[2];

            if (currentGyroValue > averageGyroValue * 100000 ) {
                totalGyroValue += currentGyroValue;
            } else {
                averageGyroValue = (averageGyroValue + currentGyroValue) / 2;
            }

            recordedTime = System.currentTimeMillis();

        } else if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {

            boolean stepFound = false;
            if (event.values[0] == 1.0)
                stepFound = true;

            if (stepFound) {

//                stepCount++;
//                double currentDistance = stepCount * STRIDE_LENGTH;
//                headingInference.calcDegrees(totalGyroValue);
//                double pointX = headingInference.getXPoint(currentDistance);
//                double pointY = headingInference.getYPoint(currentDistance);
//                sPlot.addPoint(pointX, pointY );

                headingInference.calcDegrees(totalGyroValue);
                double pointX = headingInference.getXPoint(STRIDE_LENGTH);
                double pointY = headingInference.getYPoint(STRIDE_LENGTH);

                sPlot.addPoint(sPlot.getLastXPoint() + pointX, sPlot.getLastYPoint() + pointY);

//                linearLayout.invalidate();
                linearLayout.removeAllViews();
                linearLayout.addView(sPlot.getGraphView(getApplicationContext()));

                //debug information
//                System.out.println("----------------------------");
//                System.out.println("gyroInput: " + totalGyroValue);
//                System.out.println("degree: " + headingInference.getDegree());
//                System.out.println("normal (x,y): " + "(" + pointX + "," + pointY + ")");

            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
