package nisargpatel.deadreckoning.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import nisargpatel.deadreckoning.extra.ExtraFunctions;
import nisargpatel.deadreckoning.filewriting.DataFileWriter;
import nisargpatel.deadreckoning.bias.GyroscopeBias;

public class GyroCalibrationDialogFragment extends DialogFragment implements SensorEventListener{

    public static final String DIALOG_MESSAGE = "To calibrate: Set the phone on a flat surface and press \"Start\".";
    public static final int WAIT_COUNTER = 100;

    private DataFileWriter dataFileWriter;
    private static final String FOLDER_NAME = "Dead_Reckoning/Calibration_Fragment";
    private static final String[] DATA_FILE_NAMES = {"Gyroscope_Uncalibrated"};
    private static final String[] DATA_FILE_HEADINGS = {"t;uGx;uGy;uGz;"};

    private Sensor sensorGyroscopeU;
    private SensorManager sensorManager;

    private GyroscopeBias gyroscopeBias;

    private ProgressDialog progressDialog;

    private Handler handler;

    private int runCount;

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        try {
            dataFileWriter = new DataFileWriter(FOLDER_NAME, ExtraFunctions.arrayToList(DATA_FILE_NAMES), ExtraFunctions.arrayToList(DATA_FILE_HEADINGS));
        } catch (IOException e) {
            e.printStackTrace();
        }

        runCount = 0;

        gyroscopeBias = new GyroscopeBias(500);

        progressDialog = new ProgressDialog(getActivity());

        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        sensorGyroscopeU = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder
                .setMessage(DIALOG_MESSAGE)
                .setNeutralButton("Start Calibration", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //this onClick() will be overridden during onStart()
                    }
                });

        return alertDialogBuilder.create();

    }

    @Override
    public void onStart() {
        super.onStart();

        AlertDialog alertDialog = (AlertDialog) getDialog();
        final Button buttonNeutral = alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL);
        buttonNeutral.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorManager.registerListener(GyroCalibrationDialogFragment.this, sensorGyroscopeU, SensorManager.SENSOR_DELAY_FASTEST);
                progressDialog = ProgressDialog.show(getActivity(), "Calibrating", "Please wait.", true, false);
            }
        });

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE_UNCALIBRATED) {

            //waiting a while so that the button press doesn't effect the calibration
            if (runCount++ > WAIT_COUNTER) {

                double norm = Math.sqrt(Math.pow(event.values[0], 2) + Math.pow(event.values[1], 2) + Math.pow(event.values[2], 2));

                //applying a quick low pass filter (the values should be small since the phone is not moving during calibration)
                if (Math.abs(event.values[0]) < norm && Math.abs(event.values[1]) < norm && Math.abs(event.values[2]) < norm)
                    if (gyroscopeBias.calcBias(event.values)) {
                        dataFileWriter.writeToFile("Gyroscope_Uncalibrated", "Calculated_bias: " + Arrays.toString(gyroscopeBias.getBias()));
                        dismissDialog();
                    } else {
                        //saving gyroscope data
                        float[] gyroValues = {event.values[0], event.values[1], event.values[2]};
                        ArrayList<Float> dataValues = ExtraFunctions.arrayToList(gyroValues);
                        dataValues.add(0, (float) event.timestamp);

                        dataFileWriter.writeToFile("Gyroscope_Uncalibrated", dataValues);
                    }

            }

        }

    }

    private void dismissDialog() {

        sensorManager.unregisterListener(this);
        progressDialog.dismiss();
        handler.sendEmptyMessage(1);

        dismiss();

    }

    public float[] getGyroBias() {
        return gyroscopeBias.getBias();
    }


}
