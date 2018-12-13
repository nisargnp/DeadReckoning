package nisargpatel.deadreckoning.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import nisargpatel.deadreckoning.R;
import nisargpatel.deadreckoning.activity.StepCalibrationActivity;
import nisargpatel.deadreckoning.interfaces.OnUserUpdateListener;

public class UserDetailsDialogFragment extends DialogFragment {

    public static final String USER_TAG = "USER";
    public static final String STRIDE_LENGTH_TAG = "STRIDE_LENGTH_TAG";
    public static final String PREFERRED_STEP_COUNTER = "PREFERRED_STEP_COUNTER";

    private final static String CALIBRATION_MESSAGE = "Enter stride length manually, or go to Calibration Mode for automatic stride length calculation:";

    private OnUserUpdateListener onUserUpdateListener;
    private boolean addingUser;
    private String userName;

    public UserDetailsDialogFragment() {}

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Context context = getActivity();

        //create dialog view
        View dialogBox = View.inflate(context, R.layout.dialog_user_details, null);

        //set dialog view
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(dialogBox);

        //defining views
        final TextView textName = dialogBox.findViewById(R.id.textDialogName);
        final EditText textStrideLength = dialogBox.findViewById(R.id.textDialogStride);
        final TextView textStrideLengthMessage = dialogBox.findViewById(R.id.textDialogStrideMessage);

        //setting the custom message
        textStrideLengthMessage.setText(CALIBRATION_MESSAGE);

        //if not adding a new user, disable the name EditText
        if (!addingUser) {
            textName.setEnabled(false);
            textName.setText(userName);
        }

        alertDialogBuilder
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                })
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String userName = textName.getText().toString();
                        String strideLength = textStrideLength.getText().toString();

                        if (checkInvalidUserName(userName)) {
                            Toast.makeText(getActivity(), "Must enter valid name.", Toast.LENGTH_SHORT).show();
                        } else if (checkInvalidStrideLength(strideLength)) {
                            Toast.makeText(getActivity(), "Must enter valid stride length.", Toast.LENGTH_SHORT).show();
                        } else {

                            Bundle bundle = new Bundle();

                            if (addingUser) {
                                bundle.putString(UserDetailsDialogFragment.USER_TAG, userName);
                                bundle.putString(UserDetailsDialogFragment.STRIDE_LENGTH_TAG, strideLength);
                                bundle.putString(UserDetailsDialogFragment.PREFERRED_STEP_COUNTER, "default");

                            } else {
                                bundle.putString(UserDetailsDialogFragment.USER_TAG, userName);
                                bundle.putString(UserDetailsDialogFragment.STRIDE_LENGTH_TAG, strideLength);
                            }

                            onUserUpdateListener.onUserUpdateListener(bundle);

                            dismiss();

                        }

                    }
                })
                .setNeutralButton("Stride Length Calc", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String userName = textName.getText().toString();

                        if (checkInvalidUserName(userName)) {
                            Toast.makeText(getActivity(), "Must enter valid name.", Toast.LENGTH_SHORT).show();
                        } else {
                            Intent myIntent = new Intent(context, StepCalibrationActivity.class);
                            myIntent.putExtra("user_name", userName);
                            ((Activity)context).startActivityForResult(myIntent, 0);
                            dismiss();
                        }

                    }
                });

        return alertDialogBuilder.create();
    }

    public void setOnUserUpdateListener(OnUserUpdateListener onUserUpdateListener) {
        this.onUserUpdateListener = onUserUpdateListener;
    }

    public void setAddingUser(boolean addingUser) {
        this.addingUser = addingUser;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    private boolean checkInvalidStrideLength(String strideLength) {
        return strideLength.length() == 0;
    }

    private boolean checkInvalidUserName(String userName) {
        return userName.length() == 0;
    }

}
