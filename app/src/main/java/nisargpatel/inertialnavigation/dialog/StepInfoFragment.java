package nisargpatel.inertialnavigation.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import nisargpatel.inertialnavigation.R;

//creating a new DialogFragment to output a message
public class StepInfoFragment extends DialogFragment{

    private String message;

    AlertDialog.Builder builder;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message).setNeutralButton(R.string.okay, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Don't need to do anything on button click except exit the DialogFragment
            }
        });
        return builder.create();
    }

    //get the message to be outputted by the DialogFragment
    public void setDialogMessage(String message) {
        this.message = message;
    }

}
