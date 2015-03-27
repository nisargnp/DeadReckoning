package nisargpatel.deadreckoning.dialog;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class StepCounterFragment extends DialogFragment{

    private CharSequence[] myList;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        myList = new CharSequence[3];
        myList[0] = "apple";
        myList[1] = "potato";
        myList[2] = "banana";

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Pick one: ")
                .setItems(myList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });


        return builder.create();
    }


}
