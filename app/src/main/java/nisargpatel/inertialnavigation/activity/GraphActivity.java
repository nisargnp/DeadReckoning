package nisargpatel.inertialnavigation.activity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.dm.zbar.android.scanner.ZBarConstants;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import nisargpatel.inertialnavigation.R;
import nisargpatel.inertialnavigation.extra.NPExtras;
import nisargpatel.inertialnavigation.graph.ScatterPlot;
import nisargpatel.inertialnavigation.heading.EulerHeadingInference;
import nisargpatel.inertialnavigation.heading.GyroIntegration;
import nisargpatel.inertialnavigation.stepcounter.MovingAverageStepCounter;

public class GraphActivity extends ActionBarActivity implements SensorEventListener{

    private static final double STEP_COUNTER_SENSITIVITY = 1.0;

    private MovingAverageStepCounter movingStepCounter;
    private GyroIntegration gyroIntegration;
    private EulerHeadingInference eulerHeadingInference;
    private ScatterPlot sPlot;

    private LinearLayout linearLayout;

    private Sensor sensorAccelerometer;
    private Sensor sensorGyroscopeUncalibrated;
    private SensorManager sensorManager;

    private float strideLength;

    private boolean filesCreated;

    private BufferedWriter writer;
    private File fileAccelerometer;
    private File fileGyroscopeUncalibrated;
    private File fileXYDataSet;

    private float matrixHeading;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        //getting global settings
        strideLength =  getIntent().getFloatExtra("stride_length", 2.5f);
        String userName = getIntent().getStringExtra("user_name");

        Toast.makeText(GraphActivity.this, "Username: " + userName + "\n" + "Stride Length: " + strideLength, Toast.LENGTH_SHORT).show();

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
        movingStepCounter = new MovingAverageStepCounter(STEP_COUNTER_SENSITIVITY);
        gyroIntegration = new GyroIntegration(300, 0.0025f);
        eulerHeadingInference = new EulerHeadingInference(NPExtras.getIdentityMatrix());

        //setting up graph with origin
        sPlot = new ScatterPlot("Position");
        sPlot.addPoint(0, 0);
        linearLayout.addView(sPlot.getGraphView(getApplicationContext()));

        //initializing needed variables
        filesCreated = false;

        //setting up buttons
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorManager.registerListener(GraphActivity.this, sensorAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
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
                buttonStop.setEnabled(true);

            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorManager.unregisterListener(GraphActivity.this, sensorAccelerometer);
                sensorManager.unregisterListener(GraphActivity.this, sensorGyroscopeUncalibrated);
                Toast.makeText(getApplicationContext(), "Step counter stopped.", Toast.LENGTH_SHORT).show();

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
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE_UNCALIBRATED) {

            float[] deltaOrientation = gyroIntegration.getIntegratedValues(event.timestamp, event.values);
            matrixHeading = eulerHeadingInference.getCurrentHeading(deltaOrientation);

            ArrayList<Float> dataValues = arrayToList(event.values);
            dataValues.add(matrixHeading);

            writeToFile(fileGyroscopeUncalibrated, event.timestamp, dataValues);

        } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            float zAcc = event.values[0];

            //if step is found, stepFound == true
            boolean stepFound = movingStepCounter.stepFound(zAcc);

            if (stepFound) {

                ArrayList<Float> dataValues = arrayToList(event.values);
                dataValues.add(1f);
                writeToFile(fileAccelerometer, event.timestamp, dataValues);

                float heading = matrixHeading + (float) (Math.PI / 2.0);
                double pointX = NPExtras.getXFromPolar(strideLength, heading);
                double pointY = NPExtras.getYFromPolar(strideLength, heading);

                pointX += sPlot.getLastXPoint();
                pointY += sPlot.getLastYPoint();
                sPlot.addPoint(pointX, pointY);

                dataValues.clear();
                dataValues.add(strideLength);
                dataValues.add(matrixHeading);
                dataValues.add((float)pointX);
                dataValues.add((float)pointY);

                writeToFile(fileXYDataSet, event.timestamp, dataValues);

                linearLayout.removeAllViews();
                linearLayout.addView(sPlot.getGraphView(getApplicationContext()));

                //if step is not found
            } else {
                ArrayList<Float> dataValues = arrayToList(event.values);
                dataValues.add(0f);
                writeToFile(fileAccelerometer, event.timestamp, dataValues);
            }

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

        String[] fileType = {"Accelerometer", "GyroUncalibrated", "XYDataSet"};

        fileAccelerometer = new File(folderPath, getFileName(fileType[0]));
        fileGyroscopeUncalibrated = new File(folderPath, getFileName(fileType[1]));
        fileXYDataSet = new File(folderPath, getFileName(fileType[2]));

        createDataFile(fileAccelerometer, fileType[0]);
        createDataFile(fileGyroscopeUncalibrated, fileType[1]);
        createDataFile(fileXYDataSet, fileType[2]);

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

        if (fileName.contains("Gyro"))
            writer.write("dt;Gx;Gy;Gz;heading");
        else if (fileName.contains("Acc"))
            writer.write("dt;Ax;Ay;Az;stepFound");
        else if (fileName.contains("XY"))
            writer.write("dt;strideLength;heading;pointX;pointY");

        writer.write(System.getProperty("line.separator"));
        writer.close();

    }

    private void writeToFile(File file, float time, ArrayList<Float> dataValues) {
        try {
            writer = new BufferedWriter(new FileWriter(file, true));
            writer.write(String.valueOf(time));
            for (float dataValue : dataValues)
                writer.write(";" + dataValue);
            writer.write(System.getProperty("line.separator"));
            writer.close();
        } catch (IOException ignored) {}
    }

    private ArrayList<Float> arrayToList(float[] staticArray) {
        ArrayList<Float> dynamicList = new ArrayList<>();
        for (float staticArrayValue : staticArray)
            dynamicList.add(staticArrayValue);
        return dynamicList;
    }
}
