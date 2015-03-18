package nisargpatel.inertialnavigation.activity;

import android.annotation.TargetApi;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import nisargpatel.inertialnavigation.R;
import nisargpatel.inertialnavigation.extra.ExtraFunctions;
import nisargpatel.inertialnavigation.filewriting.DataFileWriter;
import nisargpatel.inertialnavigation.graph.ScatterPlot;
import nisargpatel.inertialnavigation.heading.EulerHeadingInference;
import nisargpatel.inertialnavigation.heading.GyroscopeIntegration;
import nisargpatel.inertialnavigation.stepcounting.StaticStepCounter;

public class GraphActivity extends ActionBarActivity implements SensorEventListener{

    private static final double STEP_COUNTER_SENSITIVITY = 1.0;
    private static final double UPPER_THRESHOLD = 11.5;
    private static final double LOWER_THRESHOLD = 6.5;

    private static final String FOLDER_NAME = "Inertial_Navigation_Data/Graph_Activity";
    private static final String[] DATA_FILE_NAMES = {"Accelerometer", "Gyroscope-Uncalibrated", "XY-Data-Set"};
    private static final String[] DATA_FILE_HEADINGS = {"t;Ax;Ay;Az;findStep;",
                                                        "t;uGx;uGy;uGz;xBias;yBias;zBias;heading;",
                                                        "t;strideLength;heading;pointX;pointY;"};

    private StaticStepCounter thresholdStepCounter;
    private GyroscopeIntegration gyroscopeIntegration;
    private EulerHeadingInference eulerHeadingInference;
    private DataFileWriter dataFileWriter;
    private ScatterPlot sPlot;

    private LinearLayout linearLayout;

    private Sensor sensorAccelerometer;
    private Sensor sensorGyroscopeUncalibrated;
    private SensorManager sensorManager;

    private float strideLength;

    private boolean filesCreated;

    private float matrixHeading;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        //getting global settings
        strideLength =  getIntent().getFloatExtra("stride_length", 2.5f);
        String userName = getIntent().getStringExtra("user_name");
        float[] gyroBias = getIntent().getFloatArrayExtra("gyroscope_bias");
        float[][] initialOrientation = ExtraFunctions.stringArrayToFloatArray((String[][]) getIntent().getSerializableExtra("initial_orientation")); //converting from Serialized to String[][] to float[][]

        //defining views
        final Button buttonStart = (Button) findViewById(R.id.buttonGraphStart);
        final Button buttonStop = (Button) findViewById(R.id.buttonGraphStop);
        Button buttonClear = (Button) findViewById(R.id.buttonGraphClear);
        linearLayout = (LinearLayout) findViewById(R.id.linearLayoutGraph);

        //defining sensors
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorGyroscopeUncalibrated = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED);

        //initializing needed classes
        thresholdStepCounter = new StaticStepCounter(UPPER_THRESHOLD, LOWER_THRESHOLD);
        gyroscopeIntegration = new GyroscopeIntegration(0.0025f, gyroBias);
        eulerHeadingInference = new EulerHeadingInference(initialOrientation);

        //setting up graph with origin
        sPlot = new ScatterPlot("Position");
        sPlot.addPoint(0, 0);
        linearLayout.addView(sPlot.getGraphView(getApplicationContext()));

        //initializing needed variables
        filesCreated = false;
        matrixHeading = 0;

        Toast.makeText(GraphActivity.this, "user: " + userName + "\n" + "stride length: " + strideLength, Toast.LENGTH_SHORT).show();

        //setting up buttons
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorManager.registerListener(GraphActivity.this, sensorAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
                sensorManager.registerListener(GraphActivity.this, sensorGyroscopeUncalibrated, SensorManager.SENSOR_DELAY_FASTEST);

                Toast.makeText(getApplicationContext(), "Tracking started.", Toast.LENGTH_SHORT).show();

                if (!filesCreated) {
                    try {
                        dataFileWriter = new DataFileWriter(FOLDER_NAME, ExtraFunctions.arrayToList(DATA_FILE_NAMES), ExtraFunctions.arrayToList(DATA_FILE_HEADINGS));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                buttonStart.setEnabled(false);
                buttonStop.setEnabled(true);

            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorManager.unregisterListener(GraphActivity.this, sensorAccelerometer);
                sensorManager.unregisterListener(GraphActivity.this, sensorGyroscopeUncalibrated);

                Toast.makeText(getApplicationContext(), "Tracking stopped.", Toast.LENGTH_SHORT).show();

                buttonStart.setEnabled(true);
                buttonStop.setEnabled(false);
            }
        });

        buttonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eulerHeadingInference.clearMatrix();

                sPlot.clearSet();
                sPlot.addPoint(0,0);
                linearLayout.removeAllViews();
                linearLayout.addView(sPlot.getGraphView(getApplicationContext()));
            }
        });

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE_UNCALIBRATED) {

            float[] deltaOrientation = gyroscopeIntegration.getIntegratedValues(event.timestamp, event.values);
            matrixHeading = eulerHeadingInference.getCurrentHeading(deltaOrientation);

            ArrayList<Float> dataValues = ExtraFunctions.arrayToList(event.values);
            dataValues.add(0, (float) event.timestamp);
            dataValues.add(matrixHeading);

            dataFileWriter.writeToFile("Gyroscope-Uncalibrated", dataValues);

        } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            float zAcc = event.values[2];

            //if step is found, findStep == true
            boolean stepFound = thresholdStepCounter.findStep(zAcc);

            if (stepFound) {

                ArrayList<Float> dataValues = ExtraFunctions.arrayToList(event.values);
                dataValues.add(0, (float) event.timestamp);
                dataValues.add(1f);
                dataFileWriter.writeToFile("Accelerometer", dataValues);

                //rotation heading output by 90 degrees (pi/2)
                float heading = matrixHeading + (float) (Math.PI / 2.0);
                double pointX = ExtraFunctions.getXFromPolar(strideLength, heading);
                double pointY = ExtraFunctions.getYFromPolar(strideLength, heading);

                pointX += sPlot.getLastXPoint();
                pointY += sPlot.getLastYPoint();
                sPlot.addPoint(pointX, pointY);

                dataValues.clear();
                dataValues.add(strideLength);
                dataValues.add(matrixHeading);
                dataValues.add((float)pointX);
                dataValues.add((float)pointY);

                dataFileWriter.writeToFile("XY-Data-Set", dataValues);

                linearLayout.removeAllViews();
                linearLayout.addView(sPlot.getGraphView(getApplicationContext()));

                //if step is not found
            } else {
                ArrayList<Float> dataValues = ExtraFunctions.arrayToList(event.values);
                dataValues.add(0, (float) event.timestamp);
                dataValues.add(0f);
                dataFileWriter.writeToFile("Accelerometer", dataValues);
            }

        }
    }

}
