package nisargpatel.inertialnavigation.graph;

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

    private XYSeries mySeries;
    private XYSeriesRenderer myRenderer;

    private XYMultipleSeriesDataset myMultiSeries;
    private XYMultipleSeriesRenderer myMultiRenderer;

    public ScatterPlot (String seriesName) {
        this.seriesName = seriesName;
        xList = new ArrayList<Double>();
        yList = new ArrayList<Double>();
    }

    public GraphicalView getGraphView(Context context) {

        double[] xSet = new double[xList.size()];
        for (int i = 0; i < xList.size(); i++)
            xSet[i] = xList.get(i);

        double[] ySet = new double[yList.size()];
        for (int i = 0; i < yList.size(); i++)
            ySet[i] = yList.get(i);

        mySeries = new XYSeries(seriesName);
        for (int i = 0; i < xSet.length; i++)
            mySeries.add(xSet[i], ySet[i]);

        myRenderer = new XYSeriesRenderer();
        myRenderer.setFillPoints(true);
        myRenderer.setPointStyle(PointStyle.CIRCLE);
        myRenderer.setColor(Color.GREEN);

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
        myMultiRenderer.setXAxisMin(-100);
        myMultiRenderer.setXAxisMax(100);
        myMultiRenderer.setYAxisMin(-100);
        myMultiRenderer.setYAxisMax(100);

        return ChartFactory.getScatterChartView(context, myMultiSeries, myMultiRenderer);
    }

    public void addPoint(double x, double y) {
        xList.add(x);
        yList.add(y);
    }

    public double getLastXPoint() {
        return xList.get(xList.size() - 1);
    }

    public double getLastYPoint() {
        return yList.get(yList.size() - 1);
    }

}
