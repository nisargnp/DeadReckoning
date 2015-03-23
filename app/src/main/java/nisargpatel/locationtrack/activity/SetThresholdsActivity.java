package nisargpatel.locationtrack.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import nisargpatel.locationtrack.R;

public class SetThresholdsActivity extends ActionBarActivity {

    TextView textUpperThreshold;
    TextView textLowerThreshold;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_thresholds);

       textUpperThreshold = (TextView) findViewById(R.id.textUpper);
       textLowerThreshold = (TextView) findViewById(R.id.textLower);

        findViewById(R.id.buttonSetThresholds).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String upper = textUpperThreshold.getText().toString();
                String lower = textLowerThreshold.getText().toString();
                if (upper.length() != 0 && lower.length() != 0) {
                    StepCountActivity.setThresholds(Double.parseDouble(upper), Double.parseDouble(lower));
                    Toast.makeText(getApplicationContext(), R.string.threshold_set, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(SetThresholdsActivity.this, "Enter valid thresholds.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
