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
import nisargpatel.deadreckoning.heading.GyroscopeBias;

public class GyroCalibrateDialogFragment extends DialogFragment implements SensorEventListener{

    public static final String DIALOG_MESSAGE = "To calibrate: Set the phone on a flat surface and press \"Start\".";

    private DataFileWriter dataFileWriter;
    private static final String FOLDER_NAME = "Dead_Reckoning/Calibration_Fragment";
    private static final String[] DATA_FILE_NAMES = {"Gyroscope-Uncalibrated"};
    private static final String[] DATA_FILE_HEADINGS = {"t;uGx;uGy;uGz;xBias;yBias;zBias;"};

    private Sensor sensorGyroscopeU;
    private SensorManager sensorManager;

    private GyroscopeBias gyroscopeBias;

    private ProgressDialog progressDialog;

    private Handler handler;

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
                sensorManager.registerListener(GyroCalibrateDialogFragment.this, sensorGyroscopeU, SensorManager.SENSOR_DELAY_FASTEST);
                progressDialog = ProgressDialog.show(getActivity(), "Calibrating", "Please wait.", true, false);
            }
        });

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE_UNCALIBRATED) {
            if (gyroscopeBias.calcBias(event.values)) {
                dataFileWriter.writeToFile("Gyroscope_Uncalibrated", "Calculated biases: " + Arrays.toString(gyroscopeBias.getBias()));
                dismissDialog();
            } else {
                ArrayList<Float> dataValues = ExtraFunctions.arrayToList(event.values);
                dataValues.add(0, (float) event.timestamp);
                dataFileWriter.writeToFile("Gyroscope-Uncalibrated", dataValues);
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
