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
        myMultiRenderer.setPointSize(10);
        myMultiRenderer.setLabelsTextSize(40);
        myMultiRenderer.setLegendTextSize(40);
        //myMultiRenderer.setAxisTitleTextSize(0);
        //myMultiRenderer.setChartTitleTextSize(0);

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
}
