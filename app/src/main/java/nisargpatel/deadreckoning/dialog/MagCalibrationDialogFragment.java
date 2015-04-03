package nisargpatel.deadreckoning.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
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
import nisargpatel.deadreckoning.heading.MagneticFieldBias;

public class MagCalibrationDialogFragment extends DialogFragment implements SensorEventListener{

    public static final String DIALOG_MESSAGE = "To calibrate: Press \"Start\" and move the phone in figure-8s.";

    private DataFileWriter dataFileWriter;
    private static final String FOLDER_NAME = "Dead_Reckoning/Calibration_Fragment";
    private static final String[] DATA_FILE_NAMES = {"Magnetic_Field_Uncalibrated"};
    private static final String[] DATA_FILE_HEADINGS = {"t;uMx;uMy;uMz;xBias;yBias;zBias;"};

    private boolean isRunning;

    private Sensor sensorMagneticField;
    private SensorManager sensorManager;

    private MagneticFieldBias magneticFieldBias;

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

        magneticFieldBias = new MagneticFieldBias();

        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        sensorMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED);

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
                if (isRunning) {
                    dataFileWriter.writeToFile("Magnetic_Field_Uncalibrated", "Calculated_bias: " + Arrays.toString(magneticFieldBias.getBias()));
                    dismissDialog();
                } else {
                    sensorManager.registerListener(MagCalibrationDialogFragment.this, sensorMagneticField, SensorManager.SENSOR_DELAY_FASTEST);

                    isRunning = true;
                    buttonNeutral.setText("Stop Calibration");
                }
            }
        });

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED) {

            float[] magValues = {event.values[0], event.values[1], event.values[2]};

            magneticFieldBias.calcBias(magValues);

            //storing data to file
            ArrayList<Float> dataValues = ExtraFunctions.arrayToList(magValues);
            dataValues.add(0, (float) event.timestamp);
            dataFileWriter.writeToFile("Magnetic_Field_Uncalibrated", dataValues);
        }

    }

    private void dismissDialog() {
        sensorManager.unregisterListener(this);
        handler.sendEmptyMessage(0);
        dismiss();
    }

    public float[] getMagBias() {
        return magneticFieldBias.getBias();
    }

}
