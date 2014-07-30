package aggregator.gui.graphs;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.text.SimpleDateFormat;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisState;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.DateTickUnitType;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardXYSeriesLabelGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import utils.ArrayMath;

public class EnergyGraph extends JPanel {

	
	public EnergyGraph() {
		this.initGraph();
	}
	
	//graph parameters
	private JFreeChart jfreechart;
	private ChartPanel localChartPanel;
	private TimeSeries flexGraphTimeSeries;
	private TimeSeries flexGraphMaxTimeSeries;
	private TimeSeries flexMaxTimeSeries;
	private TimeSeries flexPathTimeSeries;
	private TimeSeries repairedFlexPathTimeSeries;

	public void initGraph() {
		jfreechart = ChartFactory.createTimeSeriesChart("", "", "Energy (Wh)", createDataset(), true, true, false);
		localChartPanel = new ChartPanel(jfreechart);
		localChartPanel.setMaximumSize(new Dimension(600, 280));
		localChartPanel.setPreferredSize(new Dimension(600, 280));
		localChartPanel.setAutoscrolls(false);
		localChartPanel.setMouseZoomable(false);
		createStyle();
		this.add(localChartPanel);
		this.setSize(750, 600);
	}

	private void createStyle() {
		XYPlot localXYPlot = (XYPlot) jfreechart.getPlot();
		XYItemRenderer localXYItemRenderer = localXYPlot.getRenderer();

		localXYItemRenderer.setSeriesStroke(0, new BasicStroke(2.0F));

		XYLineAndShapeRenderer localXYLineAndShapeRenderer = (XYLineAndShapeRenderer) localXYPlot.getRenderer();
		localXYLineAndShapeRenderer.setBaseShapesVisible(true);

		localXYLineAndShapeRenderer.setSeriesFillPaint(0, Color.white);
		localXYLineAndShapeRenderer.setUseFillPaint(true);
		localXYLineAndShapeRenderer.setLegendItemToolTipGenerator(new StandardXYSeriesLabelGenerator("Tooltip {0}"));

		// shape dots
		XYPlot plot = (XYPlot) localChartPanel.getChart().getPlot();
		XYLineAndShapeRenderer r = (XYLineAndShapeRenderer) plot.getRenderer();

		// determine series
		// repaired path
		r.setSeriesPaint(0, Color.BLACK);
		r.setSeriesShapesVisible(0, false);
		r.setSeriesStroke(0, new BasicStroke(1));
		// path
		r.setSeriesPaint(1, Color.GREEN);
		r.setSeriesShapesVisible(1, false);
		r.setSeriesStroke(1, new BasicStroke(1));
		// flexgraph
		r.setSeriesPaint(2, Color.RED);
		r.setSeriesShapesVisible(2, false);
		r.setSeriesStroke(2, new BasicStroke(3));
		// flexgraphmax
		r.setSeriesShapesVisible(3, false);
		r.setSeriesPaint(3, Color.RED);
		r.setSeriesStroke(3, new BasicStroke(3));
		// flexmax
		r.setSeriesShapesVisible(4, false);
		r.setSeriesPaint(4, Color.BLUE);
		r.setSeriesStroke(4, new BasicStroke(3));
        jfreechart.removeLegend();
	}

	public XYDataset createDataset() {
		flexGraphTimeSeries = new TimeSeries("flexGraph");
		flexGraphMaxTimeSeries = new TimeSeries("flexGraphMax");
		flexMaxTimeSeries = new TimeSeries("flexMax");
		flexPathTimeSeries = new TimeSeries("flexPath");
		repairedFlexPathTimeSeries = new TimeSeries("repairedFlexPath");
		
		TimeSeriesCollection timeseriescollection = new TimeSeriesCollection();
		timeseriescollection.addSeries(repairedFlexPathTimeSeries);
		timeseriescollection.addSeries(flexPathTimeSeries);
		timeseriescollection.addSeries(flexGraphTimeSeries);
		timeseriescollection.addSeries(flexGraphMaxTimeSeries);
		timeseriescollection.addSeries(flexMaxTimeSeries);
		
		return timeseriescollection;
	}

	private DateTime prevSyncTime = null;
	private long prevRange = 0;
	
	public void update(DateTime syncTime, long[] flexGraph, long[] flexGraphMax, long[] flexMax, long[] flexPath) {	
		flexGraphTimeSeries.clear();
		flexGraphMaxTimeSeries.clear();
		flexMaxTimeSeries.clear();
		flexPathTimeSeries.clear();
		repairedFlexPathTimeSeries.clear();
		//check if synchronization interval is not messed up
		if(flexGraph.length == 0) {
			return;
		}

		//calculate finalTime
		DateTime finalTime = syncTime.plus(intervalDuration.getMillis()*flexGraph.length);
		//ADJUST AXIS
		if(!syncTime.equals(prevSyncTime) || flexGraph[flexGraph.length-1]+1000 > prevRange+500) {
			XYPlot localXYPlot = (XYPlot) jfreechart.getPlot();
			//set value range
			ValueAxis range = localXYPlot.getRangeAxis();
			range.setLowerBound(-1); // set lower limit so that can't move in -ve
			range.setUpperBound(flexGraph[flexGraph.length-1]+1000);
			//Set custom date axis
			FlexGraphCustomDateAxis customDateAxis = new FlexGraphCustomDateAxis();
			customDateAxis.setRange(syncTime.toDate(), finalTime.toDate());
			localXYPlot.setDomainAxis(customDateAxis);
			localXYPlot.setRangeGridlinesVisible(true);
			localXYPlot.setDomainMinorGridlinesVisible(true);	
		}
		//set to detect changes for axis ranges
		this.prevSyncTime = new DateTime(syncTime);
		this.prevRange = flexGraph[flexGraph.length-1]+1000;
		
		//SHOW INFO
		//repaired flexpath
		//flexpath
		DateTime temptime = new DateTime(syncTime);
		for(int t=0; t<flexPath.length; t++) {
			flexPathTimeSeries.add(new Second(temptime.toDate()), flexPath[t]);
			temptime = temptime.plus(intervalDuration);
		}
		//flexgraph
		temptime = new DateTime(syncTime);
		for(int t=0; t<flexGraph.length; t++) {
			flexGraphTimeSeries.add(new Second(temptime.toDate()), flexGraph[t]);
			temptime = temptime.plus(intervalDuration);
		}
		//flexgraphmax
		temptime = new DateTime(syncTime);
		for(int t=0; t<flexGraphMax.length; t++) {
			flexGraphMaxTimeSeries.add(new Second(temptime.toDate()), flexGraphMax[t]);
			temptime = temptime.plus(intervalDuration);
		}
		//flexMax
		temptime = new DateTime(syncTime);
		for(int t=0; t<flexMax.length; t++) {
			temptime = temptime.plus(intervalDuration);
			flexMaxTimeSeries.add(new Second(temptime.toDate()), flexMax[t]);
		}
	}
	
	private final Duration intervalDuration = new Duration(900*1000);


}

class FlexGraphCustomDateAxis extends DateAxis {
    /**
     * Draws the axis line, tick marks and tick mark labels.
     * 
     * @param g2  the graphics device.
     * @param cursor  the cursor.
     * @param plotArea  the plot area.
     * @param dataArea  the data area.
     * @param edge  the edge that the axis is aligned with.
     * 
     * @return The width or height used to draw the axis.
     */
    protected AxisState drawTickMarksAndLabels(Graphics2D g2, 
                                               double cursor,
                                               Rectangle2D plotArea,
                                               Rectangle2D dataArea, 
                                               RectangleEdge edge) {
                                              
       AxisState state = super.drawTickMarksAndLabels(g2,cursor,plotArea,dataArea,edge);
       
         // replace the returned ticks by ticks with interval of 1 month

         // remember old TickUnit
       DateTickUnit tickUnit = new DateTickUnit(DateTickUnitType.HOUR, 1);
       setTickUnit(tickUnit);
       state.setTicks(refreshTicks(g2, state, dataArea, edge));
       
       this.setDateFormatOverride(new SimpleDateFormat("HH:mm"));
       //this.setMinorTickCount(60*1000);
       this.setMinorTickMarksVisible(true);
       this.setMinorTickCount(4);
       // restore old TickUnit
       //setTickUnit(tickUnit);
       
       return state;          
    }

}