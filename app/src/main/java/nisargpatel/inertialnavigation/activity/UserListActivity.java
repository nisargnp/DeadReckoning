package nisargpatel.inertialnavigation.activity;

import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import nisargpatel.inertialnavigation.R;
import nisargpatel.inertialnavigation.dialog.CalibrationFragment;

public class UserListActivity extends FragmentActivity {

    public static String[] users = {"Default", "Custom User 1", "Custom User 2"};

    private static final String PREFS_NAME = "Inertial Navigation Preferences";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor sharedPreferencesEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        sharedPreferences = getSharedPreferences(PREFS_NAME, 0);
        sharedPreferencesEditor = sharedPreferences.edit();
        sharedPreferencesEditor.apply();

        TestList testing = new TestList();
        getFragmentManager().beginTransaction().add(android.R.id.content, testing).commit();

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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void openDialog() {

        LayoutInflater layoutInflater = LayoutInflater.from(UserListActivity.this);
        View dialogBox = layoutInflater.inflate(R.layout.calibration_dialog, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(UserListActivity.this);
        alertDialogBuilder.setView(dialogBox);

        final EditText textStrideLength = (EditText) dialogBox.findViewById(R.id.textDialogStride);

        alertDialogBuilder
                .setCancelable(false)
                .setNegativeButton("Ok",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                float strideLength = Float.valueOf(textStrideLength.getText().toString());
                                sharedPreferencesEditor.putFloat("stride_length", strideLength);
                                sharedPreferencesEditor.apply();

                                Toast.makeText(getApplicationContext(), "Stride length set: " + strideLength + " ft/sec.", Toast.LENGTH_SHORT).show();
                                Intent myIntent = new Intent(UserListActivity.this, GraphActivity.class);
                                startActivity(myIntent);
                            }
                        })
                .setPositiveButton("Auto-Calibration Mode",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent myIntent = new Intent(UserListActivity.this, GraphActivity.class);
                                startActivity(myIntent);

                                myIntent = new Intent(UserListActivity.this, CalibrationActivity.class);
                                startActivity(myIntent);
                            }
                        });

        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();
    }

    private static CalibrationFragment calibrationDialog;

    public static class TestList extends ListFragment {
        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            setListAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, UserListActivity.users));
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            super.onListItemClick(l, v, position, id);

            FragmentActivity myActivity = (FragmentActivity)v.getContext();

            calibrationDialog = new CalibrationFragment();
            calibrationDialog.setDialogMessage("This is a test.");

            switch (position) {
                case 0:
                    calibrationDialog.show(myActivity.getSupportFragmentManager(), "Calibration");
                    break;
                case 1:
                    Toast.makeText(getActivity(), "Use Default.", Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    Toast.makeText(getActivity(), "Use Default.", Toast.LENGTH_SHORT).show();
                    break;
            }

        }
    }
}
