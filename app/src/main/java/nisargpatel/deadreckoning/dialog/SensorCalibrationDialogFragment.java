package nisargpatel.deadreckoning.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import nisargpatel.deadreckoning.activity.GraphActivity;
import nisargpatel.deadreckoning.extra.ExtraFunctions;
import nisargpatel.deadreckoning.filewriting.DataFileWriter;
import nisargpatel.deadreckoning.heading.InitialOrientation;

public class SensorCalibrationDialogFragment extends DialogFragment implements SensorEventListener {

    public static final String DIALOG_MESSAGE = "Calibrate phone manually, or press \"Cancel\" to use Android-calibrated sensors.";

    private static final String FOLDER_NAME = "Dead_Reckoning/Calibration_Fragment";
    private static final String[] DATA_FILE_NAMES = {"Initial_Orientation"};
    private static final String[] DATA_FILE_HEADINGS = {"Initial_Orientation"};

    private DataFileWriter dataFileWriter;

    private SensorManager sensorManager;

    private float[] currGravityValues;
    private float[] currMagneticFieldValues;

    private static float[] gyroBias;
    private static float[] magBias;

    private int isCalibrating;

    private static final int EXIT_DIALOG = 0;
    private static final int CALIBRATING = 1;
    private static final int NOT_CALIBRATING = 2;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {



        isCalibrating = EXIT_DIALOG;

        sensorManager = (SensorManager) getActivity().getSystemService(Service.SENSOR_SERVICE);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder
                .setMessage(DIALOG_MESSAGE)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //this onClick() will be overridden during onStart()
                    }
                })
                .setPositiveButton("Calibrate", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //this onClick() will be overridden during onStart()
                    }
                });

        return dialogBuilder.create();
    }

    //defining button press actions
    @Override
    public void onStart() {
        super.onStart();

        //overriding the button listeners because by default, the dialog dismisses itself as soon as a button is pressed
        //we want to wait until the calibration is finished before manually dismissing the dialog
        AlertDialog alertDialog = (AlertDialog)getDialog();

        if (alertDialog != null) {
            Button negativeButton = alertDialog.getButton(Dialog.BUTTON_NEGATIVE);
            negativeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isCalibrating = NOT_CALIBRATING;
                    dismiss();
                }
            });

            Button positiveButton = alertDialog.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    createDataFiles();
                    sensorManager.registerListener(SensorCalibrationDialogFragment.this, sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), SensorManager.SENSOR_DELAY_FASTEST);
                    sensorManager.registerListener(SensorCalibrationDialogFragment.this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED), SensorManager.SENSOR_DELAY_FASTEST);
                    isCalibrating = CALIBRATING;
                    startCalibrationDialogs();
                }
            });
        }

    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        if (isCalibrating != EXIT_DIALOG) {
            Log.d("handler", Arrays.toString(magBias));
            Log.d("handler", Arrays.toString(gyroBias));
            Log.d("handler", Arrays.toString(currMagneticFieldValues));

            startGraphActivity();
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onSensorChanged(SensorEvent event) {

        ArrayList<Float> dataValues = ExtraFunctions.arrayToList(event.values);
        dataValues.add(0, (float) event.timestamp);

        if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
            currGravityValues = event.values.clone();
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED) {
            currMagneticFieldValues = event.values.clone();
        }

    }

    private void startCalibrationDialogs() {

        GyroCalibrationDialogFragment gyroCalibrateDialog = new GyroCalibrationDialogFragment();
        gyroCalibrateDialog.setHandler(new SensorCalibrationHandler(getActivity(), getDialog(), null, gyroCalibrateDialog));

        MagCalibrationDialogFragment magCalibrateDialog = new MagCalibrationDialogFragment();
        magCalibrateDialog.setHandler(new SensorCalibrationHandler(getActivity(), getDialog(), magCalibrateDialog, gyroCalibrateDialog));

        magCalibrateDialog.show(getActivity().getFragmentManager(), "Magnetic Field Calibrate");
    }

    private Intent addExtras(Intent myIntent) {

        myIntent.putExtra("user_name", getArguments().getString("user_name", "Default"));
        myIntent.putExtra("stride_length", getArguments().getDouble("stride_length", 2.5));
        myIntent.putExtra("preferred_step_counter", getArguments().getString("preferred_step_counter"));

        if (isCalibrating == CALIBRATING) {
            float[][] initialOrientation = InitialOrientation.calcOrientation(currGravityValues, currMagneticFieldValues, magBias);
            float[] initMagValues = {currMagneticFieldValues[0], currMagneticFieldValues[1], currMagneticFieldValues[2]};

            dataFileWriter.writeToFile("Initial_Orientation", "initGravity: " + Arrays.toString(currGravityValues));
            dataFileWriter.writeToFile("Initial_Orientation", "initMag: " + Arrays.toString(initMagValues));
            dataFileWriter.writeToFile("Initial_Orientation", "magBias: " + Arrays.toString(magBias));
            dataFileWriter.writeToFile("Initial_Orientation", "initOrientation: " + Arrays.deepToString(initialOrientation));

            myIntent.putExtra("is_calibrated", true);
            myIntent.putExtra("gyroscope_bias", gyroBias);
            myIntent.putExtra("initial_orientation", initialOrientation); //float[][] will get serialized
        } else if (isCalibrating == NOT_CALIBRATING) {
            myIntent.putExtra("is_calibrated", false);
            myIntent.putExtra("gyroscope_bias", new float[3]);
            myIntent.putExtra("initial_orientation", ExtraFunctions.IDENTITY_MATRIX); //float[][] will get serialized
        }

        return myIntent;

    }

    private void startGraphActivity() {
        sensorManager.unregisterListener(this);

        //start graph activity
        Intent myIntent = new Intent(getActivity(), GraphActivity.class);
        myIntent = addExtras(myIntent);
        startActivity(myIntent);

    }

    private void createDataFiles() {
        try {
            dataFileWriter = new DataFileWriter(FOLDER_NAME, ExtraFunctions.arrayToList(DATA_FILE_NAMES), ExtraFunctions.arrayToList(DATA_FILE_HEADINGS));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void setMagBias(float[] mBias) {
        magBias = mBias;
    }

    private static void setGyroBias(float[] mBias) {
        gyroBias = mBias;
    }

    private static class SensorCalibrationHandler extends Handler {

        private Context context;
        private Dialog dialog;

        private MagCalibrationDialogFragment magCalibrateDialog;
        private GyroCalibrationDialogFragment gyroCalibrateDialog;

        public SensorCalibrationHandler(Context context, Dialog dialog, MagCalibrationDialogFragment magCalibrateDialog, GyroCalibrationDialogFragment gyroCalibrateDialog) {
            this.context = context;
            this.dialog = dialog;

            this.magCalibrateDialog = magCalibrateDialog;
            this.gyroCalibrateDialog = gyroCalibrateDialog;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            //when the magnetometer values come in, start the GyroCalibrationDialog
            if (msg.what == 0) {
                setMagBias(magCalibrateDialog.getMagBias());
                gyroCalibrateDialog.show(((Activity)context).getFragmentManager(), "Gyroscope Calibrate");
            } else if (msg.what == 1) {
                setGyroBias(gyroCalibrateDialog.getGyroBias());
                dialog.dismiss();
            }

        }

    }

}
