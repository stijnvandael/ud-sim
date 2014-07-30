package aggregator.gui.graphs;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.text.SimpleDateFormat;
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
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;
import org.joda.time.DateTime;

import units.PowerValue;
import clock.Time;

public class PowerGraph extends JPanel {
	
	public PowerGraph() {
		this.initGraph();
	}
	
	//graph parameters
	private JFreeChart jfreechart;
	private ChartPanel localChartPanel;
	//ev information
	private TimeSeries upregTimeSeries;
	private TimeSeries downregTimeSeries;
	private TimeSeries popTimeSeries;
	private TimeSeries powerTimeSeries;
	//pjm information
	private TimeSeries pjmTimeSeries;
	
	public void initGraph() {
		jfreechart = ChartFactory.createTimeSeriesChart("", "Time (hours)", "Power (kW)", createDataset(), true, true, false);
		localChartPanel = new ChartPanel(jfreechart);
		localChartPanel.setMaximumSize(new Dimension(600, 300));
		localChartPanel.setPreferredSize(new Dimension(600, 300));
		localChartPanel.setAutoscrolls(false);
		localChartPanel.setMouseZoomable(false);
		createStyle();
		this.add(localChartPanel);
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
		// pop
		r.setSeriesShapesVisible(0, false);
		r.setSeriesPaint(0, Color.BLUE);
		r.setSeriesStroke(0, new BasicStroke(3));
		// power
		//r.setSeriesShapesVisible(1, false);
		//r.setSeriesPaint(1, Color.BLACK);
		//r.setSeriesStroke(1, new BasicStroke(3));
		// pjm
		r.setSeriesShapesVisible(1, false);
		r.setSeriesPaint(1, Color.GREEN);
		r.setSeriesStroke(1, new BasicStroke(3));
		// up
		r.setSeriesPaint(2, Color.RED);
		r.setSeriesShapesVisible(2, false);
		r.setSeriesStroke(2, new BasicStroke(3));
		// down
		r.setSeriesPaint(3, Color.RED);
		r.setSeriesShapesVisible(3, false);
		r.setSeriesStroke(3, new BasicStroke(3));
		//r.setSeriesStroke(4, new BasicStroke(1.0f));
		//set value range
		ValueAxis range = localXYPlot.getRangeAxis();
		range.setRange(-20, 20);
		//Set custom date axis
		PowerGraphCustomDateAxis customDateAxis = new PowerGraphCustomDateAxis();
		customDateAxis.setRange(Time.getInstance().getDateTime().toDate(), Time.getInstance().getDateTime().plus(8*3600*1000).toDate());
		localXYPlot.setDomainAxis(customDateAxis);
		localXYPlot.setRangeGridlinesVisible(true);
		localXYPlot.setDomainMinorGridlinesVisible(true);
		//mark arrival/departure time
//        IntervalMarker intervalmarker1 = new IntervalMarker(arrivalTime.getMillis(), arrivalTime.getMillis());
//        intervalmarker1.setLabelOffsetType(LengthAdjustmentType.EXPAND);
//        intervalmarker1.setPaint(new Color(200, 200, 200));
//        localXYPlot.addDomainMarker(intervalmarker1, Layer.BACKGROUND);
//        IntervalMarker intervalmarker2 = new IntervalMarker(departureTime.getMillis(), departureTime.getMillis());
//        intervalmarker2.setLabelOffsetType(LengthAdjustmentType.EXPAND);
//        intervalmarker2.setPaint(new Color(200, 200, 200));
//        localXYPlot.addDomainMarker(intervalmarker2, Layer.BACKGROUND);
        this.jfreechart.removeLegend();
	}

	public XYDataset createDataset() {
		upregTimeSeries = new TimeSeries("upregulation");
		downregTimeSeries = new TimeSeries("downregulation");
		popTimeSeries = new TimeSeries("POP");
		//powerTimeSeries = new TimeSeries("Power");
		pjmTimeSeries = new TimeSeries("regulation signal");
		TimeSeriesCollection timeseriescollection = new TimeSeriesCollection();
		timeseriescollection.addSeries(popTimeSeries);
		//timeseriescollection.addSeries(powerTimeSeries);
		timeseriescollection.addSeries(pjmTimeSeries);
		timeseriescollection.addSeries(upregTimeSeries);
		timeseriescollection.addSeries(downregTimeSeries);
		return timeseriescollection;
	}

	private long maxValue = 0;
	
	public void update(DateTime time, PowerValue power, PowerValue pop, PowerValue downreg, PowerValue upreg) {
		//if time is after departure time, change axis
		XYPlot localXYPlot1 = (XYPlot) localChartPanel.getChart().getPlot();
		if(time.getMillis() > ((PowerGraphCustomDateAxis)localXYPlot1.getDomainAxis()).getRange().getUpperBound()) {	
			((PowerGraphCustomDateAxis)localXYPlot1.getDomainAxis()).setRange(new Date((long) (((PowerGraphCustomDateAxis)localXYPlot1.getDomainAxis()).getRange().getLowerBound() + 1*3600*1000)), new Date((long) (((PowerGraphCustomDateAxis)localXYPlot1.getDomainAxis()).getRange().getUpperBound() + 1*3600*1000)));
		}
		popTimeSeries.addOrUpdate(new Second(time.toDate()), pop.inWatt()/1000.0);
		//powerTimeSeries.add(new Second(time.toDate()), power.inWatt()/1000.0);
		downregTimeSeries.addOrUpdate(new Second(time.toDate()), downreg.inWatt()/1000.0);
		upregTimeSeries.addOrUpdate(new Second(time.toDate()), upreg.inWatt()/1000.0);
		//ADJUST AXIS
		if(Math.abs(power.inWatt()) > maxValue || Math.abs(pop.inWatt()) > maxValue || Math.abs(upreg.inWatt()) > maxValue ) {
			maxValue = Math.max( Math.max(Math.abs(power.inWatt()), Math.abs(pop.inWatt())), Math.abs(upreg.inWatt()));
			XYPlot localXYPlot = (XYPlot) jfreechart.getPlot();
			//set value range
			ValueAxis range = localXYPlot.getRangeAxis();
			range.setLowerBound((-maxValue-1000)/1000.0); // set lower limit so that can't move in -ve
			range.setUpperBound((maxValue+1000)/1000.0);
		}
	}

	public void updateRegSignal(DateTime time, PowerValue regSignal) {
		try {
			pjmTimeSeries.add(new Second(time.toDate()), regSignal.inWatt()/1000.0);
		}catch(org.jfree.data.general.SeriesException e) {
			
		}
	}

}

class PowerGraphCustomDateAxis extends DateAxis {
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