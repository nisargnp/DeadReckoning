package nisargpatel.deadreckoning.dialog;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

public class StepCalibrationDialogFragment extends DialogFragment{

    private Handler handler;
    private String[] stepList;

    public StepCalibrationDialogFragment(String list[], Handler handler) {
        stepList = new String[list.length];

        for (int i = 0; i < list.length; i++)
            stepList[i] = String.valueOf(list[i]);

        this.handler = handler;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Pick a sensitivity that best matches your step count (sensitivity : step_count)")
                .setItems(stepList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (handler != null)
                            //Don't need to send any info other than index (which), so an empty message will be sent with a "what" signature that defines the index (which)
                            handler.sendEmptyMessage(which);
                        else
                            Toast.makeText(getActivity(), "Handler not defined.", Toast.LENGTH_SHORT).show();
                    }
                });


        return builder.create();
    }

}
