package nisargpatel.inertialnavigation.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import nisargpatel.inertialnavigation.R;

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
                //Do Stuff On Button Click
            }
        });

        return builder.create();
    }

    public void setDialogMessage(String message) {
        this.message = message;
    }

}
