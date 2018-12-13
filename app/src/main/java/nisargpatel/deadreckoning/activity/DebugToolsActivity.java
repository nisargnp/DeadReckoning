package nisargpatel.deadreckoning.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import nisargpatel.deadreckoning.R;

public class DebugToolsActivity extends ListActivity {

    private static final String[] debugToolsList = {"Step Counter", "Heading Test", "Data Collector"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug_tools);

        setListAdapter(new ArrayAdapter<>(DebugToolsActivity.this, android.R.layout.simple_list_item_1, debugToolsList));
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        switch (position) {
            case 0: { //step counter
                Intent myIntent = new Intent(DebugToolsActivity.this, StepCountActivity.class);
                startActivity(myIntent);
                break;
            } case 1: { //heading test
                Intent myIntent = new Intent(DebugToolsActivity.this, HeadingActivity.class);
                startActivity(myIntent);
                break;
            } case 2: { //data collector
                Intent myIntent = new Intent(DebugToolsActivity.this, DataCollectActivity.class);
                startActivity(myIntent);
                break;
            }
        }

    }
}
