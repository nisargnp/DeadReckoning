package nisargpatel.inertialnavigation.activity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.dm.zbar.android.scanner.ZBarConstants;
import com.dm.zbar.android.scanner.ZBarScannerActivity;

import net.sourceforge.zbar.Symbol;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import nisargpatel.inertialnavigation.R;
import nisargpatel.inertialnavigation.graph.ScatterPlot;
import nisargpatel.inertialnavigation.heading.EulerHeadingInference;
import nisargpatel.inertialnavigation.math.MathFunctions;
import nisargpatel.inertialnavigation.stepcounter.MovingAverageStepCounter;

public class GraphActivity extends ActionBarActivity implements SensorEventListener{

    private static final String PREFS_NAME = "Inertial Navigation Preferences";
    private static final int ZBAR_QR_SCANNER_REQUEST = 1;
    private static final double STEP_COUNTER_SENSITIVITY = 1.0;

    private MovingAverageStepCounter movingStepCounter;
    private EulerHeadingInference eulerHeadingInference;
    private ScatterPlot sPlot;

    private LinearLayout linearLayout;

    private Sensor sensorAccelerometer;
    private Sensor sensorGyroscopeCalibrated;
    private Sensor sensorGyroscopeUncalibrated;
    private SensorManager sensorManager;

    private float strideLength;

    private boolean filesCreated;
    private boolean useEulerAngles;

    private BufferedWriter writer;
    private File fileAccelerometer;
    private File fileGyroscopeCalibrated;
    private File fileGyroscopeUncalibrated;

    private int runCountGyro;
    private float lastTimestampGyro;
    private float gyroHeading;

    private int runCountGyroU;
    private float lastTimestampGyroU;
    private float matrixHeading;
    private float biasGyroU[];

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        //getting global settings
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();

        if (sharedPreferences.getBoolean("first_run", true)) {
            Intent myIntent = new Intent(GraphActivity.this, CalibrationActivity.class);
            startActivity(myIntent);
        }

        sharedPreferencesEditor.putBoolean("first_run", false).apply();
        strideLength = sharedPreferences.getFloat("stride_length", 2.5f);

        //defining views
        final Button buttonStart = (Button) findViewById(R.id.buttonGraphStart);
        final Button buttonStop = (Button) findViewById(R.id.buttonGraphStop);
        final Button buttonSwitch = (Button) findViewById(R.id.buttonGraphSwitch);
        Button buttonClear = (Button) findViewById(R.id.buttonGraphClear);
        linearLayout = (LinearLayout) findViewById(R.id.linearLayoutGraph);

        //defining sensors
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorGyroscopeCalibrated = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorGyroscopeUncalibrated = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED);

        //initializing needed classes
        movingStepCounter = new MovingAverageStepCounter(STEP_COUNTER_SENSITIVITY);
        eulerHeadingInference = new EulerHeadingInference(MathFunctions.getIdentityMatrix());

        //setting up graph with origin
        sPlot = new ScatterPlot("Position");
        sPlot.addPoint(0, 0);
        linearLayout.addView(sPlot.getGraphView(getApplicationContext()));

        //initializing needed variables
        useEulerAngles = false;
        filesCreated = false;
        runCountGyro = 0;
        runCountGyroU = 0;
        gyroHeading = (float) Math.PI / 2.0f;
        matrixHeading = 0;
        biasGyroU = new float[3];

        Toast.makeText(getApplicationContext(), "Not using Euler angles.", Toast.LENGTH_SHORT).show();

        //setting up buttons
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorManager.registerListener(GraphActivity.this, sensorAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
                sensorManager.registerListener(GraphActivity.this, sensorGyroscopeCalibrated, SensorManager.SENSOR_DELAY_FASTEST);
                sensorManager.registerListener(GraphActivity.this, sensorGyroscopeUncalibrated, SensorManager.SENSOR_DELAY_FASTEST);

                Toast.makeText(getApplicationContext(), "Step counter started.", Toast.LENGTH_SHORT).show();

                if (!filesCreated) {
                    try {
                        createDataFiles();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                buttonStart.setEnabled(false);
                buttonSwitch.setEnabled(false);
                buttonStop.setEnabled(true);

            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorManager.unregisterListener(GraphActivity.this, sensorAccelerometer);
                sensorManager.unregisterListener(GraphActivity.this, sensorGyroscopeCalibrated);
                sensorManager.unregisterListener(GraphActivity.this, sensorGyroscopeUncalibrated);
                Toast.makeText(getApplicationContext(), "Step counter stopped.", Toast.LENGTH_SHORT).show();

                buttonStart.setEnabled(true);
                buttonSwitch.setEnabled(true);
                buttonStop.setEnabled(false);
            }
        });

        buttonSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                useEulerAngles = !useEulerAngles;

                if (useEulerAngles)
                    Toast.makeText(getApplicationContext(), "Using Euler angles.", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getApplicationContext(), "Not using Euler angles.", Toast.LENGTH_SHORT).show();

            }
        });

        buttonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gyroHeading = 0;
                eulerHeadingInference.clearMatrix();

                sPlot.clearSet();
                sPlot.addPoint(0,0);
                linearLayout.removeAllViews();
                linearLayout.addView(sPlot.getGraphView(getApplicationContext()));
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

        switch (id) {
            case R.id.settings: {
                Intent myIntent = new Intent(this, SettingsActivity.class);
                startActivity(myIntent);
                break;
            }
            case R.id.QRScan:
                QRCodeScanner();
                break;
            case R.id.stepCounter: {
                Intent myIntent = new Intent(this, StepCounterActivity.class);
                startActivity(myIntent);
                break;
            }
            case R.id.orientationTest: {
                Intent myIntent = new Intent(this, OrientationTestActivity.class);
                startActivity(myIntent);
                break;
            }
            case R.id.dataCollect: {
                Intent myIntent = new Intent(this, DataCollectActivity.class);
                startActivity(myIntent);
                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Toast.makeText(getApplicationContext(), data.getStringExtra(ZBarConstants.SCAN_RESULT), Toast.LENGTH_LONG).show();
        } else if (resultCode == RESULT_CANCELED) {
            Toast.makeText(getApplicationContext(), "QR Code Scanner canceled.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {

            runCountGyro++;

            //on the first run, the timestamp of the first point needs to be set
            //so that delta-t can be calculated for the next point
            if (runCountGyro <= 1) {
                lastTimestampGyro = MathFunctions.nsToSec(event.timestamp);
                return;
            }

            double deltaTimeGyro = MathFunctions.nsToSec(event.timestamp) - lastTimestampGyro;
            double deltaHeading = deltaTimeGyro * event.values[2];

            gyroHeading += deltaHeading;

            writeToFile(fileGyroscopeCalibrated, event.timestamp, event.values, gyroHeading);

            lastTimestampGyro = MathFunctions.nsToSec(event.timestamp);

        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE_UNCALIBRATED) {

            runCountGyroU++;

            //setting the initial timestamp on the first run
            if (runCountGyroU == 1) {
                biasGyroU[0] = event.values[0];
                biasGyroU[1] = event.values[1];
                biasGyroU[2] = event.values[2];
                return;
            }

            double currentGyroUTime = MathFunctions.nsToSec(event.timestamp);
            double deltaGyroUTime = currentGyroUTime - lastTimestampGyroU;

            //averaging bias for the first few hundred data points
            if (runCountGyroU <= 300) {
                biasGyroU[0] = (biasGyroU[0] * ((runCountGyroU - 1) / runCountGyroU)) + (event.values[0] / runCountGyroU);
                biasGyroU[1] = (biasGyroU[1] * ((runCountGyroU - 1) / runCountGyroU)) + (event.values[1] / runCountGyroU);
                biasGyroU[2] = (biasGyroU[2] * ((runCountGyroU - 1) / runCountGyroU)) + (event.values[2] / runCountGyroU);
                lastTimestampGyroU = MathFunctions.nsToSec(event.timestamp);
                return;
            }

            float[] deltaOrientationGyroU = new float[3];
            deltaOrientationGyroU[0] = (float) deltaGyroUTime * (event.values[0] - biasGyroU[0]);
            deltaOrientationGyroU[1] = (float) deltaGyroUTime * (event.values[1] - biasGyroU[1]);
            deltaOrientationGyroU[2] = (float) deltaGyroUTime * (event.values[2] - biasGyroU[2]);

            matrixHeading = eulerHeadingInference.getCurrentHeading(deltaOrientationGyroU);

            writeToFile(fileGyroscopeUncalibrated, event.timestamp, event.values, matrixHeading);

            lastTimestampGyroU = MathFunctions.nsToSec(event.timestamp);

            Log.d("matrixHeading", String.valueOf(matrixHeading));

        } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            float zAcc = event.values[0];

            //if step is found, stepFound == true
            boolean stepFound = movingStepCounter.stepFound(zAcc);

            if (stepFound) {

                writeToFile(fileAccelerometer, event.timestamp, event.values, 1);

                float heading;
                if (useEulerAngles)
                    heading = matrixHeading + (float) (Math.PI / 2.0);
                else
                    heading = gyroHeading;

                double pointX = MathFunctions.getXFromPolar(strideLength, heading);
                double pointY = MathFunctions.getYFromPolar(strideLength, heading);

                sPlot.addPoint(sPlot.getLastXPoint() + pointX, sPlot.getLastYPoint() + pointY);

                linearLayout.removeAllViews();
                linearLayout.addView(sPlot.getGraphView(getApplicationContext()));

                //if step is not found
            } else {
                writeToFile(fileAccelerometer, event.timestamp, event.values, 0);
            }

        }
    }

    private void QRCodeScanner() {
        boolean isCameraAvailable = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
        if (isCameraAvailable) {
            Intent myIntent = new Intent(getApplicationContext(), ZBarScannerActivity.class);
            myIntent.putExtra(ZBarConstants.SCAN_MODES, new int[]{Symbol.QRCODE});
            startActivityForResult(myIntent, ZBAR_QR_SCANNER_REQUEST);
        } else {
            Toast.makeText(getApplicationContext(), "Camera not available.", Toast.LENGTH_SHORT).show();
        }
    }

    private void createDataFiles() throws IOException{

        //creating the folder
        String folderName = "Inertial_Navigation_Data/Graph_Activity";

        File myFolder = new File(Environment.getExternalStorageDirectory(), folderName);
        if (!myFolder.exists())
            if (myFolder.mkdirs())
                Toast.makeText(getApplicationContext(), "Folder created.", Toast.LENGTH_SHORT).show();

        String folderPath = myFolder.getPath();

        String[] fileType = {"Accelerometer", "GyroCalibrated", "GyroUncalibrated"};

        fileAccelerometer = new File(folderPath, getFileName(fileType[0]));
        fileGyroscopeCalibrated = new File(folderPath, getFileName(fileType[1]));
        fileGyroscopeUncalibrated = new File(folderPath, getFileName(fileType[2]));

        createDataFile(fileAccelerometer, fileType[0]);
        createDataFile(fileGyroscopeCalibrated, fileType[1]);
        createDataFile(fileGyroscopeUncalibrated, fileType[2]);

    }

    private String getFileName(String type) {

        Time today = new Time(Time.getCurrentTimezone());
        today.setToNow();

        String date = "(" + today.year + "-" + (today.month + 1) + "-" + today.monthDay + ")";
        String currentTime = "(" + today.format("%H%M%S") + ")";

        return type + " " + date + " @ " + currentTime + ".txt";

    }

    private void createDataFile(File file, String fileName) throws IOException {

        if (file.createNewFile())
            Log.d("dataFiles",getFileName(fileName));

        writer = new BufferedWriter(new FileWriter(file, true));

        writer.write("time;x;y;z");
        if (fileName.contains("gyro"))
            writer.write(";heading");
        else if (fileName.contains("acc"))
            writer.write(";stepFound");

        writer.write(System.getProperty("line.separator"));
        writer.close();

    }

    private void writeToFile(File file, float time, float[] sensorValues, float extraValue) {
        try {
            writer = new BufferedWriter(new FileWriter(file, true));
            writer.write(String.valueOf(time));
            for (double sensorValue : sensorValues)
                writer.write(";" + sensorValue);
            writer.write(";" + extraValue);
            writer.write(System.getProperty("line.separator"));
            writer.close();
        } catch (IOException ignored) {}
    }
}
