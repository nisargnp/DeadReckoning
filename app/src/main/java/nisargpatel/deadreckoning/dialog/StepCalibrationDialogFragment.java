package nisargpatel.deadreckoning.dialog;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import nisargpatel.deadreckoning.interfaces.OnPreferredStepCounterListener;

public class StepCalibrationDialogFragment extends DialogFragment{

    private OnPreferredStepCounterListener onPreferredStepCounterListener;
    private String[] stepList;

    public StepCalibrationDialogFragment() {}

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Pick the sensitivity that best matches your step count:")
                .setItems(stepList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onPreferredStepCounterListener.onPreferredStepCounter(which);
                    }
                });

        return builder.create();
    }

    public void setOnPreferredStepCounterListener(OnPreferredStepCounterListener onPreferredStepCounterListener) {
        this.onPreferredStepCounterListener = onPreferredStepCounterListener;
    }

    public void setStepList(String[] stepList) {
        this.stepList = stepList;
    }

}
