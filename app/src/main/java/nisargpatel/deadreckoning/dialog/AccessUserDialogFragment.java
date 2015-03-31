package nisargpatel.deadreckoning.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import nisargpatel.deadreckoning.activity.UserActivity;

public class AccessUserDialogFragment extends DialogFragment {

    private String userName;
    private String strideLength;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final String DIALOG_MESSAGE = "Go to " + userName + "'s settings?";

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
                        Intent myIntent = new Intent(getActivity(), UserActivity.class);
                        myIntent.putExtra("user_name", userName);
                        myIntent.putExtra("stride_length", strideLength);
                        startActivity(myIntent);

                        dismiss();
                    }
                });


        //return the dialog builder
        return dialogBuilder.create();
    }


    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setStrideLength(String strideLength) {
        this.strideLength = strideLength;
    }

}
