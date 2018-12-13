package nisargpatel.deadreckoning.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.Arrays;

import nisargpatel.deadreckoning.activity.GraphActivity;

public class SensorCalibrationDialogFragment extends DialogFragment {

    public static final String DIALOG_MESSAGE = "Calibrate phone manually, or press \"Auto\" to use Android-calibrated sensors.";

    private static float[] gyroBias;
    private static float[] magBias;

    private int isCalibrating;

    private static final int EXIT_DIALOG = 0;
    private static final int CALIBRATING = 1;
    private static final int NOT_CALIBRATING = 2;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        isCalibrating = EXIT_DIALOG;

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder
                .setMessage(DIALOG_MESSAGE)
                .setNegativeButton("Manually Calibrate", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //this onClick() will be overridden during onStart()
                    }
                })
                .setPositiveButton("Auto", new DialogInterface.OnClickListener() {
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
                    isCalibrating = CALIBRATING;
                    startCalibrationDialogs();
                }
            });

            Button positiveButton = alertDialog.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isCalibrating = NOT_CALIBRATING;
                    dismiss();
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
            startGraphActivity();
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

        myIntent.putExtra("user_name", getArguments().getString("user_name", "unknown"));
        myIntent.putExtra("stride_length", getArguments().getFloat("stride_length", 2.5f));
        myIntent.putExtra("preferred_step_counter", getArguments().getString("preferred_step_counter", "default"));
        myIntent.putExtra("step_detector", getArguments().getBoolean("step_detector", false));

        if (isCalibrating == CALIBRATING) {
            myIntent.putExtra("is_calibrated", true);
            myIntent.putExtra("gyro_bias", gyroBias);
            myIntent.putExtra("mag_bias", magBias);
        } else if (isCalibrating == NOT_CALIBRATING) {
            myIntent.putExtra("is_calibrated", false);
            myIntent.putExtra("gyro_bias", new float[3]);
            myIntent.putExtra("mag_bias", new float[3]);
        }

        return myIntent;

    }

    private void startGraphActivity() {
        Intent myIntent = new Intent(getActivity(), GraphActivity.class);
        myIntent = addExtras(myIntent);
        startActivity(myIntent);
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

        SensorCalibrationHandler(Context context, Dialog dialog, MagCalibrationDialogFragment magCalibrateDialog, GyroCalibrationDialogFragment gyroCalibrateDialog) {
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
