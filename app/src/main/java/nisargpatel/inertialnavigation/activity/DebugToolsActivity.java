package nisargpatel.inertialnavigation.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import nisargpatel.inertialnavigation.R;

public class DebugToolsActivity extends ListActivity {

    private static final String[] debugToolsList = {"Step Counter", "Heading Test", "Data Collector", "QR Code Scanner"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug_tools);

        setListAdapter(new ArrayAdapter<>(DebugToolsActivity.this, android.R.layout.simple_list_item_1, debugToolsList));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_debug_tools, menu);
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
            } case 3: { //qr code scanner
                Intent myIntent = new Intent(DebugToolsActivity.this, QRCodeActivity.class);
                startActivity(myIntent);
                break;
            }
        }

    }
}
