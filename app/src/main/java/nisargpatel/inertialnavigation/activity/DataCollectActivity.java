package nisargpatel.inertialnavigation.activity;

import android.annotation.TargetApi;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.format.Time;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

import nisargpatel.inertialnavigation.R;
import nisargpatel.inertialnavigation.extra.NPExtras;
import nisargpatel.inertialnavigation.filewriting.DataFileWriter;

public class DataCollectActivity extends ActionBarActivity implements SensorEventListener{

    private static final String FOLDER_NAME = "Inertial_Navigation_Data/Data_Collect_Activity";
    private static final String[] DATA_FILE_NAMES = {"Accelerometer", "GyroscopeCalibrated",
                                                     "GyroscopeUncalibrated", "RotationVector",
                                                     "GeomagneticRotationVector", "MagneticField",
                                                     "Gravity", "RotationMatrix"};
    private static final String[] DATA_FILE_HEADINGS = {"dt;Ax;Ay;Az",
                                                        "dt;Gx;Gy;Gz",
                                                        "dt;Gx;Gy;Gz",
                                                        "dt;Rx;Ry;Rz",
                                                        "dt;Rx;Ry;Rz",
                                                        "dt;Mx;My;Mz",
                                                        "dt;gx;gy;gz",
                                                        "time;(1,1);(1,2);(1,3);(2,1);(2,2);(2,3);(3,1);(3,2);(3,3)"};

    DataFileWriter dataFileWriter;

    private TextView info;

    private Sensor sensorAccelerometer;
    private Sensor sensorGyroCalibrated;
    private Sensor sensorGyroUncalibrated;
    private Sensor sensorRotationVector;
    private Sensor sensorGeomagneticRotationVector;
    private Sensor sensorMagneticField;
    private Sensor sensorGravity;
    private SensorManager sensorManager;

    private float[] rotationMatrix;

    private boolean gotAccData, gotMagData;
    private float[] accData, magData;


    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_collect);

        gotAccData = gotMagData = false;

        rotationMatrix = new float[9];

        info = (TextView) findViewById(R.id.textDataCollect);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorGyroCalibrated = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorGyroUncalibrated = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED);
        sensorRotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        sensorGeomagneticRotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR);
        sensorMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorGravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

        final Button buttonStart = (Button) findViewById(R.id.buttonDataStart);
        final Button buttonPause = (Button) findViewById(R.id.buttonDataPause);
        final Button buttonStop = (Button) findViewById(R.id.buttonDataStop);

        buttonPause.setEnabled(false);
        buttonStop.setEnabled(false);

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Time today = new Time(Time.getCurrentTimezone());
                today.setToNow();
                String currentTime = today.format("%H:%M:%S");

                info.setText("File creation time: " + currentTime + "\n\n" + "Files created: ");
                for (String dataFileName : DATA_FILE_NAMES)
                    info.setText(info.getText() + "\n\n" + "\t" + dataFileName);

                sensorManager.registerListener(DataCollectActivity.this, sensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
                sensorManager.registerListener(DataCollectActivity.this, sensorGyroCalibrated, SensorManager.SENSOR_DELAY_NORMAL);
                sensorManager.registerListener(DataCollectActivity.this, sensorGyroUncalibrated, SensorManager.SENSOR_DELAY_NORMAL);
                sensorManager.registerListener(DataCollectActivity.this, sensorRotationVector, SensorManager.SENSOR_DELAY_NORMAL);
                sensorManager.registerListener(DataCollectActivity.this, sensorGeomagneticRotationVector, SensorManager.SENSOR_DELAY_NORMAL);
                sensorManager.registerListener(DataCollectActivity.this, sensorMagneticField, SensorManager.SENSOR_DELAY_FASTEST);
                sensorManager.registerListener(DataCollectActivity.this, sensorGravity, SensorManager.SENSOR_DELAY_FASTEST);

                try {
                    dataFileWriter = new DataFileWriter(FOLDER_NAME, NPExtras.arrayToList(DATA_FILE_NAMES), NPExtras.arrayToList(DATA_FILE_HEADINGS));
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
                sensorManager.unregisterListener(DataCollectActivity.this, sensorGravity);
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
                sensorManager.unregisterListener(DataCollectActivity.this, sensorGravity);

                buttonStart.setEnabled(true);
                buttonPause.setEnabled(false);
                buttonStop.setEnabled(false);
            }
        });

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onSensorChanged(SensorEvent event) {

        float time  = NPExtras.nsToSec(event.timestamp);

        ArrayList<Float> sensorValuesList = NPExtras.arrayToList(event.values);
        sensorValuesList.add(0, time);

        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                dataFileWriter.writeToFile("Accelerometer", sensorValuesList);
                accData = event.values.clone();
                gotAccData = true;
                break;
            case Sensor.TYPE_GYROSCOPE:
                dataFileWriter.writeToFile("GyroscopeCalibrated", sensorValuesList);
                break;
            case Sensor.TYPE_GYROSCOPE_UNCALIBRATED:
                dataFileWriter.writeToFile("GyroscopeUncalibrated", sensorValuesList);
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                dataFileWriter.writeToFile("RotationVector", sensorValuesList);
                break;
            case Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR:
                dataFileWriter.writeToFile("GeomagneticRotationVector", sensorValuesList);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                dataFileWriter.writeToFile("MagneticField", sensorValuesList);
                magData = event.values.clone();
                gotMagData = true;
                break;
            case Sensor.TYPE_GRAVITY:
                dataFileWriter.writeToFile("Gravity", sensorValuesList);
                break;

        }

        if (gotAccData && gotMagData) {
            SensorManager.getRotationMatrix(rotationMatrix, null, accData, magData);

            ArrayList<Float> rotationMatrixList = NPExtras.arrayToList(rotationMatrix);
            rotationMatrixList.add(0, time);
            dataFileWriter.writeToFile("RotationMatrix", rotationMatrixList);

            gotAccData = gotMagData = false;
        }

    }

}
