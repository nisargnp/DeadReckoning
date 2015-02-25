package nisargpatel.inertialnavigation.activity;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import nisargpatel.inertialnavigation.R;

public class UserActivity extends ListActivity {

    String[] users = {"Default", "Custom User 1", "Custom User 2"};

    private static final String PREFS_NAME = "Inertial Navigation Preferences";

    private SharedPreferences.Editor sharedPreferencesEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, 0);
        sharedPreferencesEditor = sharedPreferences.edit();
        sharedPreferencesEditor.apply();

        setListAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, users));

        //onListItemClick(new OrientationTestActivity());

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

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        switch (position) {
            case 0:
                openDialog();
                break;
            case 1:
                Toast.makeText(this, "Use Default.", Toast.LENGTH_SHORT).show();
                break;
            case 2:
                Toast.makeText(this, "Use Default.", Toast.LENGTH_SHORT).show();
                break;
        }

    }

    public void openDialog() {

        LayoutInflater layoutInflater = LayoutInflater.from(UserActivity.this);
        View dialogBox = layoutInflater.inflate(R.layout.calibration_dialog, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(UserActivity.this);
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
                                Intent myIntent = new Intent(UserActivity.this, GraphActivity.class);
                                startActivity(myIntent);
                            }
                        })
                .setPositiveButton("Auto-Calibration Mode",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent myIntent = new Intent(UserActivity.this, GraphActivity.class);
                                startActivity(myIntent);

                                myIntent = new Intent(UserActivity.this, CalibrationActivity.class);
                                startActivity(myIntent);
                            }
                        });

        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();
    }
}
