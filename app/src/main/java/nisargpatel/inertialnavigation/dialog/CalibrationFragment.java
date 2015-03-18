package nisargpatel.inertialnavigation.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

public class CalibrationFragment extends DialogFragment implements SensorEventListener{

    public static final String DIALOG_MESSAGE = "Set phone on flat surface away from large metal objects and press \"Okay\" to calibrate.";


    SensorManager sensorManager;
    private Sensor sensorGravity;
    private Sensor sensorGyroscopeUncal;
    private Sensor sensorMagneticFieldUncal;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        sensorManager = (SensorManager) getActivity().getSystemService(Service.SENSOR_SERVICE);
        sensorGravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        sensorGyroscopeUncal = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED);
        sensorMagneticFieldUncal = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder
                .setMessage(DIALOG_MESSAGE)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                })
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sensorManager.registerListener(CalibrationFragment.this, sensorGravity, SensorManager.SENSOR_DELAY_FASTEST);
                        sensorManager.registerListener(CalibrationFragment.this, sensorGyroscopeUncal, SensorManager.SENSOR_DELAY_FASTEST);
                        sensorManager.registerListener(CalibrationFragment.this, sensorMagneticFieldUncal, SensorManager.SENSOR_DELAY_FASTEST);

                        //TODO: implement gyroscope and magnetic field calibration
                    }
                });

        return dialogBuilder.create();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {}

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
