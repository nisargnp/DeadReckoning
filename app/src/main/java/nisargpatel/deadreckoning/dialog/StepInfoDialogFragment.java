package nisargpatel.deadreckoning.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.app.DialogFragment;

import nisargpatel.deadreckoning.R;

//creating a new DialogFragment to output a message
public class StepInfoDialogFragment extends DialogFragment{

    private String message;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder
                .setMessage(message)
                .setNeutralButton(R.string.okay,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dismiss();
                            }
                        });
        return dialogBuilder.create();
    }

    //get the message to be outputted by the DialogFragment
    public void setDialogMessage(String message) {
        this.message = message;
    }

}
