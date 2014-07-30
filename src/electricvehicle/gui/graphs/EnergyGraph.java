package electricvehicle.gui.graphs;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.Layer;
import org.jfree.ui.LengthAdjustmentType;
import org.jfree.ui.RectangleEdge;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import units.EnergyValue;
import units.PowerValue;

public class EnergyGraph extends JPanel {

	//input parameters
	private EnergyValue currentEnergy;
	private EnergyValue capacity;
	private EnergyValue minEnergy;
	private EnergyValue maxEnergy;
	private PowerValue maxChargePower;
	private EnergyValue requiredEnergy;
	private DateTime arrivalTime;
	private DateTime departureTime;
	private EnergyValue deltaE;
	
	public EnergyGraph(EnergyValue currentEnergy, EnergyValue capacity, EnergyValue minEnergy, EnergyValue maxEnergy, PowerValue maxChargePower, EnergyValue requiredEnergy, DateTime arrivalTime, DateTime departureTime, EnergyValue deltaE) {
		this.currentEnergy = currentEnergy;
		this.capacity = capacity;
		this.minEnergy = minEnergy;
		this.maxEnergy = maxEnergy;
		this.maxChargePower = maxChargePower;
		this.arrivalTime = arrivalTime;
		this.departureTime = departureTime;
		this.requiredEnergy = requiredEnergy;
		this.deltaE = deltaE;
		this.initGraph();
	}
	
	//graph parameters
	private JFreeChart jfreechart;
	private ChartPanel localChartPanel;
	private TimeSeries upperLimitTimeSeries;
	private TimeSeries realUpperLimitTimeSeries;
	private TimeSeries maxUpperLimitTimeSeries;
	private TimeSeries lowerLimitTimeSeries;
	private TimeSeries realLowerLimitTimeSeries;
	private TimeSeries minLowerLimitTimeSeries;
	private TimeSeries pathTimeSeries;
	private TimeSeries flexGraphSeries;
	private TimeSeries flexGraphMaxSeries;
	private TimeSeries flexPathSeries;
	private TimeSeries repairedFlexPathSeries;

	public void initGraph() {
		jfreechart = ChartFactory.createTimeSeriesChart("", "Time (hours)", "Energy (kWh)", createDataset(), true, true, false);
		localChartPanel = new ChartPanel(jfreechart);
		localChartPanel.setPreferredSize(new Dimension(800, 400));
		localChartPanel.setAutoscrolls(false);
		localChartPanel.setMouseZoomable(false);
		createStyle();
		this.add(localChartPanel);
		this.setSize(800, 600);
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
		//repaired flexpath
		r.setSeriesPaint(0, Color.BLACK);
		r.setSeriesShapesVisible(0, false);
		r.setSeriesStroke(0, new BasicStroke(1));
		//r.setSeriesStroke(0, new BasicStroke(1.0f));
		r.setSeriesFillPaint(0, Color.BLACK);		
		//flexpath
		r.setSeriesPaint(1, Color.GREEN);
		r.setSeriesShapesVisible(1, false);
		r.setSeriesStroke(1, new BasicStroke(1));
		//r.setSeriesStroke(1, new BasicStroke(1.0f));
		r.setSeriesFillPaint(1, Color.GREEN);
		// path
		//r.setSeriesShape(2, new Rectangle2D.Double(-2.5, -2.5, 5.0, 5.0));
		r.setSeriesPaint(2, Color.BLUE);
		r.setSeriesShapesVisible(2, false);
		r.setSeriesStroke(2, new BasicStroke(3));
		//r.setSeriesStroke(2, new BasicStroke(1.0f));
		r.setSeriesFillPaint(2, Color.BLACK);
		// flex graph
		r.setSeriesShapesVisible(3, false);
		r.setSeriesPaint(3, Color.RED);
		r.setSeriesStroke(3, new BasicStroke(3));
		// flex graph max
		r.setSeriesShapesVisible(4, false);
		r.setSeriesPaint(4, Color.RED);
		r.setSeriesStroke(4, new BasicStroke(3));
		// lower limit
		r.setSeriesShapesVisible(5, false);
		r.setSeriesPaint(5, Color.BLACK);
		r.setSeriesStroke(5, new BasicStroke(3));
		// upper limit
		r.setSeriesShapesVisible(6, false);
		r.setSeriesPaint(6, Color.BLACK);
		r.setSeriesStroke(6, new BasicStroke(3));
		// real lower limit
		r.setSeriesShapesVisible(7, false);
		r.setSeriesShape(7, new Rectangle2D.Double(-2.5, -2.5, 5.0, 5.0));
		r.setSeriesPaint(7, Color.BLACK);
		r.setSeriesStroke(7, new BasicStroke(1.0f,BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[] {10.0f}, 0.0f));
		// real upper limit
		r.setSeriesShapesVisible(8, false);
		r.setSeriesPaint(8, Color.BLACK);
		r.setSeriesStroke(8, new BasicStroke(1.0f,BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[] {10.0f}, 0.0f));
		// min upper limit
		r.setSeriesShapesVisible(9, false);
		r.setSeriesPaint(9, Color.BLACK);
		r.setSeriesStroke(9, new BasicStroke(1.0f,BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[] {10.0f}, 0.0f));
		// max upper limit
		r.setSeriesShapesVisible(10, false);
		r.setSeriesPaint(10, Color.BLACK);
		r.setSeriesStroke(10, new BasicStroke(1.0f,BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[] {10.0f}, 0.0f));
		//set value range
		ValueAxis range = localXYPlot.getRangeAxis();
		range.setLowerBound(-1); // set lower limit so that can't move in -ve
		range.setUpperBound(capacity.inWH()/1000.0+5);
		//Set custom date axis
		FlexGraphCustomDateAxis customDateAxis = new FlexGraphCustomDateAxis();
		if(departureTime.getMillis() > arrivalTime.getMillis()) {
			customDateAxis.setRange(arrivalTime.toDate(), departureTime.toDate());
		}else{
			customDateAxis.setRange(arrivalTime.toDate(), arrivalTime.plus(3600*1000).toDate());
		}
		localXYPlot.setDomainAxis(customDateAxis);
		localXYPlot.setRangeGridlinesVisible(true);
		localXYPlot.setDomainMinorGridlinesVisible(true);
		//mark arrival/departure time
        IntervalMarker intervalmarker1 = new IntervalMarker(arrivalTime.getMillis(), arrivalTime.getMillis());
        intervalmarker1.setLabelOffsetType(LengthAdjustmentType.EXPAND);
        intervalmarker1.setPaint(new Color(200, 200, 200));
        localXYPlot.addDomainMarker(intervalmarker1, Layer.BACKGROUND);
        IntervalMarker intervalmarker2 = new IntervalMarker(departureTime.getMillis(), departureTime.getMillis());
        intervalmarker2.setLabelOffsetType(LengthAdjustmentType.EXPAND);
        intervalmarker2.setPaint(new Color(200, 200, 200));
        localXYPlot.addDomainMarker(intervalmarker2, Layer.BACKGROUND);
        jfreechart.removeLegend();
	}

	public XYDataset createDataset() {
		upperLimitTimeSeries = new TimeSeries("Upper Limit");
		lowerLimitTimeSeries = new TimeSeries("Lower limit");
		realUpperLimitTimeSeries = new TimeSeries("Real Upper Limit");
		realLowerLimitTimeSeries = new TimeSeries("Real lower limit");
		minLowerLimitTimeSeries = new TimeSeries("Min lower limit");
		maxUpperLimitTimeSeries = new TimeSeries("Max Upper Limit");
		if(departureTime.getMillis() > arrivalTime.getMillis()) {
			//create upper limit
			upperLimitTimeSeries.addOrUpdate(new Second(arrivalTime.toDate()), capacity.inWH()/1000.0);
			upperLimitTimeSeries.addOrUpdate(new Second(departureTime.toDate()), capacity.inWH()/1000.0);
			realUpperLimitTimeSeries.addOrUpdate(new Second(arrivalTime.toDate()), maxEnergy.minus(deltaE).inWH()/1000.0);
			realUpperLimitTimeSeries.addOrUpdate(new Second(departureTime.toDate()), maxEnergy.minus(deltaE).inWH()/1000.0);
			maxUpperLimitTimeSeries.addOrUpdate(new Second(arrivalTime.toDate()), maxEnergy.inWH()/1000.0);
			maxUpperLimitTimeSeries.addOrUpdate(new Second(departureTime.toDate()), maxEnergy.inWH()/1000.0);
			
			//create lower limit
			Duration d = requiredEnergy.divide(maxChargePower);
			DateTime minFlexCrossPoint = departureTime.minus(requiredEnergy.divide(new PowerValue(-maxChargePower.inWatt())));
			if(arrivalTime.toDate().before(minFlexCrossPoint.toDate())) {
				lowerLimitTimeSeries.addOrUpdate(new Second(arrivalTime.toDate()), 0);
			}
			lowerLimitTimeSeries.addOrUpdate(new Second(minFlexCrossPoint.toDate()), 0);
			lowerLimitTimeSeries.addOrUpdate(new Second(departureTime.toDate()), requiredEnergy.inWH()/1000.0);
			if(arrivalTime.toDate().before(minFlexCrossPoint.toDate())) {
				realLowerLimitTimeSeries.addOrUpdate(new Second(arrivalTime.toDate()), minEnergy.plus(deltaE).inWH()/1000);
			}
			DateTime realMinFlexCrossPoint = departureTime.minus(requiredEnergy.plus(deltaE).minus(minEnergy.plus(deltaE)).divide(new PowerValue(-maxChargePower.inWatt())));	
			realLowerLimitTimeSeries.addOrUpdate(new Second(realMinFlexCrossPoint.toDate()), minEnergy.plus(deltaE).inWH()/1000);
			realLowerLimitTimeSeries.addOrUpdate(new Second(departureTime.toDate()), requiredEnergy.plus(deltaE).inWH()/1000.0);
			if(arrivalTime.toDate().before(minFlexCrossPoint.toDate())) {
				minLowerLimitTimeSeries.addOrUpdate(new Second(arrivalTime.toDate()), minEnergy.inWH()/1000);
			}
			DateTime lowerMinFlexCrossPoint = departureTime.minus(requiredEnergy.minus(minEnergy).divide(new PowerValue(-maxChargePower.inWatt())));	
			minLowerLimitTimeSeries.add(new Second(lowerMinFlexCrossPoint.toDate()), minEnergy.inWH()/1000);
		}
		
		//charging path
		pathTimeSeries = new TimeSeries("charging path");
		//pathTimeSeries.add(new Second(arrivalTime.toDate()), this.currentEnergy.inWH()/1000.0);
		
		//flexgraph planing
		flexGraphSeries = new TimeSeries("flex graph");	
		flexGraphMaxSeries = new TimeSeries("flex graph max");
		flexPathSeries = new TimeSeries("flex path");
		repairedFlexPathSeries = new TimeSeries("repaired flex path");
		
		TimeSeriesCollection timeseriescollection = new TimeSeriesCollection();
		timeseriescollection.addSeries(repairedFlexPathSeries);
		timeseriescollection.addSeries(flexPathSeries);
		timeseriescollection.addSeries(pathTimeSeries);
		timeseriescollection.addSeries(flexGraphSeries);
		timeseriescollection.addSeries(flexGraphMaxSeries);
		timeseriescollection.addSeries(lowerLimitTimeSeries);
		timeseriescollection.addSeries(upperLimitTimeSeries);
		timeseriescollection.addSeries(realLowerLimitTimeSeries);
		timeseriescollection.addSeries(realUpperLimitTimeSeries);
		timeseriescollection.addSeries(maxUpperLimitTimeSeries);
		timeseriescollection.addSeries(minLowerLimitTimeSeries);
		
		return timeseriescollection;
	}

	//show current state parameters
	public void stateChanged(DateTime time, EnergyValue energy, PowerValue power, PowerValue downreg, PowerValue upreg) {
		//if time is after departure time, shift axis
		XYPlot localXYPlot = (XYPlot) localChartPanel.getChart().getPlot();
		if(time.getMillis() > ((FlexGraphCustomDateAxis)localXYPlot.getDomainAxis()).getRange().getUpperBound()) {	
			((FlexGraphCustomDateAxis)localXYPlot.getDomainAxis()).setRange(new Date((long) (((FlexGraphCustomDateAxis)localXYPlot.getDomainAxis()).getRange().getLowerBound() + 1*3600*1000)), new Date((long) (((FlexGraphCustomDateAxis)localXYPlot.getDomainAxis()).getRange().getUpperBound() + 1*3600*1000)));
		}
		//add state observation
		pathTimeSeries.addOrUpdate(new Second(time.toDate()), energy.inWH()/1000.0);
	}

	//show planning parameters
	public void planningChanged(ArrayList<EnergyValue> flexGraph, ArrayList<EnergyValue> flexGraphMax, ArrayList<EnergyValue> flexPath, ArrayList<EnergyValue> binFlexMin, long syncTime, Duration intervalDuration, EnergyValue syncEnergy) {
		//remove previous flexgraph and flexpath
		flexGraphSeries.clear();
		flexGraphMaxSeries.clear();
		flexPathSeries.clear();
		repairedFlexPathSeries.clear();
		//set current flexgraph
		DateTime flexTime = new DateTime(syncTime);
		for(EnergyValue e: flexGraph) {
			flexGraphSeries.addOrUpdate(new Second(flexTime.toDate()), syncEnergy.plus(e).inWH()/1000.0);
			flexTime = flexTime.plus(intervalDuration);
		}
		//set current flexgraphmax
		flexTime = new DateTime(syncTime);
		for(EnergyValue e: flexGraphMax) {
			flexGraphMaxSeries.addOrUpdate(new Second(flexTime.toDate()), syncEnergy.plus(e).inWH()/1000.0);
			flexTime = flexTime.plus(intervalDuration);
		}
		//set current flexpath
		flexTime = new DateTime(syncTime);
		for(EnergyValue e: flexPath) {
			flexPathSeries.addOrUpdate(new Second(flexTime.toDate()), syncEnergy.plus(e).inWH()/1000.0);
			flexTime = flexTime.plus(intervalDuration);
		}
		//set current repaired flexpath
		flexTime = new DateTime(syncTime);
		for(EnergyValue e: binFlexMin) {
			repairedFlexPathSeries.addOrUpdate(new Second(flexTime.toDate()), syncEnergy.plus(e).inWH()/1000.0);
			flexTime = flexTime.plus(intervalDuration);
		}
	}

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