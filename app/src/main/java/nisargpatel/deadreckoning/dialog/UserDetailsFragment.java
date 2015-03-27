package nisargpatel.deadreckoning.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import nisargpatel.deadreckoning.R;
import nisargpatel.deadreckoning.activity.UserListActivity;

public class UserDetailsFragment extends DialogFragment {

    private final static String CALIBRATION_MESSAGE = "Enter stride length manually, or go to Calibration Mode for automatic stride length calculation:";

    private Handler handler;

    private boolean addingUser;

    private String userName = null;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Context context = getActivity();

        //create dialog view
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View dialogBox = layoutInflater.inflate(R.layout.dialog_user_details, null);

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
                .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                })
                .setNegativeButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String userName = textName.getText().toString();
                        String strideLength = textStrideLength.getText().toString();

                        if (!checkValidUserName(userName)) {
                            Toast.makeText(getActivity(), "Must enter valid name.", Toast.LENGTH_SHORT).show();
                        } else if (!checkValidStrideLength(strideLength)) {
                            Toast.makeText(getActivity(), "Must enter valid stride length.", Toast.LENGTH_SHORT).show();
                        } else {

                            if (addingUser) {
                                UserListActivity.userList.add(userName);
                                UserListActivity.strideList.add(strideLength);
                            } else {
                                int index = UserListActivity.userList.indexOf(userName);
                                UserListActivity.strideList.set(index, String.valueOf(strideLength));
                            }

                            UserListActivity.updatePrefs();
                            dismiss();

                        }

                    }
                })
                .setPositiveButton("Auto-Calibrate", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String userName = textName.getText().toString();

                        if (!checkValidUserName(userName)) {
                            Toast.makeText(getActivity(), "Must enter valid name.", Toast.LENGTH_SHORT).show();
                        } else {

                            Bundle bundle = new Bundle();
                            bundle.putBoolean("adding_user", true);
                            bundle.putString("user_name", userName);

                            Message msg = new Message();
                            msg.setData(bundle);
                            handler.sendMessage(msg);

                        }

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

    private boolean checkValidUserName(String userName) {
        return userName.length() != 0;
    }

    private boolean checkValidStrideLength(String strideLength) {
        return strideLength.length() != 0;
    }

}
