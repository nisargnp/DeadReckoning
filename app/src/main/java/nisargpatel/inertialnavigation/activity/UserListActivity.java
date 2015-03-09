package nisargpatel.inertialnavigation.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import nisargpatel.inertialnavigation.R;
import nisargpatel.inertialnavigation.dialog.UserDetailsFragment;
import nisargpatel.inertialnavigation.dialog.AccessUserFragment;
import nisargpatel.inertialnavigation.extra.NPExtras;

public class UserListActivity extends FragmentActivity{

    private static final int REQUEST_CODE = 0;

    private ListView myList;

    public static ArrayList<String> userList;
    public static ArrayList<String> strideList;

    private static SharedPreferences.Editor sharedPreferencesEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        SharedPreferences sharedPreference = getSharedPreferences(NPExtras.PREFS_NAME, 0);
        sharedPreferencesEditor = sharedPreference.edit();
        sharedPreferencesEditor.apply();

        userList = NPExtras.getArrayFromSharedPreferences("user_list", sharedPreference);
        strideList = NPExtras.getArrayFromSharedPreferences("stride_list", sharedPreference);

        myList = (ListView) findViewById(R.id.listView);
        refreshListView();

        myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Log.d("itemClick", "click position: " + position);

                Intent myIntent = new Intent(UserListActivity.this, GraphActivity.class);
                myIntent.putExtra("user_name", userList.get(position));
                myIntent.putExtra("stride_length", Float.parseFloat(strideList.get(position)));
                startActivity(myIntent);

            }
        });

        myList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                Log.d("itemClick", "long click position: " + position);

                AccessUserFragment accessUserDialog = new AccessUserFragment();
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

                UserDetailsFragment userDetailsDialog = new UserDetailsFragment();
                userDetailsDialog.setHandler(new UserSettingsDialogHandler(UserListActivity.this, myList));
                userDetailsDialog.addingUser(true);
                userDetailsDialog.show(getSupportFragmentManager(), "Calibration");

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

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

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

        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            String userName = data.getStringExtra("user_name");
            String strideLength = String.valueOf(data.getDoubleExtra("stride_length", 2.5));

            userList.add(userName);
            strideList.add(strideLength);
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

    public static void updatePrefs() {
        NPExtras.addArrayToSharedPreferences("user_list", userList, sharedPreferencesEditor);
        NPExtras.addArrayToSharedPreferences("stride_list", strideList, sharedPreferencesEditor);
    }

    //this handler will let UserListActivity know when the UserDetailsFragment dialog has been dismissed
    private static class UserSettingsDialogHandler extends Handler {

        Context context;
        ListView myList;

        public UserSettingsDialogHandler(Context context, ListView myList) {
            this.context = context;
            this.myList = myList;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            refreshListView();

            //Starting the CalibrationActivity from the context of UserListActivity so that
            //CalibrationActivity triggers UserListActivity's OnActivityResult() on finish()
            if (msg.getData().getBoolean("adding_user", false)) {
                Intent myIntent = new Intent(context, CalibrationActivity.class);
                myIntent.putExtra("user_name", msg.getData().getString("user_name"));
                ((FragmentActivity)context).startActivityForResult(myIntent, REQUEST_CODE);
            }

        }

        private void refreshListView() {
            ArrayAdapter<String> listAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, userList);
            myList.setAdapter(listAdapter);
        }

    }


}
