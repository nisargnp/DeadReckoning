package nisargpatel.inertialnavigation.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import nisargpatel.inertialnavigation.R;

public class SettingsActivity extends ActionBarActivity {

    private static final String PREFS_NAME = "Inertial Navigation Preferences";

    private SharedPreferences sharedPreferences;
    private TextView textStrideLength;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sharedPreferences = getSharedPreferences(PREFS_NAME, 0);

        String strideLength = String.valueOf(sharedPreferences.getFloat("stride_length", 2.5f));
        textStrideLength = (TextView) findViewById(R.id.textSettingsStrideLength);
        textStrideLength.setText(strideLength);

        findViewById(R.id.buttonSettingsCalibration).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(SettingsActivity.this, CalibrationActivity.class);
                startActivity(myIntent);
            }
        });

        findViewById(R.id.buttonSettingsSetThresholds).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(SettingsActivity.this, SetThresholdsActivity.class);
                startActivity(myIntent);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        String strideLength = String.valueOf(sharedPreferences.getFloat("stride_length", 2.5f));
        textStrideLength.setText(strideLength);

    }

}
