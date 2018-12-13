package nisargpatel.deadreckoning.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

import nisargpatel.deadreckoning.R;
import nisargpatel.deadreckoning.dialog.UserDetailsDialogFragment;
import nisargpatel.deadreckoning.extra.ExtraFunctions;
import nisargpatel.deadreckoning.interfaces.OnUserUpdateListener;

public class UserActivity extends Activity implements OnUserUpdateListener {

    private static final int REQUEST_CODE = 0;

    TextView textStrideLength;

    private String userName;

    private SharedPreferences.Editor sharedPreferencesEditor;
    private ArrayList<String> userList;
    private ArrayList<String> strideList;
    private ArrayList<String> preferredStepCounterList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        SharedPreferences sharedPreference = getSharedPreferences(ExtraFunctions.PREFS_NAME, 0);
        sharedPreferencesEditor = sharedPreference.edit();
        sharedPreferencesEditor.apply();

        userList = ExtraFunctions.getArrayFromSharedPreferences("user_list", sharedPreference);
        strideList = ExtraFunctions.getArrayFromSharedPreferences("stride_list", sharedPreference);
        preferredStepCounterList = ExtraFunctions.getArrayFromSharedPreferences("preferred_step_counter", sharedPreference);

        Intent myIntent = getIntent();
        String strideLength = myIntent.getStringExtra("stride_length");
        userName = myIntent.getStringExtra("user_name");

        TextView textUserName = findViewById(R.id.textUserName);
        textUserName.setText(userName);

        textStrideLength = findViewById(R.id.textUserStrideLength);
        setStrideLengthText(strideLength);

        findViewById(R.id.buttonUserCalibration).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserDetailsDialogFragment userDetailsDialog = new UserDetailsDialogFragment();
                userDetailsDialog.setOnUserUpdateListener(UserActivity.this);
                userDetailsDialog.setAddingUser(false);
                userDetailsDialog.setUserName(userName);
                userDetailsDialog.show(getFragmentManager(), "Calibration");
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_user, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //remove the current user_name and his/her stride_length from the global database
        if (id == R.id.delete_user) {
            int index = userList.indexOf(userName);
            userList.remove(index);
            strideList.remove(index);
            updatePrefs();
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            double strideLength = data.getDoubleExtra("stride_length", 2.5);
            int index = userList.indexOf(userName);
            strideList.set(index, String.valueOf(strideLength));
            setStrideLengthText("" + strideLength);

            updatePrefs();
        }

    }

    @Override
    public void onUserUpdateListener(Bundle bundle) {
        String strideLength = bundle.getString(UserDetailsDialogFragment.STRIDE_LENGTH_TAG);
        int index = userList.indexOf(userName);
        strideList.set(index, String.valueOf(strideLength));
        setStrideLengthText(strideLength != null ? strideLength : "0");

        updatePrefs();
    }

    private void updatePrefs() {
        ExtraFunctions.addArrayToSharedPreferences("user_list", userList, sharedPreferencesEditor);
        ExtraFunctions.addArrayToSharedPreferences("stride_list", strideList, sharedPreferencesEditor);
        ExtraFunctions.addArrayToSharedPreferences("preferred_step_counter", preferredStepCounterList, sharedPreferencesEditor);
    }

    private void setStrideLengthText(String strideLength) {
        if (strideLength.length() > 3)
            textStrideLength.setText(strideLength.substring(0,3));
        else
            textStrideLength.setText(strideLength);
    }

}
