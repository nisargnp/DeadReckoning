package nisargpatel.inertialnavigation.activity;

import android.annotation.TargetApi;
import android.content.Intent;
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
import nisargpatel.inertialnavigation.heading.HeadingInference;
import nisargpatel.inertialnavigation.stepcounters.MovingAverageStepCounter;

public class GraphActivity extends ActionBarActivity implements SensorEventListener{

    private static final int ZBAR_QR_SCANNER_REQUEST = 1;

    private HeadingInference headingInference;
    private MovingAverageStepCounter movingStepCounter;

    //private Sensor sensorStepDetector;
    private Sensor sensorAccelerometer;
    private Sensor sensorGyroscope;
    private Sensor sensorMagnetometer;
    private SensorManager sensorManager;

    private LinearLayout linearLayout;

    private ScatterPlot sPlot;

    private long recordedTime;

    private static final int STRIDE_LENGTH = 2;

    private boolean sessionFilesCreated;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        //initializing needed classes
        movingStepCounter = new MovingAverageStepCounter(1.0);
        double gyroInput[] = {-2900, -1450, 0, 1450, 2900};
        double radianInput[] = {-90, 0, 90, 180, 270};
        headingInference = new HeadingInference(gyroInput, radianInput);

        //declaring views
        Button buttonStart = (Button) findViewById(R.id.buttonGraphStart);
        Button buttonStop = (Button) findViewById(R.id.buttonGraphStop);
        linearLayout = (LinearLayout) findViewById(R.id.linearLayoutGraph);

        //declaring sensors
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        //sensorStepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorGyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorMagnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR);

        //setting up graph with origin
        sPlot = new ScatterPlot("Position");
        sPlot.addPoint(0, 0);
        linearLayout.addView(sPlot.getGraphView(getApplicationContext()));

        //
//        if (!sessionFilesCreated) {
//            //creating data files
//            createFile("accelerometer");
//            createFile("gyroscope");
//            createFile("magnetometer");
//            sessionFilesCreated = true;
//        }

        //buttons
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //sensorManager.registerListener(GraphActivity.this, sensorStepDetector, SensorManager.SENSOR_DELAY_FASTEST);
                sensorManager.registerListener(GraphActivity.this, sensorAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
                sensorManager.registerListener(GraphActivity.this, sensorGyroscope, SensorManager.SENSOR_DELAY_FASTEST);
                sensorManager.registerListener(GraphActivity.this, sensorMagnetometer, SensorManager.SENSOR_DELAY_FASTEST);
                recordedTime = System.currentTimeMillis();
                Toast.makeText(getApplicationContext(), "Step counter started.", Toast.LENGTH_SHORT).show();
            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //sensorManager.unregisterListener(GraphActivity.this, sensorStepDetector);
                sensorManager.unregisterListener(GraphActivity.this, sensorAccelerometer);
                sensorManager.unregisterListener(GraphActivity.this, sensorGyroscope);
                sensorManager.unregisterListener(GraphActivity.this, sensorMagnetometer);
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

        if (id == R.id.calibration) {
            Intent myIntent = new Intent(this, CalibrationActivity.class);
            startActivity(myIntent);
        }

        if (id == R.id.QRScan) {
            QRCodeScanner();
        }

        if (id == R.id.stepCounter) {
            Intent myIntent = new Intent(this, StepCounterActivity.class);
            startActivity(myIntent);
        }

        if (id == R.id.orientationTest) {
            Intent myIntent = new Intent(this, OrientationTestActivity.class);
            startActivity(myIntent);
        }

        if (id == R.id.dataCollect) {
            Intent myIntent = new Intent(this, DataCollectActivity.class);
            startActivity(myIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Toast.makeText(getApplicationContext(), data.getStringExtra(ZBarConstants.SCAN_RESULT), Toast.LENGTH_LONG).show();
        } else if (resultCode == RESULT_CANCELED) {
            //zBarScanner recommends the following for when the scanner is canceled, however, for some reason, it causes the app to crash
            String errorMessage = data.getStringExtra(ZBarConstants.ERROR_INFO);
            Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
            //Toast.makeText(getApplicationContext(), "QR scanner canceled.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private double averageGyroValue;
    private double totalGyroValue;

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {

            double xVelocity = (double) event.values[0];
            double yVelocity = (double) event.values[1];
            double zVelocity = (double) event.values[2];

            double currentGyroValue = (System.currentTimeMillis() - recordedTime) * event.values[2];

            if (currentGyroValue > averageGyroValue * 100000 ) {
                totalGyroValue += currentGyroValue;
            } else {
                averageGyroValue = (averageGyroValue + currentGyroValue) / 2;
            }

            recordedTime = System.currentTimeMillis();

//            writeToFile(fileGyroscope, xVelocity, yVelocity, zVelocity, totalGyroValue);

        }
        //else if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {}
        else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            Double xAcc = (double) event.values[0];
            Double yAcc = (double) event.values[1];
            Double zAcc = (double) event.values[2];

            boolean stepFound = false;

            if (movingStepCounter.stepFound(zAcc))
                stepFound = true;

            if (stepFound) {

//                writeToFile(fileAccelerometer, xAcc, yAcc, zAcc, 1);

                headingInference.calcDegrees(totalGyroValue);
                double pointX = headingInference.getXPoint(STRIDE_LENGTH);
                double pointY = headingInference.getYPoint(STRIDE_LENGTH);

                sPlot.addPoint(sPlot.getLastXPoint() + pointX, sPlot.getLastYPoint() + pointY);

                linearLayout.removeAllViews();
                linearLayout.addView(sPlot.getGraphView(getApplicationContext()));
            } else {
//                writeToFile(fileAccelerometer, xAcc, yAcc, zAcc, 0);
            }
        } else if (event.sensor.getType() == Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR) {
            Double xField = (double) event.values[0];
            Double yField = (double) event.values[1];
            Double zField = (double) event.values[2];

//            writeToFile(fileMagnetometer, xField, yField, zField, 0);
        }
    }

    private BufferedWriter writer;

    private File fileAccelerometer;
    private File fileGyroscope;
    private File fileMagnetometer;

    private void writeToFile(File myFile, double d1, double d2, double d3, double extra) {
        try {
            writer = new BufferedWriter(new FileWriter(myFile, true));
            writer.write(System.currentTimeMillis() + ";" +  d1 + ";" + d2 + ";" + d3 + ";" + extra);
            writer.write(System.getProperty("line.separator"));
            writer.close();
        } catch (IOException ignored) {}
    }

    private void createFile(String type) {

        String folderName = "Inertial Navigation Data";

        File myFolder = new File(Environment.getExternalStorageDirectory(), folderName);
        if (!myFolder.exists())
            if (myFolder.mkdir())
                Toast.makeText(getApplicationContext(), "Folder created.", Toast.LENGTH_SHORT).show();

        String folderPath = myFolder.getPath();

        //determines what the data file's name will be
        String fileName = getFileName(type);

        switch (type) {
            case "accelerometer":
                try {
                    //creating file
                    fileAccelerometer = new File(folderPath, fileName);
                    if (fileAccelerometer.createNewFile())
                        Toast.makeText(getApplicationContext(), fileName, Toast.LENGTH_SHORT).show();
                    //writing the heading of the file
                    writer = new BufferedWriter(new FileWriter(fileAccelerometer, true));
                    writer.write("time;xAcc;yAcc;zAcc;stepFound");
                    writer.write(System.getProperty("line.separator"));
                    writer.close();
                } catch (IOException ignored) {
                }
                break;
            case "gyroscope":
                try {
                    //creating file
                    fileGyroscope = new File(folderPath, fileName);
                    if(fileGyroscope.createNewFile())
                        Toast.makeText(getApplicationContext(), fileName, Toast.LENGTH_SHORT).show();
                    //writing the heading of the file
                    writer = new BufferedWriter(new FileWriter(fileGyroscope, true));
                    writer.write("time;xVelocity;yVelocity;zVelocity;orientation");
                    writer.write(System.getProperty("line.separator"));
                    writer.close();
                } catch (IOException ignored) {
                }
                break;
            case "magnetometer":
                try {
                    //creating file
                    fileMagnetometer = new File(folderPath, fileName);
                    if(fileMagnetometer.createNewFile())
                        Toast.makeText(getApplicationContext(), fileName, Toast.LENGTH_SHORT).show();
                    //writing the heading of the file
                    writer = new BufferedWriter(new FileWriter(fileMagnetometer, true));
                    writer.write("time;xField;yField;zField;orientation");
                    writer.write(System.getProperty("line.separator"));
                    writer.close();
                } catch (IOException ignored) {
                }
                break;
        }

    }

    private String getFileName(String type) {

        Time today = new Time(Time.getCurrentTimezone());
        today.setToNow();

        String date = "(" + today.year + "-" + today.month + "-" + today.monthDay + ")";
        String currentTime = "(" + today.format("%H.%M.%S") + ")";

        return type + " " + date + " " + currentTime + ".txt";

    }

    private void QRCodeScanner() {
        if (isCameraAvailable()) {
            Intent myIntent = new Intent(getApplicationContext(), ZBarScannerActivity.class);
            myIntent.putExtra(ZBarConstants.SCAN_MODES, new int[]{Symbol.QRCODE});
            startActivityForResult(myIntent, ZBAR_QR_SCANNER_REQUEST);
        } else {
            Toast.makeText(getApplicationContext(), "Camera not available.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isCameraAvailable() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }
}
