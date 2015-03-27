package nisargpatel.deadreckoning.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import nisargpatel.deadreckoning.R;
import nisargpatel.deadreckoning.dialog.UserDetailsFragment;

public class UserActivity extends Activity {

    private static final int REQUEST_CODE = 0;

    TextView textStrideLength;

    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        Intent myIntent = getIntent();
        String strideLength = myIntent.getStringExtra("stride_length");
        userName = myIntent.getStringExtra("user_name");

        TextView textUserName = (TextView) findViewById(R.id.textUserName);
        textStrideLength = (TextView) findViewById(R.id.textUserStrideLength);

        textUserName.setText(userName);

        if (strideLength.length() > 3)
            textStrideLength.setText(strideLength.substring(0,3));
        else
            textStrideLength.setText(strideLength);

        findViewById(R.id.buttonUserCalibration).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserDetailsFragment userDetailsDialog = new UserDetailsFragment();
                userDetailsDialog.addingUser(false);
                userDetailsDialog.setUserName(userName);
                userDetailsDialog.setHandler(new UserSettingsDialogHandler(UserActivity.this, textStrideLength, userName));
                userDetailsDialog.show(getFragmentManager(), "Calibration");
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //remove the current user_name and his/her stride_length from the global database
        if (id == R.id.delete_user) {
            int index = UserListActivity.userList.indexOf(userName);
            UserListActivity.userList.remove(index);
            UserListActivity.strideList.remove(index);
            UserListActivity.updatePrefs();
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            double strideLength = data.getDoubleExtra("stride_length", 2.5);
            int index = UserListActivity.userList.indexOf(userName);
            UserListActivity.strideList.set(index, String.valueOf(strideLength));
        }

        refreshStrideLength();
    }

    private void refreshStrideLength() {
        int index = UserListActivity.userList.indexOf(userName);
        String strideLength = UserListActivity.strideList.get(index);
        if (strideLength.length() > 3)
            textStrideLength.setText(strideLength.substring(0,3));
        else
            textStrideLength.setText(strideLength);
    }


    //this handler will let UserActivity know when the UserDetailsFragment dialog has been dismissed.
    private static class UserSettingsDialogHandler extends Handler {

        private Context context;
        private TextView textStrideLength;
        private String userName;

        public UserSettingsDialogHandler(Context context, TextView textStrideLength, String userName) {
            this.context = context;
            this.textStrideLength = textStrideLength;
            this.userName = userName;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (msg.getData().getBoolean("adding_user", false)) {
                Intent myIntent = new Intent(context, StrideLengthActivity.class);
                myIntent.putExtra("user_name", msg.getData().getString("user_name"));
                ((Activity)context).startActivityForResult(myIntent, REQUEST_CODE);
            } else {
                refreshStrideLength();
            }

        }

        private void refreshStrideLength() {
            int index = UserListActivity.userList.indexOf(userName);
            String strideLength = UserListActivity.strideList.get(index);
            if (strideLength.length() > 3)
                textStrideLength.setText(strideLength.substring(0,3));
            else
                textStrideLength.setText(strideLength);
        }

    }
}
