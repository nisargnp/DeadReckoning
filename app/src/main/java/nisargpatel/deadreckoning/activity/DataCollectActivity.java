package nisargpatel.deadreckoning.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.text.format.Time;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

import nisargpatel.deadreckoning.R;
import nisargpatel.deadreckoning.extra.ExtraFunctions;
import nisargpatel.deadreckoning.filewriting.DataFileWriter;

public class DataCollectActivity extends Activity implements SensorEventListener{

    private static final String FOLDER_NAME = "Dead_Reckoning/Data_Collect_Activity";
    private static final String[] DATA_FILE_NAMES = {
            "Accelerometer",
            "Linear_Acceleration",
            "Gyroscope_Calibrated",
            "Gyroscope_Uncalibrated",
            "Magnetic_Field",
            "Magnetic_Field_Uncalibrated",
            "Gravity",
            "Rotation_Matrix"
    };
    private static final String[] DATA_FILE_HEADINGS = {
            "Accelerometer" + "\n" + "t;Ax;Ay;Az",
            "Linear_Acceleration" + "\n" + "t;Ax;Ay;Az",
            "Gyroscope_Calibrated" + "\n" + "t;Gx;Gy;Gz",
            "Gyroscope_Uncalibrated" + "\n" + "t;uGx;uGy;uGz;xDrift;yDrift;zDrift",
            "Magnetic_Field" + "\n" + "t;Mx;My;Mz",
            "Magnetic_Field_Uncalibrated" + "\n" + "t;uMx;uMy;uMz;xHardIronBias;yHardIronBias;zHardIronBias",
            "Gravity" + "\n" + "t;gx;gy;gz",
            "Rotation_Matrix" + "\n" + "t;(1,1);(1,2);(1,3);(2,1);(2,2);(2,3);(3,1);(3,2);(3,3)"
    };

    private TextView info;
    private Button buttonStart;
    private Button buttonPause;
    private Button buttonStop;

    private Sensor[] sensors;
    private SensorManager sensorManager;

    private DataFileWriter dataFileWriter;

    private float[] rotationMatrix;
    private float[] accData, magData;

    private long startTime;
    private boolean firstRun;
    private boolean gotAccData, gotMagData;
    private boolean wasRunning;


    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_collect);

        startTime = 0;
        firstRun = true;
        gotAccData = gotMagData = false;
        wasRunning = false;

        rotationMatrix = new float[9];

        info = (TextView) findViewById(R.id.textDataCollect);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        sensors = new Sensor[7];
        sensors[0] = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensors[1] = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensors[2] = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensors[3] = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED);
        sensors[4] = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensors[5] = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED);
        sensors[6] = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

        buttonStart = (Button) findViewById(R.id.buttonDataStart);
        buttonPause = (Button) findViewById(R.id.buttonDataPause);
        buttonStop = (Button) findViewById(R.id.buttonDataStop);

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Time today = new Time(Time.getCurrentTimezone());
                today.setToNow();
                String currentTime = today.format("%H:%M:%S");

                info.setText("File creation time: " + currentTime + "\n\n" + "Files created: ");
                for (String dataFileName : DATA_FILE_NAMES)
                    info.setText(info.getText() + "\n\n" + "\t" + dataFileName);

                for (Sensor sensor : sensors)
                    sensorManager.registerListener(DataCollectActivity.this, sensor, SensorManager.SENSOR_DELAY_FASTEST);

                try {
                    dataFileWriter = new DataFileWriter(FOLDER_NAME, ExtraFunctions.arrayToList(DATA_FILE_NAMES), ExtraFunctions.arrayToList(DATA_FILE_HEADINGS));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                enableStopButton();

                wasRunning = true;
            }
        });

        buttonPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (Sensor sensor : sensors)
                    sensorManager.unregisterListener(DataCollectActivity.this, sensor);
                wasRunning = false;
            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (Sensor sensor : sensors)
                    sensorManager.unregisterListener(DataCollectActivity.this, sensor);

                enableStartButton();

                wasRunning = false;
            }
        });

    }

    private void enableStopButton() {
        buttonStart.setEnabled(false);
        buttonPause.setEnabled(true);
        buttonStop.setEnabled(true);
    }

    private void enableStartButton() {
        buttonStart.setEnabled(true);
        buttonPause.setEnabled(false);
        buttonStop.setEnabled(false);
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
            for (Sensor sensor : sensors)
                sensorManager.registerListener(DataCollectActivity.this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
            enableStopButton();
        } else {
            enableStartButton();
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (firstRun) {
            startTime = event.timestamp;
            firstRun = false;
        }

        ArrayList<Float> sensorValuesList = ExtraFunctions.arrayToList(event.values);
        sensorValuesList.add(0, (float)(event.timestamp - startTime));

        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                dataFileWriter.writeToFile("Accelerometer", sensorValuesList);
                accData = event.values.clone();
                gotAccData = true;
                break;
            case Sensor.TYPE_LINEAR_ACCELERATION:
                dataFileWriter.writeToFile("Linear_Acceleration", sensorValuesList);
                break;
            case Sensor.TYPE_GYROSCOPE:
                dataFileWriter.writeToFile("Gyroscope_Calibrated", sensorValuesList);
                break;
            case Sensor.TYPE_GYROSCOPE_UNCALIBRATED:
                dataFileWriter.writeToFile("Gyroscope_Uncalibrated", sensorValuesList);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                dataFileWriter.writeToFile("Magnetic_Field", sensorValuesList);
                magData = event.values.clone();
                gotMagData = true;
                break;
            case Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED:
                dataFileWriter.writeToFile("Magnetic_Field_Uncalibrated", sensorValuesList);
                break;
            case Sensor.TYPE_GRAVITY:
                dataFileWriter.writeToFile("Gravity", sensorValuesList);
                break;

        }

        if (gotAccData && gotMagData) {
            SensorManager.getRotationMatrix(rotationMatrix, null, accData, magData);

            ArrayList<Float> rotationMatrixList = ExtraFunctions.arrayToList(rotationMatrix);
            rotationMatrixList.add(0, (float)(event.timestamp - startTime));
            dataFileWriter.writeToFile("Rotation_Matrix", rotationMatrixList);

            gotAccData = gotMagData = false;
        }

    }

}
