package nisargpatel.deadreckoning.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import nisargpatel.deadreckoning.R;
import nisargpatel.deadreckoning.extra.ExtraFunctions;
import nisargpatel.deadreckoning.filewriting.DataFileWriter;
import nisargpatel.deadreckoning.graph.ScatterPlot;
import nisargpatel.deadreckoning.heading.EulerHeadingInference;
import nisargpatel.deadreckoning.heading.GyroscopeIntegration;
import nisargpatel.deadreckoning.stepcounting.DynamicStepCounter;

public class GraphActivity extends Activity implements SensorEventListener, LocationListener{

    private static final String FOLDER_NAME = "Dead_Reckoning/Graph_Activity";
    private static final String[] DATA_FILE_NAMES = {"Linear_Acceleration", "Gyroscope_Uncalibrated", "XY_Data_Set"};
    private static final String[] DATA_FILE_HEADINGS = {"t;Ax;Ay;Az;findStep;",
                                                        "t;uGx;uGy;uGz;xBias;yBias;zBias;heading;",
                                                        "timeGPS;t;strideLength;heading;pointX;pointY;"};

    private DynamicStepCounter dynamicStepCounter;
    private GyroscopeIntegration gyroscopeIntegration;
    private EulerHeadingInference eulerHeadingInference;
    private DataFileWriter dataFileWriter;
    private ScatterPlot sPlot;

    private Button buttonStart;
    private Button buttonStop;
    private Button buttonClear;
    private LinearLayout linearLayout;

    private Sensor sensorStepDetector;
    private Sensor sensorLinearAcceleration;
    private Sensor sensorGyroscopeUncalibrated;

    private SensorManager sensorManager;
    private LocationManager locationManager;

    private float strideLength;

    private boolean filesCreated;
    private boolean wasRunning;

    private float matrixHeading;

    private boolean isCalibrated;

    long curr_GPS_time;

    float[] gyroBias;
    float[][] initialOrientation;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        curr_GPS_time = 0;

        //getting global settings
        strideLength =  getIntent().getFloatExtra("stride_length", 2.5f);
        String userName = getIntent().getStringExtra("user_name");
        gyroBias = getIntent().getFloatArrayExtra("gyroscope_bias");
        initialOrientation = (float[][]) getIntent().getSerializableExtra("initial_orientation");
        String stepCounterSensitivity = UserListActivity.preferredStepCounterList.get(UserListActivity.userList.indexOf(userName));
        isCalibrated = getIntent().getBooleanExtra("is_calibrated", false) || stepCounterSensitivity.equals("default");

        //defining views
        buttonStart = (Button) findViewById(R.id.buttonGraphStart);
        buttonStop = (Button) findViewById(R.id.buttonGraphStop);
        buttonClear = (Button) findViewById(R.id.buttonGraphClear);
        linearLayout = (LinearLayout) findViewById(R.id.linearLayoutGraph);

        //defining location
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        //defining sensors
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorStepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        sensorLinearAcceleration = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorGyroscopeUncalibrated = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED);

        //initializing needed classes
        Log.d("step_counter", "" + stepCounterSensitivity);
        Log.d("step_counter", "" + UserListActivity.preferredStepCounterList);
        dynamicStepCounter = new DynamicStepCounter(stepCounterSensitivity.equals("default") ? 0 : Double.parseDouble(stepCounterSensitivity)); //if index != "default", set it to the stepCounterSensitivity
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
                sensorManager.registerListener(GraphActivity.this, sensorGyroscopeUncalibrated, SensorManager.SENSOR_DELAY_FASTEST);
                if (isCalibrated)
                    sensorManager.registerListener(GraphActivity.this, sensorStepDetector, SensorManager.SENSOR_DELAY_FASTEST);
                else
                    sensorManager.registerListener(GraphActivity.this, sensorLinearAcceleration, SensorManager.SENSOR_DELAY_FASTEST);

                //registers the gps to start tracking location
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, GraphActivity.this);

                Toast.makeText(getApplicationContext(), "Tracking started.", Toast.LENGTH_SHORT).show();

                if (!filesCreated) {
                    try {
                        dataFileWriter = new DataFileWriter(FOLDER_NAME, ExtraFunctions.arrayToList(DATA_FILE_NAMES), ExtraFunctions.arrayToList(DATA_FILE_HEADINGS));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    filesCreated = true;
                }

                dataFileWriter.writeToFile("XY_Data_Set", "Initial_orientation: " + Arrays.deepToString(initialOrientation));
                dataFileWriter.writeToFile("Gyroscope_Uncalibrated", "Gyroscope_bias: " + Arrays.toString(gyroBias));

                buttonStart.setEnabled(false);
                buttonStop.setEnabled(true);

                wasRunning = true;

            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorManager.unregisterListener(GraphActivity.this);

                Toast.makeText(getApplicationContext(), "Tracking stopped.", Toast.LENGTH_SHORT).show();

                buttonStart.setEnabled(true);
                buttonStop.setEnabled(false);

                wasRunning = false;
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
    protected void onStop() {
        super.onStop();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (wasRunning) {
            sensorManager.registerListener(GraphActivity.this, sensorGyroscopeUncalibrated, SensorManager.SENSOR_DELAY_FASTEST);
            if (isCalibrated)
                sensorManager.registerListener(GraphActivity.this, sensorStepDetector, SensorManager.SENSOR_DELAY_FASTEST);
            else
                sensorManager.registerListener(GraphActivity.this, sensorLinearAcceleration, SensorManager.SENSOR_DELAY_FASTEST);

            buttonStart.setEnabled(false);
            buttonStop.setEnabled(true);
            buttonClear.setEnabled(true);
        } else {
            buttonStart.setEnabled(true);
            buttonStop.setEnabled(false);
            buttonClear.setEnabled(true);
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE_UNCALIBRATED) {

            float[] deltaOrientation = gyroscopeIntegration.getIntegratedValues(event.timestamp, event.values);
            matrixHeading = eulerHeadingInference.getCurrentHeading(deltaOrientation);

            //saving gyroscope data
            ArrayList<Float> dataValues = ExtraFunctions.arrayToList(new float[] {event.values[0], event.values[1], event.values[2]});
            dataValues.add(0, (float) event.timestamp);
            dataValues.add(matrixHeading);

            dataFileWriter.writeToFile("Gyroscope_Uncalibrated", dataValues);

        } else if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {

            float norm = (float) Math.sqrt(Math.pow(event.values[0], 2) + Math.pow(event.values[1], 2) + Math.pow(event.values[2], 2));

            //if step is found, findStep == true
            boolean stepFound = dynamicStepCounter.findStep(norm);

            if (stepFound) {

                //saving linear acceleration data
                ArrayList<Float> dataValues = ExtraFunctions.arrayToList(event.values);
                dataValues.add(0, (float) event.timestamp);
                dataValues.add(1f);
                dataFileWriter.writeToFile("Linear_Acceleration", dataValues);

                //rotation heading output by 90 degrees (pi/2)
//                float heading = matrixHeading + (float) (Math.PI / 2.0);
                float heading = matrixHeading;
                double pointX = ExtraFunctions.getXFromPolar(strideLength, heading);
                double pointY = ExtraFunctions.getYFromPolar(strideLength, heading);

                pointX += sPlot.getLastXPoint();
                pointY += sPlot.getLastYPoint();
                sPlot.addPoint(pointX, pointY);

                //saving XY location data
                dataValues = ExtraFunctions.createList((float)curr_GPS_time, (float)event.timestamp,
                        strideLength, matrixHeading, (float)pointX, (float)pointY);

                dataFileWriter.writeToFile("XY_Data_Set", dataValues);

                linearLayout.removeAllViews();
                linearLayout.addView(sPlot.getGraphView(getApplicationContext()));

                //if step is not found
            } else {
                //saving linear acceleration data
                ArrayList<Float> dataValues = ExtraFunctions.arrayToList(event.values);
                dataValues.add(0, (float) event.timestamp);
                dataValues.add(0f);
                dataFileWriter.writeToFile("Linear_Acceleration", dataValues);
            }

        } else if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {

            boolean stepFound = (event.values[0] == 1);

            if (stepFound) {

                //rotation heading output by 90 degrees (pi/2)
                //so that moving straight forward is represented by moving directly "up" (90 degrees) on the map
//                float heading = matrixHeading + (float) (Math.PI / 2.0);
                float heading = matrixHeading;
                double pointX = ExtraFunctions.getXFromPolar(strideLength, heading);
                double pointY = ExtraFunctions.getYFromPolar(strideLength, heading);

                pointX += sPlot.getLastXPoint();
                pointY += sPlot.getLastYPoint();
                sPlot.addPoint(pointX, pointY);

                //saving XY location data
                ArrayList<Float> dataValues = ExtraFunctions.createList((float)curr_GPS_time, (float)event.timestamp,
                        strideLength, matrixHeading, (float)pointX, (float)pointY);

                dataFileWriter.writeToFile("XY_Data_Set", dataValues);

                linearLayout.removeAllViews();
                linearLayout.addView(sPlot.getGraphView(getApplicationContext()));
            }

        }
    }



    @Override
    public void onLocationChanged(Location location) {
        Log.d("location", "" + location.getTime());
        curr_GPS_time = location.getTime();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onProviderDisabled(String provider) {}

}
