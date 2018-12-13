package nisargpatel.deadreckoning.graph;

import android.content.Context;
import android.graphics.Color;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.ArrayList;

public class ScatterPlot {

    private String seriesName;
    private ArrayList<Double> xList;
    private ArrayList<Double> yList;

    public ScatterPlot (String seriesName) {
        this.seriesName = seriesName;
        xList = new ArrayList<>();
        yList = new ArrayList<>();
    }

    public GraphicalView getGraphView(Context context) {

        XYSeries mySeries;
        XYSeriesRenderer myRenderer;
        XYMultipleSeriesDataset myMultiSeries;
        XYMultipleSeriesRenderer myMultiRenderer;

        //adding the x-axis data from an ArrayList to a standard array
        double[] xSet = new double[xList.size()];
        for (int i = 0; i < xList.size(); i++)
            xSet[i] = xList.get(i);

        //adding the y-axis data from an ArrayList to a standard array
        double[] ySet = new double[yList.size()];
        for (int i = 0; i < yList.size(); i++)
            ySet[i] = yList.get(i);

        //creating a new sequence using the x-axis and y-axis data
        mySeries = new XYSeries(seriesName);
        for (int i = 0; i < xSet.length; i++)
            mySeries.add(xSet[i], ySet[i]);

        //defining chart visual properties
        myRenderer = new XYSeriesRenderer();
        myRenderer.setFillPoints(true);
        myRenderer.setPointStyle(PointStyle.CIRCLE);
//        myRenderer.setColor(Color.GREEN);
        myRenderer.setColor(Color.parseColor("#ff0099ff"));

        myMultiSeries = new XYMultipleSeriesDataset();
        myMultiSeries.addSeries(mySeries);

        myMultiRenderer = new XYMultipleSeriesRenderer();
        myMultiRenderer.addSeriesRenderer(myRenderer);

        //setting text graph element sizes
        myMultiRenderer.setPointSize(10); //size of scatter plot points
        myMultiRenderer.setShowLegend(false); //hide legend

        //set chart and label sizes
        myMultiRenderer.setChartTitle("Position");
        myMultiRenderer.setChartTitleTextSize(75);
        myMultiRenderer.setLabelsTextSize(40);

        //setting X labels and Y labels position
        int[] chartMargins = {100, 100, 25, 100}; //top, left, bottom, right
        myMultiRenderer.setMargins(chartMargins);
        myMultiRenderer.setYLabelsPadding(50);
        myMultiRenderer.setXLabelsPadding(10);

        //setting chart min/max
        double bound = getMaxBound();
        myMultiRenderer.setXAxisMin(-bound);
        myMultiRenderer.setXAxisMax(bound);
        myMultiRenderer.setYAxisMin(-bound);
        myMultiRenderer.setYAxisMax(bound);

        //returns the graphical view containing the graphz
        return ChartFactory.getScatterChartView(context, myMultiSeries, myMultiRenderer);
    }

    //add a point to the series
    public void addPoint(double x, double y) {
        xList.add(x);
        yList.add(y);
    }

    public float getLastXPoint() {
        double x = xList.get(xList.size() - 1);
        return (float)x;
    }

    public float getLastYPoint() {
        double y = yList.get(yList.size() - 1);
        return (float)y;
    }

    public void clearSet() {
        xList.clear();
        yList.clear();
    }

    private double getMaxBound() {
        double max = 0;
        for (double num : xList)
            if (max < Math.abs(num))
                max = num;
        for (double num : yList)
            if (max < Math.abs(num))
                max = num;
        return (Math.abs(max) / 100) * 100 + 100; //rounding up to the nearest hundred
    }
}
