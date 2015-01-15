package nisargpatel.inertialnavigation;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import nisargpatel.inertialnavigation.graph.ScatterPlot;
import nisargpatel.inertialnavigation.heading.HeadingInference;
import nisargpatel.inertialnavigation.stepcounters.MovingAverageStepCounter;

public class GraphActivity extends ActionBarActivity implements SensorEventListener{

    private HeadingInference headingInference;
    private MovingAverageStepCounter movingStepCounter;

    private Sensor sensorStepDetector;
    private Sensor sensorAccelerometer;
    private Sensor sensorGyroscope;
    private Sensor sensorMagnetometer;
    private SensorManager sensorManager;

    private Button buttonStart;
    private Button buttonStop;

    private LinearLayout linearLayout;

    private ScatterPlot sPlot;

    private long recordedTime;

    private static final int STRIDE_LENGTH = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        double gyroInput[] = {-2900, -1450, 0, 1450, 2900};
        double radianInput[] = {-90, 0, 90, 180, 270};

        headingInference = new HeadingInference(gyroInput, radianInput);
        movingStepCounter = new MovingAverageStepCounter(1.0);

        sPlot = new ScatterPlot("Position");

        buttonStart = (Button) findViewById(R.id.buttonGraphStart);
        buttonStop = (Button) findViewById(R.id.buttonGraphStop);
        linearLayout = (LinearLayout) findViewById(R.id.linearLayoutGraph);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorStepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorGyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorMagnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR);

        //setting up graph with origin
        sPlot.addPoint(0, 0);
        linearLayout.addView(sPlot.getGraphView(getApplicationContext()));

        createFile("accelerometer");
        createFile("gyroscope");
        createFile("magnetometer");

        //buttons
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorManager.registerListener(GraphActivity.this, sensorStepDetector, SensorManager.SENSOR_DELAY_FASTEST);
                sensorManager.registerListener(GraphActivity.this, sensorAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
                sensorManager.registerListener(GraphActivity.this, sensorGyroscope, SensorManager.SENSOR_DELAY_FASTEST);
                recordedTime = System.currentTimeMillis();
                Toast.makeText(getApplicationContext(), "Step counter started.", Toast.LENGTH_SHORT).show();
            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorManager.unregisterListener(GraphActivity.this, sensorStepDetector);
                sensorManager.unregisterListener(GraphActivity.this, sensorAccelerometer);
                sensorManager.unregisterListener(GraphActivity.this, sensorGyroscope);
                Toast.makeText(getApplicationContext(), "Step counter stopped.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_graph, menu);
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

    private double currentGyroValue;
    private double averageGyroValue;
    private double totalGyroValue;

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {

            double xVelocity = (double) event.values[0];
            double yVelocity = (double) event.values[1];
            double zVelocity = (double) event.values[2];

            currentGyroValue = (System.currentTimeMillis() - recordedTime) * event.values[2];

            if (currentGyroValue > averageGyroValue * 100000 ) {
                totalGyroValue += currentGyroValue;
            } else {
                averageGyroValue = (averageGyroValue + currentGyroValue) / 2;
            }

            recordedTime = System.currentTimeMillis();

            writeToFile(fileGyroscope, xVelocity, yVelocity, zVelocity, totalGyroValue);

        }
        else if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
//            boolean stepFound = false;
//            if (event.values[0] == 1.0)
//                stepFound = true;
//
//            if (stepFound) {
//                headingInference.calcDegrees(totalGyroValue);
//                double pointX = headingInference.getXPoint(STRIDE_LENGTH);
//                double pointY = headingInference.getYPoint(STRIDE_LENGTH);
//
//                sPlot.addPoint(sPlot.getLastXPoint() + pointX, sPlot.getLastYPoint() + pointY);
//
////                linearLayout.invalidate();
//                linearLayout.removeAllViews();
//                linearLayout.addView(sPlot.getGraphView(getApplicationContext()));
//
//                //debug information
////                System.out.println("----------------------------");
////                System.out.println("gyroInput: " + totalGyroValue);
////                System.out.println("degree: " + headingInference.getDegree());
////                System.out.println("normal (x,y): " + "(" + pointX + "," + pointY + ")");
//            }
        }
        else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            Double xAcc = (double) event.values[0];
            Double yAcc = (double) event.values[1];
            Double zAcc = (double) event.values[2];

            boolean stepFound = false;

            if (movingStepCounter.stepFound(System.currentTimeMillis(), zAcc));
                stepFound = true;

            if (stepFound) {

                writeToFile(fileAccelerometer, xAcc, yAcc, zAcc, 1);

                headingInference.calcDegrees(totalGyroValue);
                double pointX = headingInference.getXPoint(STRIDE_LENGTH);
                double pointY = headingInference.getYPoint(STRIDE_LENGTH);

                sPlot.addPoint(sPlot.getLastXPoint() + pointX, sPlot.getLastYPoint() + pointY);

                linearLayout.removeAllViews();
                linearLayout.addView(sPlot.getGraphView(getApplicationContext()));
            } else {
                writeToFile(fileAccelerometer, xAcc, yAcc, zAcc, 0);
            }
        } else if (event.sensor.getType() == Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR) {
            Double xField = (double) event.values[0];
            Double yField = (double) event.values[1];
            Double zField = (double) event.values[2];

            writeToFile(fileMagnetometer, xField, yField, zField, 0);
        }
    }

    private FileWriter fileWriter;
    private BufferedWriter writer;

    private File fileAccelerometer;
    private File fileGyroscope;
    private File fileMagnetometer;

    private void writeToFile(File myFile, double d1, double d2, double d3, double extra) {

        try {
            //myFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), fileName);
            writer = new BufferedWriter(new FileWriter(myFile, true));

            writer.write(System.currentTimeMillis() + ";" +  d1 + ";" + d1 + ";" + d3 + ";" + extra);
            writer.write(System.getProperty("line.separator"));
            writer.close();
        } catch (IOException ignored) {}
    }

    private String folderPath;
    private String fileName;

    private void createFile(String type) {

        String folderName = "Inertial Navigation Data";

        File myFolder = new File(Environment.getExternalStorageDirectory(), folderName);
        if (!myFolder.exists())
            myFolder.mkdir();

        folderPath = myFolder.getPath();

        //determines what the data file's name will be
        fileName = getFileName(type);

        //lets the user know the name of the new file
        Toast.makeText(getApplicationContext(), fileName, Toast.LENGTH_SHORT).show();

        if (type.equals("accelerometer")) {
            try {
                //creating file
                fileAccelerometer = new File(myFolder.getPath(), fileName);
                fileAccelerometer.createNewFile();
                //writing the heading of the file
                writer = new BufferedWriter(new FileWriter(fileAccelerometer, true));
                writer.write("time;xAcc;yAcc;zAcc;stepFound");
                writer.close();
            } catch (IOException ignored) {}
        } else if (type.equals("gyroscope")) {
            try {
                //creating file
                fileGyroscope = new File(myFolder.getPath(), fileName);
                fileGyroscope.createNewFile();
                //writing the heading of the file
                writer = new BufferedWriter(new FileWriter(fileGyroscope, true));
                writer.write("time;xVelocity;yVelocity;zVelocity;orientation");
                writer.close();
            } catch (IOException ignored) {}
        } else if (type.equals("magnetometer")) {
            try {
                //creating file
                fileMagnetometer = new File(myFolder.getPath(), fileName);
                fileMagnetometer.createNewFile();
                //writing the heading of the file
                writer = new BufferedWriter(new FileWriter(fileMagnetometer, true));
                writer.write("time;xField;yField;zField;orientation");
                writer.close();
            } catch (IOException ignored) {}
        }

    }

    private String getFileName(String type) {

        Time today = new Time(Time.getCurrentTimezone());
        today.setToNow();

        //long moreTime = System.currentTimeMillis();

        String date = "(" + today.year + "-" + today.month + "-" + today.monthDay + ")";
        String currentTime = "(" + today.format("%H.%M.%S") + ")";

        return type + " " + date + " " + currentTime;

        //provides the fileName for new data files depending on what already exists in the directory
        //example: if accData2 already exists, then accData3 is created
//        File folder = new File(folderPath.getPath());
//        File[] listOfFiles = folder.listFiles();
//
//        boolean fileFound;
//        int fileCount = 0;
//
//        //goes through every file in the directory to check its name
//        do {
//            fileCount++;
//            fileFound = false;
//            for (int i = 1; i <= listOfFiles.length; i++) {
//                //array starts at 0
//                if (listOfFiles[i - 1].getName().equals("accData" + fileCount + ".txt")) {
//                    fileFound = true;
//                }
//            }
//        } while (fileFound);
//
//        return "accData" + fileCount + ".txt";
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
