package nisargpatel.inertialnavigation.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import nisargpatel.inertialnavigation.R;
import nisargpatel.inertialnavigation.activity.CalibrationActivity;
import nisargpatel.inertialnavigation.activity.GraphActivity;

public class CalibrationFragment extends DialogFragment {

    private String message;

    private static final String PREFS_NAME = "Inertial Navigation Preferences";

    private SharedPreferences.Editor sharedPreferenceEditor;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        sharedPreferenceEditor = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();

        final Context context = getActivity();

        //create dialog view
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View dialogBox = layoutInflater.inflate(R.layout.calibration_dialog, null);

        //set dialog view
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(dialogBox);

        ((TextView) dialogBox.findViewById(R.id.textDialogStrideMessage)).setText(message);

        final EditText textStrideLength = (EditText) dialogBox.findViewById(R.id.textDialogStride);
        alertDialogBuilder
                .setCancelable(false)
                .setNegativeButton("Ok",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                float strideLength = Float.valueOf(textStrideLength.getText().toString());
                                sharedPreferenceEditor.putFloat("stride_length", strideLength);
                                sharedPreferenceEditor.apply();

                                Toast.makeText(context, "Stride length set: " + strideLength + " ft/sec.", Toast.LENGTH_SHORT).show();
                                Intent myIntent = new Intent(context, GraphActivity.class);
                                startActivity(myIntent);
                            }
                        })
                .setPositiveButton("Auto-Calibration Mode",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent myIntent = new Intent(context, GraphActivity.class);
                                startActivity(myIntent);

                                myIntent = new Intent(context, CalibrationActivity.class);
                                startActivity(myIntent);
                            }
                        });

        return alertDialogBuilder.create();
    }

    public void setDialogMessage(String message) {
        this.message = message;
    }
}
