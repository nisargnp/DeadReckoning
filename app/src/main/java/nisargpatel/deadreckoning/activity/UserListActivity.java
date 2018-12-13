package nisargpatel.deadreckoning.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import nisargpatel.deadreckoning.R;
import nisargpatel.deadreckoning.dialog.AccessUserDialogFragment;
import nisargpatel.deadreckoning.dialog.SensorCalibrationDialogFragment;
import nisargpatel.deadreckoning.dialog.UserDetailsDialogFragment;
import nisargpatel.deadreckoning.extra.ExtraFunctions;
import nisargpatel.deadreckoning.interfaces.OnUserUpdateListener;

public class UserListActivity extends AppCompatActivity implements OnUserUpdateListener {

    public static final int REQUEST_CODE = 0;

    private ListView myList;

    private ArrayList<String> userList;
    private ArrayList<String> strideList;
    private ArrayList<String> preferredStepCounterList;

    private SharedPreferences sharedPreference;
    private SharedPreferences.Editor sharedPreferencesEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        sharedPreference = getSharedPreferences(ExtraFunctions.PREFS_NAME, 0);
        sharedPreferencesEditor = sharedPreference.edit();
        sharedPreferencesEditor.apply();

        userList = ExtraFunctions.getArrayFromSharedPreferences("user_list", sharedPreference);
        strideList = ExtraFunctions.getArrayFromSharedPreferences("stride_list", sharedPreference);
        preferredStepCounterList = ExtraFunctions.getArrayFromSharedPreferences("preferred_step_counter", sharedPreference);

        myList = findViewById(R.id.listView);
        refreshListView();

        //clicking on a menu item
        myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Log.d("User_List_Activity", "click position: " + position);

                Bundle mBundle = new Bundle();
                mBundle.putString("user_name", userList.get(position));
                mBundle.putFloat("stride_length", Float.parseFloat(strideList.get(position)));
                mBundle.putString("preferred_step_counter", preferredStepCounterList.get(position));
                mBundle.putBoolean("step_detector", checkSensor(Sensor.TYPE_STEP_DETECTOR));

                SensorCalibrationDialogFragment calibrationDialog = new SensorCalibrationDialogFragment();
                calibrationDialog.setArguments(mBundle);
                calibrationDialog.show(getFragmentManager(), "Calibrate Sensors");

            }
        });

        //long clicking on a menu item
        myList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                Log.d("User_List_Activity", "long click position: " + position);

                AccessUserDialogFragment accessUserDialog = new AccessUserDialogFragment();
                accessUserDialog.setUserName(userList.get(position));
                accessUserDialog.setStrideLength(strideList.get(position));
                accessUserDialog.show(getFragmentManager(), "User Settings");

                return true;

            }
        });

        FloatingActionButton buttonNewUser = findViewById(R.id.fab);
        buttonNewUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                UserDetailsDialogFragment userDetailsDialog = new UserDetailsDialogFragment();
                userDetailsDialog.setOnUserUpdateListener(UserListActivity.this);
                userDetailsDialog.setUserName(null);
                userDetailsDialog.setAddingUser(true);
                userDetailsDialog.show(getFragmentManager(), "Calibration");

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.debug_tools) {
            Intent myIntent = new Intent(UserListActivity.this, DebugToolsActivity.class);
            startActivity(myIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //getting data back from StepCalibrationActivity
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            String userName = data.getStringExtra("user_name");
            String strideLength = String.valueOf(data.getDoubleExtra("stride_length", 2.5));
            String preferredStepCounter = String.valueOf(data.getDoubleExtra("preferred_step_counter", 0));

            userList.add(userName);
            strideList.add(strideLength);
            preferredStepCounterList.add(preferredStepCounter);
            updatePrefs();

            refreshListView();

        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        userList = ExtraFunctions.getArrayFromSharedPreferences("user_list", sharedPreference);
        strideList = ExtraFunctions.getArrayFromSharedPreferences("stride_list", sharedPreference);
        preferredStepCounterList = ExtraFunctions.getArrayFromSharedPreferences("preferred_step_counter", sharedPreference);

        refreshListView();
    }

    @Override
    public void onUserUpdateListener(Bundle bundle) {
        userList.add(bundle.getString(UserDetailsDialogFragment.USER_TAG));
        strideList.add(bundle.getString(UserDetailsDialogFragment.STRIDE_LENGTH_TAG));
        preferredStepCounterList.add("0");

        refreshListView();
        updatePrefs();
    }

    private void refreshListView() {
        ArrayAdapter<String> listAdapter = new ArrayAdapter<>(UserListActivity.this, android.R.layout.simple_list_item_1, userList);
        myList.setAdapter(listAdapter);
    }

    private boolean checkSensor(int sensorType) {
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        return sensorManager != null && (sensorManager.getDefaultSensor(sensorType) != null);
    }

    private void updatePrefs() {
        ExtraFunctions.addArrayToSharedPreferences("user_list", userList, sharedPreferencesEditor);
        ExtraFunctions.addArrayToSharedPreferences("stride_list", strideList, sharedPreferencesEditor);
        ExtraFunctions.addArrayToSharedPreferences("preferred_step_counter", preferredStepCounterList, sharedPreferencesEditor);
    }

}
