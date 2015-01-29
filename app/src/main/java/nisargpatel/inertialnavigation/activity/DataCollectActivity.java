package nisargpatel.inertialnavigation.activity;

import android.annotation.TargetApi;
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
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import nisargpatel.inertialnavigation.R;

public class DataCollectActivity extends ActionBarActivity implements SensorEventListener{

    private TextView info;

    private Sensor sensorAccelerometer;
    private Sensor sensorGyroCalibrated;
    private Sensor sensorGyroUncalibrated;
    private Sensor sensorRotationVector;
    private Sensor sensorGeomagneticRotationVector;
    private Sensor sensorMagneticField;
    private SensorManager sensorManager;

    private float[] rotationMatrixFromVector;
    private float[] rotationMatrix;
    private float[] orientationValues;

    private boolean gotAccData, gotMagData;
    private float[] accData, magData;


    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_collect);

        gotAccData = gotMagData = false;
//        accData = new float[3];
//        magData = new float[3];

        rotationMatrixFromVector = new float[9];
        rotationMatrix = new float[9];
        orientationValues = new float[3];

        info = (TextView) findViewById(R.id.textDataCollect);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorGyroCalibrated = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorGyroUncalibrated = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED);
        sensorRotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        sensorGeomagneticRotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR);
        sensorMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        final Button buttonStart = (Button) findViewById(R.id.buttonDataStart);
        final Button buttonPause = (Button) findViewById(R.id.buttonDataPause);
        final Button buttonStop = (Button) findViewById(R.id.buttonDataStop);

        buttonPause.setEnabled(false);
        buttonStop.setEnabled(false);

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                info.setText("");

                sensorManager.registerListener(DataCollectActivity.this, sensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
                sensorManager.registerListener(DataCollectActivity.this, sensorGyroCalibrated, SensorManager.SENSOR_DELAY_NORMAL);
                sensorManager.registerListener(DataCollectActivity.this, sensorGyroUncalibrated, SensorManager.SENSOR_DELAY_NORMAL);
                sensorManager.registerListener(DataCollectActivity.this, sensorRotationVector, SensorManager.SENSOR_DELAY_NORMAL);
                sensorManager.registerListener(DataCollectActivity.this, sensorGeomagneticRotationVector, SensorManager.SENSOR_DELAY_NORMAL);
                sensorManager.registerListener(DataCollectActivity.this, sensorMagneticField, SensorManager.SENSOR_DELAY_FASTEST);

                try {
                    createFiles();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                buttonStart.setEnabled(false);
                buttonPause.setEnabled(true);
                buttonStop.setEnabled(true);
            }
        });

        buttonPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorManager.unregisterListener(DataCollectActivity.this, sensorAccelerometer);
                sensorManager.unregisterListener(DataCollectActivity.this, sensorGyroCalibrated);
                sensorManager.unregisterListener(DataCollectActivity.this, sensorGyroUncalibrated);
                sensorManager.unregisterListener(DataCollectActivity.this, sensorRotationVector);
                sensorManager.unregisterListener(DataCollectActivity.this, sensorGeomagneticRotationVector);
                sensorManager.unregisterListener(DataCollectActivity.this, sensorMagneticField);
            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorManager.unregisterListener(DataCollectActivity.this, sensorAccelerometer);
                sensorManager.unregisterListener(DataCollectActivity.this, sensorGyroCalibrated);
                sensorManager.unregisterListener(DataCollectActivity.this, sensorGyroUncalibrated);
                sensorManager.unregisterListener(DataCollectActivity.this, sensorRotationVector);
                sensorManager.unregisterListener(DataCollectActivity.this, sensorGeomagneticRotationVector);
                sensorManager.unregisterListener(DataCollectActivity.this, sensorMagneticField);

                buttonStart.setEnabled(true);
                buttonPause.setEnabled(false);
                buttonStop.setEnabled(false);
            }
        });

    }

    BufferedWriter writer;
    File fileAccelerometer;
    File fileGyroscopeCalibrated;
    File fileGyroscopeUncalibrated;
    File fileRotationVector;
    File fileGeomagneticRotationVector;
    File fileRotationMatrixFromVector;
    File fileRotationMatrix;
    File fileOrientationValues;

    private void createFiles() throws IOException{

        //creating the folder
        String folderName = "Inertial Navigation Data";

        File myFolder = new File(Environment.getExternalStorageDirectory(), folderName);
        if (!myFolder.exists())
            if (myFolder.mkdir())
                Toast.makeText(getApplicationContext(), "Folder created.", Toast.LENGTH_SHORT).show();

        String folderPath = myFolder.getPath();

        String[] fileType = {"Accelerometer", "GyroCalibrated", "GyroUncalibrated", "RotationVector", "GeomagneticRotationVector", "RotationMatrixFromVector", "RotationMatrix", "OrientationValues"};

        fileAccelerometer = new File(folderPath, getFileName(fileType[0]));
        fileGyroscopeCalibrated = new File(folderPath, getFileName(fileType[1]));
        fileGyroscopeUncalibrated = new File(folderPath, getFileName(fileType[2]));
        fileRotationVector = new File(folderPath, getFileName(fileType[3]));
        fileGeomagneticRotationVector = new File(folderPath, getFileName(fileType[4]));

        fileRotationMatrixFromVector = new File(folderPath, getFileName(fileType[5]));
        fileRotationMatrix = new File(folderPath, getFileName(fileType[6]));
        fileOrientationValues = new File(folderPath, getFileName(fileType[7]));

        createSingleFile(fileAccelerometer, fileType[0]);
        createSingleFile(fileGyroscopeCalibrated, fileType[1]);
        createSingleFile(fileGyroscopeUncalibrated, fileType[2]);
        createSingleFile(fileRotationVector, fileType[3]);
        createSingleFile(fileGeomagneticRotationVector, fileType[4]);

        createSingleFile(fileRotationMatrixFromVector, fileType[5]);
        createSingleFile(fileRotationMatrix, fileType[6]);
        createSingleFile(fileOrientationValues, fileType[7]);

    }

    private String getFileName(String type) {

        Time today = new Time(Time.getCurrentTimezone());
        today.setToNow();

        String date = "(" + today.year + "-" + (today.month + 1) + "-" + today.monthDay + ")";
        String currentTime = "(" + today.format("%H%M%S") + ")";

        return type + " " + date + " @ " + currentTime + ".txt";

    }

    private void createSingleFile(File file, String fileName) throws IOException {

        if (file.createNewFile())
            info.setText(info.getText() + "\n\n" + getFileName(fileName));

        writer = new BufferedWriter(new FileWriter(file, true));

        if (fileName.contains("Matrix"))
            writer.write("time;(1,1);(1,2);(1,3);(2,1);(2,2);(2,3);(3,1);(3,2);(3,3)");
        else
            writer.write("time;x;y;z");

        writer.write(System.getProperty("line.separator"));
        writer.close();

    }

    private void writeToFile(File file, long time, float[] sensorValues) {
        try {
            writer = new BufferedWriter(new FileWriter(file, true));
            writer.write(String.valueOf(time));
            for (double sensorValue : sensorValues)
                writer.write(";" + sensorValue);
            writer.write(System.getProperty("line.separator"));
            writer.close();
        } catch (IOException ignored) {}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_data_collection, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        long time = System.currentTimeMillis();
        float[] sensorValues = event.values.clone();

        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                writeToFile(fileAccelerometer, time, sensorValues);
                accData = sensorValues.clone();
                gotAccData = true;
                break;
            case Sensor.TYPE_GYROSCOPE:
                writeToFile(fileGyroscopeCalibrated, time, sensorValues);
                break;
            case Sensor.TYPE_GYROSCOPE_UNCALIBRATED:
                writeToFile(fileGyroscopeUncalibrated, time, sensorValues);
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                writeToFile(fileRotationVector, time, sensorValues);
                SensorManager.getRotationMatrixFromVector(rotationMatrixFromVector, event.values);
                writeToFile(fileRotationMatrixFromVector, time, sensorValues);
                break;
            case Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR:
                writeToFile(fileGeomagneticRotationVector, time, sensorValues);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                magData = sensorValues.clone();
                gotMagData = true;
                break;

        }

//        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
//
//        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
//
//        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE_UNCALIBRATED) {
//
//        } else if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
//
//        } else if (event.sensor.getType() == Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR) {
//
//        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
//
//        }

        if (gotAccData && gotMagData) {
            SensorManager.getRotationMatrix(rotationMatrix, null, accData, magData);
            writeToFile(fileRotationMatrix, time, rotationMatrix);

            SensorManager.getOrientation(rotationMatrix, orientationValues);
            writeToFile(fileOrientationValues, time, orientationValues);
        }

    }

}
