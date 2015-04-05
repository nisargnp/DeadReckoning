package nisargpatel.deadreckoning.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

import nisargpatel.deadreckoning.R;
import nisargpatel.deadreckoning.dialog.AccessUserDialogFragment;
import nisargpatel.deadreckoning.dialog.SensorCalibrationDialogFragment;
import nisargpatel.deadreckoning.dialog.UserDetailsDialogFragment;
import nisargpatel.deadreckoning.extra.ExtraFunctions;

public class UserListActivity extends FragmentActivity{

    private static final int REQUEST_CODE = 0;

    private ListView myList;

    public static ArrayList<String> userList;
    public static ArrayList<String> strideList;
    public static ArrayList<String> preferredStepCounterList;

    private static SharedPreferences.Editor sharedPreferencesEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        SharedPreferences sharedPreference = getSharedPreferences(ExtraFunctions.PREFS_NAME, 0);
        sharedPreferencesEditor = sharedPreference.edit();
        sharedPreferencesEditor.apply();

        checkSensor(sharedPreferencesEditor, "step_detector", Sensor.TYPE_STEP_DETECTOR);

        userList = ExtraFunctions.getArrayFromSharedPreferences("user_list", sharedPreference);
        strideList = ExtraFunctions.getArrayFromSharedPreferences("stride_list", sharedPreference);
        preferredStepCounterList = ExtraFunctions.getArrayFromSharedPreferences("preferred_step_counter", sharedPreference);

        myList = (ListView) findViewById(R.id.listView);
        refreshListView();

        //clicking on a menu item
        myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Log.d("User_List_Activity", "click position: " + position);

                SensorCalibrationDialogFragment calibrationDialog = new SensorCalibrationDialogFragment();
                Bundle mBundle = new Bundle();
                mBundle.putString("user_name", userList.get(position));
                mBundle.putFloat("stride_length", Float.parseFloat(strideList.get(position)));
                mBundle.putString("preferred_step_counter", preferredStepCounterList.get(position));
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

        Button buttonNewUser = (Button) findViewById(R.id.buttonNewUser);
        buttonNewUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ListView myList = (ListView) findViewById(R.id.listView);
                myList.setAdapter(null);

                ArrayAdapter<String> listAdapter = new ArrayAdapter<>(UserListActivity.this, android.R.layout.simple_list_item_1, userList);
                myList.setAdapter(listAdapter);

                UserDetailsDialogFragment userDetailsDialog = new UserDetailsDialogFragment();
                userDetailsDialog.setHandler(new UserListHandler(UserListActivity.this, myList));
                userDetailsDialog.addingUser(true);
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
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

        refreshListView();
    }

    private void refreshListView() {
        ArrayAdapter<String> listAdapter = new ArrayAdapter<>(UserListActivity.this, android.R.layout.simple_list_item_1, userList);
        myList.setAdapter(listAdapter);
    }

    private void checkSensor(SharedPreferences.Editor sharedPreferencesEditor, String sensorName, int sensorType) {
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(sensorType) != null)
            sharedPreferencesEditor.putBoolean(sensorName, true);
        else
            sharedPreferencesEditor.putBoolean(sensorName, false);
    }

    public static void updatePrefs() {
        ExtraFunctions.addArrayToSharedPreferences("user_list", userList, sharedPreferencesEditor);
        ExtraFunctions.addArrayToSharedPreferences("stride_list", strideList, sharedPreferencesEditor);
        ExtraFunctions.addArrayToSharedPreferences("preferred_step_counter", preferredStepCounterList, sharedPreferencesEditor);
    }

    //this handler will let UserListActivity know when the UserDetailsDialogFragment dialog has been dismissed
    private static class UserListHandler extends Handler {

        Context context;
        ListView listView;

        public UserListHandler(Context context, ListView listView) {
            this.context = context;
            this.listView = listView;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            refreshListView();

            //Starting the StepCalibrationActivity from the context of UserListActivity so that
            //StepCalibrationActivity triggers UserListActivity's OnActivityResult() on finish()
            if (msg.getData().getBoolean("adding_user", false)) {
                Intent myIntent = new Intent(context, StepCalibrationActivity.class);
                myIntent.putExtra("user_name", msg.getData().getString("user_name"));
                ((Activity)context).startActivityForResult(myIntent, REQUEST_CODE); //will trigger this activity's onResult on finish()
            }

        }

        private void refreshListView() {
            ArrayAdapter<String> listAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, userList);
            listView.setAdapter(listAdapter);
        }

    }


}
