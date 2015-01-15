package nisargpatel.inertialnavigation;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class SetThresholdsActivity extends ActionBarActivity {

    TextView textUpperThreshold;
    TextView textLowerThreshold;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_thresholds);

       getSupportActionBar().setDisplayHomeAsUpEnabled(false);

       textUpperThreshold = (TextView) findViewById(R.id.textUpper);
       textLowerThreshold = (TextView) findViewById(R.id.textLower);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_set_thresholds, menu);
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

        return super.onOptionsItemSelected(item);
    }

    //set the thresholds when the button the pressed
    public void buttonSetThresholds(View view) {
        double upper = Double.parseDouble(textUpperThreshold.getText().toString());
        double lower = Double.parseDouble(textLowerThreshold.getText().toString());
        StepCounterActivity.setThresholds(upper, lower);

        Toast.makeText(getApplicationContext(), R.string.threshold_set, Toast.LENGTH_SHORT).show();

        finish();
    }
}
