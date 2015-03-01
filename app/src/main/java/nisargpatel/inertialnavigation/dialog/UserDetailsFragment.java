package nisargpatel.inertialnavigation.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import nisargpatel.inertialnavigation.R;
import nisargpatel.inertialnavigation.activity.CalibrationActivity;
import nisargpatel.inertialnavigation.activity.UserListActivity;

public class UserDetailsFragment extends DialogFragment {

    private final static int REQUEST_CODE = 0;
    private final static String CALIBRATION_MESSAGE = "Enter stride length manually, or go to Calibration Mode for automatic stride length calculation:";

    private Handler handler;
    private View dialogBox;

    private boolean addingUser;

    private String userName = null;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Context context = getActivity();

        //create dialog view
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        dialogBox = layoutInflater.inflate(R.layout.dialog_user_details, null);

        //set dialog view
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(dialogBox);

        //defining views
        final TextView textName = (TextView) dialogBox.findViewById(R.id.textDialogName);
        final EditText textStrideLength = (EditText) dialogBox.findViewById(R.id.textDialogStride);
        final TextView textStrideLengthMessage = (TextView) dialogBox.findViewById(R.id.textDialogStrideMessage);

        //setting the custom message
        textStrideLengthMessage.setText(CALIBRATION_MESSAGE);

        //if not adding a new user, disable the name EditText
        if (!addingUser) {
            textName.setEnabled(false);
            textName.setText(userName);
        }

        alertDialogBuilder
                .setCancelable(false)
                .setNegativeButton("Ok",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                String userName = textName.getText().toString();
                                Double strideLength = Double.parseDouble(textStrideLength.getText().toString());

                                Toast.makeText(context, "Stride length set: " + strideLength + " ft/sec.", Toast.LENGTH_SHORT).show();

                                if (addingUser) {
                                    UserListActivity.userList.add(textName.getText().toString());
                                    UserListActivity.strideList.add(textStrideLength.getText().toString());
                                } else {
                                    int index = UserListActivity.userList.indexOf(userName);
                                    UserListActivity.strideList.set(index, String.valueOf(strideLength));
                                }

                                UserListActivity.updatePrefs();

                                dismiss();

                            }
                        })
                .setPositiveButton("Auto-Calibration Mode",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                String userName = textName.getText().toString();

                                Bundle bundle = new Bundle();
                                bundle.putBoolean("adding_user", true);
                                bundle.putString("user_name", userName);

                                Message msg = new Message();
                                msg.setData(bundle);
                                handler.sendMessage(msg);


                            }
                        });

        return alertDialogBuilder.create();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        if (handler != null) {
            //lets the message that the dialog has been dismissed via the handler
            handler.sendEmptyMessage(0);
        }

    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public void addingUser(boolean addingUser) {
        this.addingUser = addingUser;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
