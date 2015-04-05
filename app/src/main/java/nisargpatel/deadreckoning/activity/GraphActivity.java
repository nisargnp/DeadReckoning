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
import nisargpatel.deadreckoning.orientation.GyroIntegration;
import nisargpatel.deadreckoning.orientation.GyroscopeEulerOrientation;
import nisargpatel.deadreckoning.orientation.MagneticFieldOrientation;
import nisargpatel.deadreckoning.stepcounting.DynamicStepCounter;

public class GraphActivity extends Activity implements SensorEventListener, LocationListener{

    //according to Google
//    private static final long SECONDS_PER_WEEK = 604800;

    //according to NovAtel
    private static final long SECONDS_PER_WEEK = 511200;

    private static final String FOLDER_NAME = "Dead_Reckoning/Graph_Activity";
    private static final String[] DATA_FILE_NAMES = {
            "Initial_Orientation",
            "Linear_Acceleration",
            "Gyroscope_Uncalibrated",
            "Magnetic_Field_Uncalibrated",
            "Gravity",
            "XY_Data_Set"};
    private static final String[] DATA_FILE_HEADINGS = {
            "Initial_Orientation",
            "t;Ax;Ay;Az;findStep;",
            "t;uGx;uGy;uGz;xBias;yBias;zBias;heading;",
            "t;uMx;uMy;uMz;xBias;yBias;zBias;heading;",
            "t;gx,gy,gz",
            "weeksGPS;secondsGPS;t;strideLength;magHeading;gyroHeading;originalPointX;originalPointY;rotatedPointX;rotatedPointY"};

    private DynamicStepCounter dynamicStepCounter;
    private GyroIntegration gyroIntegration;
    private GyroscopeEulerOrientation gyroscopeEulerOrientation;
    private DataFileWriter dataFileWriter;
    private ScatterPlot scatterPlot;

    private Button buttonStart;
    private Button buttonStop;
    private Button buttonAddPoint;
    private LinearLayout linearLayout;

    private SensorManager sensorManager;
    private LocationManager locationManager;

    float[] gyroBias;
    float[] magBias;
    float[] currGravity; //current gravity
    float[] currMag; //current magnetic field
//    float[][] initialOrientation;

    private boolean isRunning;
    private boolean isCalibrated;
    private boolean areFilesCreated;
    private float strideLength;
    private float gyroHeading;
    private float magHeading;
    private float weeksGPS;
    private float secondsGPS;

    private long startingTime;
    private boolean firstRun;

    private float initialHeading;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        //initializing needed variables
        gyroBias = magBias = currGravity = currMag = null;
//        initialOrientation = ExtraFunctions.IDENTITY_MATRIX;
        isRunning = isCalibrated = areFilesCreated = false;
        strideLength = 0;
        gyroHeading = magHeading = 0;
        weeksGPS = 0;
        secondsGPS = 0;
        startingTime = 0;
        firstRun = true;
        initialHeading = 0;

        //getting global settings
        strideLength =  getIntent().getFloatExtra("stride_length", 2.5f);
        String userName = getIntent().getStringExtra("user_name");

        gyroBias = getIntent().getFloatArrayExtra("gyro_bias");
        magBias = getIntent().getFloatArrayExtra("mag_bias");

        //todo: check in SharedPreferences if TYPE_STEP_DETECTOR is available

        String stepCounterSensitivity = UserListActivity.preferredStepCounterList
                .get(UserListActivity.userList.indexOf(userName));
        isCalibrated = getIntent().getBooleanExtra("is_calibrated", false) ||
                stepCounterSensitivity.equals("default");

        //initializing needed classes
        dynamicStepCounter = new DynamicStepCounter(stepCounterSensitivity.equals("default") ? 0 :
                Double.parseDouble(stepCounterSensitivity)); //if sensitivity != "default", set it to the stepCounterSensitivity
        gyroIntegration = new GyroIntegration(0.0025f, gyroBias);

        //defining views
        buttonStart = (Button) findViewById(R.id.buttonGraphStart);
        buttonStop = (Button) findViewById(R.id.buttonGraphStop);
        buttonAddPoint = (Button) findViewById(R.id.buttonGraphClear);
        linearLayout = (LinearLayout) findViewById(R.id.linearLayoutGraph);

        //setting up graph with origin
        scatterPlot = new ScatterPlot("Position");
        scatterPlot.addPoint(0, 0);
        linearLayout.addView(scatterPlot.getGraphView(getApplicationContext()));

        //message user w/ user_name and stride_length info
        Toast.makeText(GraphActivity.this,
                "user: " + userName + "\n" + "stride length: " + strideLength, Toast.LENGTH_SHORT).show();

        //starting GPS location tracking
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, GraphActivity.this);

        //starting sensors
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(GraphActivity.this,
                sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),
                SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(GraphActivity.this,
                sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED),
                SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(GraphActivity.this,
                sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED),
                SensorManager.SENSOR_DELAY_FASTEST);
        if (isCalibrated)
            sensorManager.registerListener(GraphActivity.this,
                    sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR),
                    SensorManager.SENSOR_DELAY_FASTEST);
        else
            sensorManager.registerListener(GraphActivity.this,
                    sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
                    SensorManager.SENSOR_DELAY_FASTEST);

        //setting up buttons
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                isRunning = true;

                createFiles();

                float[][] initialOrientation = MagneticFieldOrientation.calcOrientation(currGravity, currMag, magBias);

                initialHeading = (float)Math.atan2(initialOrientation[1][0], initialOrientation[0][0]);
                initialHeading = ExtraFunctions.polarShiftMinusHalfPI(initialHeading);
                initialHeading = -initialHeading; //switching from clockwise to counter-clockwise

                //saving initial orientation data
                dataFileWriter.writeToFile("Initial_Orientation", "initGravity: " + Arrays.toString(currGravity));
                dataFileWriter.writeToFile("Initial_Orientation", "initMag: " + Arrays.toString(currMag));
                dataFileWriter.writeToFile("Initial_Orientation", "magBias: " + Arrays.toString(magBias));
                dataFileWriter.writeToFile("Initial_Orientation", "initOrientation: " + Arrays.deepToString(initialOrientation));
                dataFileWriter.writeToFile("Initial_Orientation", "initHeading: " + initialHeading);

                Log.d("init_heading", "" + initialHeading);

                //TODO: fix rotation matrix
//                gyroscopeEulerOrientation = new GyroscopeEulerOrientation(initialOrientation);
                gyroscopeEulerOrientation = new GyroscopeEulerOrientation(ExtraFunctions.IDENTITY_MATRIX);

                dataFileWriter.writeToFile("XY_Data_Set", "Initial_orientation: " +
                        Arrays.deepToString(initialOrientation));
                dataFileWriter.writeToFile("Gyroscope_Uncalibrated", "Gyroscope_bias: " +
                        Arrays.toString(gyroBias));
                dataFileWriter.writeToFile("Magnetic_Field_Uncalibrated", "Magnetic_field_bias:" +
                        Arrays.toString(magBias));

                buttonStart.setEnabled(false);
                buttonStart.setEnabled(true);
                buttonStop.setEnabled(true);

            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                firstRun = true;
                isRunning = false;

                buttonStart.setEnabled(true);
                buttonStart.setEnabled(true);
                buttonStop.setEnabled(false);

            }
        });

        buttonAddPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //complimentary filter
                float compHeading = ExtraFunctions.calcCompHeading(magHeading, gyroHeading);

                Log.d("comp_heading", "" + compHeading);

                //getting and rotating the previous XY points so North 0 on unit circle
                float oPointX = scatterPlot.getLastYPoint();
                float oPointY = -scatterPlot.getLastXPoint();

                //calculating XY points from heading and stride_length
                oPointX += ExtraFunctions.getXFromPolar(strideLength, compHeading);
                oPointY += ExtraFunctions.getYFromPolar(strideLength, compHeading);

                //rotating points by 90 degrees, so north is up
                float rPointX = -oPointY;
                float rPointY = oPointX;

                scatterPlot.addPoint(rPointX, rPointY);

                linearLayout.removeAllViews();
                linearLayout.addView(scatterPlot.getGraphView(getApplicationContext()));

            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        sensorManager.unregisterListener(this);
        locationManager.removeUpdates(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isRunning) {
            sensorManager.registerListener(GraphActivity.this,
                    sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED),
                    SensorManager.SENSOR_DELAY_FASTEST);
            sensorManager.registerListener(GraphActivity.this,
                    sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED),
                    SensorManager.SENSOR_DELAY_FASTEST);
            if (isCalibrated)
                sensorManager.registerListener(GraphActivity.this,
                        sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR),
                        SensorManager.SENSOR_DELAY_FASTEST);
            else
                sensorManager.registerListener(GraphActivity.this,
                        sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
                        SensorManager.SENSOR_DELAY_FASTEST);

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, GraphActivity.this);

            buttonStart.setEnabled(false);
            buttonStart.setEnabled(true);
            buttonStop.setEnabled(true);
        } else {
            buttonStart.setEnabled(true);
            buttonStart.setEnabled(true);
            buttonStop.setEnabled(false);
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onSensorChanged(SensorEvent event) {

        if(firstRun) {
            startingTime = event.timestamp;
            firstRun = false;
        }

        if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
            currGravity = event.values;
            Log.d("gravity_values", Arrays.toString(event.values));
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED) {
            currMag = event.values;
            Log.d("mag_values", Arrays.toString(event.values));
        }

        if (isRunning) {
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED) {

                float[][] magOrientation = MagneticFieldOrientation.calcOrientation(currGravity, currMag, magBias);
                magHeading = (float)Math.atan2(magOrientation[1][0], magOrientation[0][0]);

                //shifting heading by pi/2 to get 0 to align w/ North instead of West
                magHeading = ExtraFunctions.polarShiftMinusHalfPI(magHeading);
                magHeading = -magHeading; //switching from clockwise to counter-clockwise

                Log.d("mag_heading", "" + magHeading);

                //saving magnetic field data
                ArrayList<Float> dataValues = ExtraFunctions.createList(
                        event.values[0], event.values[1], event.values[2],
                        magBias[0], magBias[1], magBias[2]
                );
                dataValues.add(0, (float)(event.timestamp - startingTime));
                dataValues.add(gyroHeading);
                dataFileWriter.writeToFile("Magnetic_Field_Uncalibrated", dataValues);

            } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE_UNCALIBRATED) {

                float[] deltaOrientation = gyroIntegration.getIntegratedValues(event.timestamp, event.values);

                gyroHeading = gyroscopeEulerOrientation.getCurrentHeading(deltaOrientation);
                gyroHeading += initialHeading;

                Log.d("gyro_heading", "" + gyroHeading);

                //saving gyroscope data
                ArrayList<Float> dataValues = ExtraFunctions.createList(
                        event.values[0], event.values[1], event.values[2],
                        gyroBias[0], gyroBias[1], gyroBias[2]
                );
                dataValues.add(0, (float)(event.timestamp - startingTime));
                dataValues.add(gyroHeading);
                dataFileWriter.writeToFile("Gyroscope_Uncalibrated", dataValues);

            }else if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {

                float norm = (float) Math.sqrt(Math.pow(event.values[0], 2) +
                        Math.pow(event.values[1], 2) +
                        Math.pow(event.values[2], 2));

                //if step is found, findStep == true
                boolean stepFound = dynamicStepCounter.findStep(norm);

                if (stepFound) {

                    //saving linear acceleration data
                    ArrayList<Float> dataValues = ExtraFunctions.arrayToList(event.values);
                    dataValues.add(0, (float)(event.timestamp - startingTime));
                    dataValues.add(1f);
                    dataFileWriter.writeToFile("Linear_Acceleration", dataValues);

                    //complimentary filter
                    float compHeading = ExtraFunctions.calcCompHeading(magHeading, gyroHeading);

                    Log.d("comp_heading", "" + compHeading);

                    //getting and rotating the previous XY points so North 0 on unit circle
                    float oPointX = scatterPlot.getLastYPoint();
                    float oPointY = -scatterPlot.getLastXPoint();

                    //calculating XY points from heading and stride_length
                    oPointX += ExtraFunctions.getXFromPolar(strideLength, compHeading);
                    oPointY += ExtraFunctions.getYFromPolar(strideLength, compHeading);

                    //rotating points by 90 degrees, so north is up
                    float rPointX = -oPointY;
                    float rPointY = oPointX;

                    scatterPlot.addPoint(rPointX, rPointY);

                    //saving XY location data
                    dataFileWriter.writeToFile("XY_Data_Set",
                            weeksGPS,
                            secondsGPS,
                            (event.timestamp - startingTime),
                            strideLength,
                            gyroHeading,
                            oPointX,
                            oPointY,
                            rPointX,
                            rPointY);

                    linearLayout.removeAllViews();
                    linearLayout.addView(scatterPlot.getGraphView(getApplicationContext()));

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

                    //complimentary filter
                    float compHeading = ExtraFunctions.calcCompHeading(magHeading, gyroHeading);

                    Log.d("comp_heading", "" + compHeading);

                    //getting and rotating the previous XY points so North 0 on unit circle
                    float oPointX = scatterPlot.getLastYPoint();
                    float oPointY = -scatterPlot.getLastXPoint();

                    //calculating XY points from heading and stride_length
                    oPointX += ExtraFunctions.getXFromPolar(strideLength, compHeading);
                    oPointY += ExtraFunctions.getYFromPolar(strideLength, compHeading);

                    //rotating points by 90 degrees, so north is up
                    float rPointX = -oPointY;
                    float rPointY = oPointX;

                    scatterPlot.addPoint(rPointX, rPointY);

                    //saving XY location data
                    dataFileWriter.writeToFile("XY_Data_Set",
                            weeksGPS,
                            secondsGPS,
                            (event.timestamp - startingTime),
                            strideLength,
                            gyroHeading,
                            oPointX,
                            oPointY,
                            rPointX,
                            rPointY);

                    linearLayout.removeAllViews();
                    linearLayout.addView(scatterPlot.getGraphView(getApplicationContext()));
                }

            }
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        long GPSTimeSec = location.getTime() / 1000;
        weeksGPS = GPSTimeSec / SECONDS_PER_WEEK;
        secondsGPS = GPSTimeSec % SECONDS_PER_WEEK;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onProviderDisabled(String provider) {}

    private void createFiles() {
        if (!areFilesCreated) {
            try {
                dataFileWriter = new DataFileWriter(FOLDER_NAME, DATA_FILE_NAMES, DATA_FILE_HEADINGS);
            } catch (IOException e) {
                e.printStackTrace();
            }
            areFilesCreated = true;
        }
    }

}
