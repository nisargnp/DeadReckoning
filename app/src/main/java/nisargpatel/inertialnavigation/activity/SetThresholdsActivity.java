package nisargpatel.inertialnavigation.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import nisargpatel.inertialnavigation.R;

public class SetThresholdsActivity extends ActionBarActivity {

    TextView textUpperThreshold;
    TextView textLowerThreshold;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_thresholds);

       textUpperThreshold = (TextView) findViewById(R.id.textUpper);
       textLowerThreshold = (TextView) findViewById(R.id.textLower);

    }

    //set the thresholds when the button the pressed
    public void buttonSetThresholds(View view) {
        double upper = Double.parseDouble(textUpperThreshold.getText().toString());
        double lower = Double.parseDouble(textLowerThreshold.getText().toString());
        StepCountActivity.setThresholds(upper, lower);

        Toast.makeText(getApplicationContext(), R.string.threshold_set, Toast.LENGTH_SHORT).show();

        finish();
    }
}
