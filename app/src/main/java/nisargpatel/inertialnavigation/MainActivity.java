package nisargpatel.inertialnavigation;

import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dm.zbar.android.scanner.ZBarConstants;
import com.dm.zbar.android.scanner.ZBarScannerActivity;

import net.sourceforge.zbar.Symbol;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import nisargpatel.inertialnavigation.stepcounters.MovingAverageStepCounter;
import nisargpatel.inertialnavigation.stepcounters.ThresholdStepCounter;

public class MainActivity extends ActionBarActivity implements SensorEventListener{

    private static final int ZBAR_QR_SCANNER_REQUEST = 1;

    private File myFile;
    private BufferedWriter writer;
    private String fileName;

    private static double strideLength;
    private static double upperThreshold = 11.5;
    private static double lowerThreshold = 6.5;

    //android views
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

    //handling Android views
    private TextView textThresholdSteps;
    private TextView textMovingAverageSteps;
    private TextView textAndroidSteps;
    private TextView textInstantAcc;
    private TextView textDistance;

    private Button buttonStartCounter;
    private Button buttonStopCounter;
    private Button buttonStepInfo;

    private StepInfoFragment myDialog;

    //declaring sensors
    private Sensor sensorAccelerometer;
    private Sensor sensorStepDetector;
    private SensorManager sensorManager;

    //instantiating step counters
    ThresholdStepCounter thresholdCountSteps1 = new ThresholdStepCounter(10, 8);
    ThresholdStepCounter thresholdCountSteps2 = new ThresholdStepCounter(11, 7);
    ThresholdStepCounter thresholdCountSteps3 = new ThresholdStepCounter(12, 6);
    ThresholdStepCounter thresholdCountSteps4 = new ThresholdStepCounter(13, 5);
    MovingAverageStepCounter movingAverageCountSteps1 = new MovingAverageStepCounter(0.5);
    MovingAverageStepCounter movingAverageCountSteps2 = new MovingAverageStepCounter(1.0);
    MovingAverageStepCounter movingAverageCountSteps3 = new MovingAverageStepCounter(1.5);
    MovingAverageStepCounter movingAverageCountSteps4 = new MovingAverageStepCounter(2.0);

    ThresholdStepCounter thresholdCountSteps = new ThresholdStepCounter(upperThreshold, lowerThreshold);
    MovingAverageStepCounter movingAverageCountSteps = new MovingAverageStepCounter(.75);

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //declaring all the views
        textThresholdSteps = (TextView) findViewById(R.id.textThreshold);
        textMovingAverageSteps = (TextView) findViewById(R.id.textMovingAverage);
        textAndroidSteps = (TextView) findViewById(R.id.textAndroid);
        textInstantAcc = (TextView) findViewById(R.id.textInstantAcc);
        textDistance = (TextView) findViewById(R.id.textDistance);

        buttonStartCounter = (Button) findViewById(R.id.buttonStartCounter);
        buttonStopCounter = (Button) findViewById(R.id.buttonStopCounter);
        buttonStepInfo = (Button) findViewById(R.id.buttonStepInfo);

        myDialog = new StepInfoFragment();

        //defining sensors
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorStepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        //open/create new file to hold sensor data
        openFile();

        //launches when the start button is pressed, and activates the sensors
        buttonStartCounter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorManager.registerListener(MainActivity.this, sensorAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
                sensorManager.registerListener(MainActivity.this, sensorStepDetector, SensorManager.SENSOR_DELAY_FASTEST);
                thresholdCountSteps.setThresholds(upperThreshold, lowerThreshold);
                Toast.makeText(getApplicationContext(), "Step counter started.", Toast.LENGTH_SHORT).show();
            }
        });

        //launches when the stop button is pressed, and deactivates the sensors
        buttonStopCounter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorManager.unregisterListener(MainActivity.this, sensorAccelerometer);
                sensorManager.unregisterListener(MainActivity.this, sensorStepDetector);
                Toast.makeText(getApplicationContext(), "Step counter stopped.", Toast.LENGTH_SHORT).show();
            }
        });

        buttonStepInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.setDialogMessage(getMessage());
                myDialog.show(getSupportFragmentManager(), "Step Info");
            }
        });

        ((TextView) findViewById(R.id.txtViewThresholds)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(getApplicationContext(), SetThresholdsActivity.class);
                startActivity(myIntent);
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

//        if (id == R.id.set_thresholds) {
//            Intent myIntent = new Intent(this, SetThresholdsActivity.class);
//            startActivity(myIntent);
//
//        }

        if (id == R.id.calibration) {
            Intent myIntent = new Intent(this, CalibrationActivity.class);
            startActivity(myIntent);
        }

        if (id == R.id.QRScan) {
            QRCodeScanner();
        }

        if (id == R.id.graph) {
            Intent myIntent = new Intent(this, GraphActivity.class);
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
            //zBarScanner recommends the following for when the scanner is cancled, however, for some reason, it causes the app to crash
            //String errorMessage = data.getStringExtra(ZBarConstants.ERROR_INFO);
            //Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();

            Toast.makeText(getApplicationContext(), "QR scanner canceled.", Toast.LENGTH_SHORT).show();
        }

    }

    //this method is required to implement SensorEventListener, but is not used
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    //This only works as long as the sensor is registered
    //Registration of the sensor is controlled by the OnClick methods in OnCreate
    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onSensorChanged(SensorEvent event) {

        final SensorEvent threadEvent = event;

        //all of the calculations happen in a separate thread, so that the UI thread is not bogged down
        new Thread (new Runnable() {
            @Override
            public void run() {

                //if the data is of accelerometer type, then run it through the step counters
                if (threadEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    Double zAcc = (double) threadEvent.values[2];

                    //display the instantaneous acceleration to let the user know the step counter is working
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textInstantAcc.setText(String.valueOf(threadEvent.values[2]).substring(0, 5));
                        }
                    });

                    if (thresholdCountSteps.stepFound(System.currentTimeMillis(), zAcc)) {
                        thresholdStepCount++;

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textThresholdSteps.setText(String.valueOf(thresholdStepCount));
                            }
                        });

                    }

                    if (thresholdCountSteps1.stepFound(System.currentTimeMillis(), zAcc)) {
                        thresholdStepCount1++;
                    }
                    if (thresholdCountSteps2.stepFound(System.currentTimeMillis(), zAcc)) {
                        thresholdStepCount2++;
                    }
                    if (thresholdCountSteps3.stepFound(System.currentTimeMillis(), zAcc)) {
                        thresholdStepCount3++;
                    }
                    if (thresholdCountSteps4.stepFound(System.currentTimeMillis(), zAcc)) {
                        thresholdStepCount4++;
                    }

                    if (movingAverageCountSteps.stepFound(System.currentTimeMillis(), zAcc)) {
                        movingAverageStepCount++;

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textMovingAverageSteps.setText(String.valueOf(movingAverageStepCount));
                            }
                        });


                        //writing the timestamps, acceleration data, and step locations to the data file
                        //if step is found, write a "1" in the last columhn
                        try {
                            myFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), fileName);
                            writer = new BufferedWriter(new FileWriter(myFile, true));

                            writer.write(System.currentTimeMillis() + ";" +  zAcc + ";" + 1);
                            writer.write(System.getProperty("line.separator"));
                            writer.close();
                        } catch (IOException ignored) {}

                        //if a step is not found, write a "0" in the last column
                    } else {

                        try {
                            myFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), fileName);
                            writer = new BufferedWriter(new FileWriter(myFile, true));

                            writer.write(System.currentTimeMillis() + ";" +  zAcc + ";" + 0);
                            writer.write(System.getProperty("line.separator"));
                            writer.close();
                        } catch (IOException ignored) {}
                    }

                    if (movingAverageCountSteps1.stepFound(System.currentTimeMillis(), zAcc)) {
                        movingAverageStepCount1++;
                    }
                    if (movingAverageCountSteps2.stepFound(System.currentTimeMillis(), zAcc)) {
                        movingAverageStepCount2++;
                    }
                    if (movingAverageCountSteps3.stepFound(System.currentTimeMillis(), zAcc)) {
                        movingAverageStepCount3++;
                    }
                    if (movingAverageCountSteps4.stepFound(System.currentTimeMillis(), zAcc)) {
                        movingAverageStepCount4++;
                    }

                    //if the data is not of accelerometer type (is step counter type) then increment the stepCount by 1
                } else {
                    if (threadEvent.values[0] == 1.0) {
                        androidStepCount++;

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textAndroidSteps.setText(String.valueOf(androidStepCount));
                                textDistance.setText(String.valueOf(strideLength * androidStepCount).substring(0,3));
                            }
                        });

                    }
                }


            }
        }).start(); //starts the thread

    }

    //setting thresholds for the ThresholdStepCounter (this method is called by SetThresholdsActivity)
    public static void setThresholds(double upper, double lower) {
        upperThreshold = upper;
        lowerThreshold = lower;
    }

    //open/create new file to hold sensor data
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void openFile() {
        //determines what the data file's name will be
        fileName = getFileName(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS));

        //lets the user know the name of the new file
        Toast.makeText(getApplicationContext(), fileName, Toast.LENGTH_SHORT).show();

        //if a file by the name already exists, it is opened, otherwise it is created
        try {
            myFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), fileName);
            if (!myFile.exists())
                myFile.createNewFile();

            //writing the heading of the file
            writer = new BufferedWriter(new FileWriter(myFile, true));
            writer.write(R.string.text_file_title);
            writer.close();
        } catch (IOException ignored) {}
    }

    //provides the fileName for new data files depending on what already exists in the directory
    //example: if accData2 already exists, then accData3 is created
    private String getFileName(File folderPath) {
        File folder = new File(folderPath.getPath());
        File[] listOfFiles = folder.listFiles();

        boolean fileFound;
        int fileCount = 0;

        //goes through every file in the directory to check its name
        do {
            fileCount++;
            fileFound = false;
            for (int i = 1; i <= listOfFiles.length; i++) {
                //array starts at 0
                if (listOfFiles[i - 1].getName().equals("accData" + fileCount + ".txt")) {
                    fileFound = true;
                }
            }
        } while (fileFound);

        return "accData" + fileCount + ".txt";
    }

    public static void setStrideLength(double stride) {
        strideLength = stride;
    }

    public static double getStrideLength() {
        return strideLength;
    }

    private String getMessage() {

        String message;

        String t1 = "T(10,8) = " + thresholdStepCount1;
        String t2 = "T(11,7) = " + thresholdStepCount2;
        String t3 = "T(12,6) = " + thresholdStepCount3;
        String t4 = "T(13,5) = " + thresholdStepCount4;

        String a1 = "A(0.5) = " + movingAverageStepCount1;
        String a2 = "A(1.0) = " + movingAverageStepCount2;
        String a3 = "A(1.5) = " + movingAverageStepCount3;
        String a4 = "A(2.0) = " + movingAverageStepCount4;

        String newLine = "\n";

        message = t1 + newLine + t2 + newLine + t3 + newLine + t4 + newLine + newLine + a1 + newLine + a2 + newLine + a3 + newLine + a4;

        return message;
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

    public boolean isCameraAvailable() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

}
