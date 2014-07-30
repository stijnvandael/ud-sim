package electricvehicle.gui.graphs;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JFrame;
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

import units.EnergyValue;
import units.PowerValue;
import clock.Time;

public class PowerGraph extends JPanel {
	
	public static void main(String[] args) throws InterruptedException {
		EnergyValue currentEnergy = new EnergyValue(5000);
		EnergyValue maxEnergy = new EnergyValue(35000);
		PowerValue maxPower = new PowerValue(12000);
		EnergyValue requiredEnergy = new EnergyValue(35000);
		DateTime arrivalTime = Time.getInstance().getDateTime();
		DateTime departureTime = arrivalTime.plus(8*60*60*1000);
		EnergyValue deltaE = new EnergyValue(5000);
		
        //Create and set up the window.
        JFrame frame = new JFrame("Aggregator Demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
        //Set up the content pane.
        PowerGraph graph = new PowerGraph(maxPower, arrivalTime, departureTime);
        frame.add(graph);
 
        //Display the window.
        frame.pack();
        frame.setVisible(true);
        
		for(int t = 0; t < 60; t++) {
			Thread.sleep(1000);
			graph.stateChanged(Time.getInstance().getDateTime(), currentEnergy, new PowerValue(1000), new PowerValue(-12000), new PowerValue(12000), new PowerValue(12000));
		}
	}
	
	//input parameters
	private PowerValue maxPower;
	private DateTime arrivalTime;
	private DateTime departureTime;
	
	public PowerGraph(PowerValue maxPower, DateTime arrivalTime, DateTime departureTime) {
		this.maxPower = maxPower;
		this.arrivalTime = arrivalTime;
		this.departureTime = departureTime;
		this.initGraph();
	}
	
	//graph parameters
	private JFreeChart jfreechart;
	private ChartPanel localChartPanel;
	private TimeSeries upregTimeSeries;
	private TimeSeries downregTimeSeries;
	private TimeSeries powerTimeSeries;
	private TimeSeries reqRegTimeSeries;

	public void initGraph() {
		jfreechart = ChartFactory.createTimeSeriesChart("", "Time (hours)", "Power (kW)", createDataset(), true, true, false);
		localChartPanel = new ChartPanel(jfreechart);
		localChartPanel.setPreferredSize(new Dimension(800, 300));
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
		// power
		r.setSeriesShapesVisible(0, false);
		r.setSeriesPaint(0, Color.BLUE);
		r.setSeriesStroke(0, new BasicStroke(2));
		// downregulation
		r.setSeriesShapesVisible(1, false);
		r.setSeriesPaint(1, Color.RED);
		r.setSeriesStroke(1, new BasicStroke(3));
		// upregulation
		r.setSeriesPaint(2, Color.RED);
		r.setSeriesShapesVisible(2, false);
		r.setSeriesStroke(2, new BasicStroke(3));
		// requested regulation
		r.setSeriesPaint(3, Color.GREEN);
		r.setSeriesShapesVisible(3, false);
		r.setSeriesStroke(3, new BasicStroke(3));
		//set value range
		ValueAxis range = localXYPlot.getRangeAxis();
		range.setRange(maxPower.inKWatt()-1, -maxPower.inKWatt()+1);
		//Set custom date axis
		PowerGraphCustomDateAxis customDateAxis = new PowerGraphCustomDateAxis();
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
        this.jfreechart.removeLegend();
	}

	public XYDataset createDataset() {
		upregTimeSeries = new TimeSeries("upregulation");
		downregTimeSeries = new TimeSeries("downregulation");
		powerTimeSeries = new TimeSeries("charging power");
		reqRegTimeSeries = new TimeSeries("requested regulation");
		//powerTimeSeries.add(new Second(this.arrivalTime.toDate()), 0);
		//downregTimeSeries.add(new Second(this.arrivalTime.toDate()), 0);
		//upregTimeSeries.add(new Second(this.arrivalTime.toDate()), 0);
		//reqRegTimeSeries.add(new Second(this.arrivalTime.toDate()), 0);
		TimeSeriesCollection timeseriescollection = new TimeSeriesCollection();
		timeseriescollection.addSeries(powerTimeSeries);
		timeseriescollection.addSeries(upregTimeSeries);
		timeseriescollection.addSeries(downregTimeSeries);
		timeseriescollection.addSeries(reqRegTimeSeries);
		return timeseriescollection;
	}

	public void stateChanged(DateTime time, EnergyValue energy, PowerValue power, PowerValue downreg, PowerValue upreg, PowerValue reqreg) {
		//if time is after departure time, change axis
		XYPlot localXYPlot = (XYPlot) localChartPanel.getChart().getPlot();
		if(time.getMillis() > ((PowerGraphCustomDateAxis)localXYPlot.getDomainAxis()).getRange().getUpperBound()) {	
			((PowerGraphCustomDateAxis)localXYPlot.getDomainAxis()).setRange(new Date((long) (((PowerGraphCustomDateAxis)localXYPlot.getDomainAxis()).getRange().getLowerBound() + 1*3600*1000)), new Date((long) (((PowerGraphCustomDateAxis)localXYPlot.getDomainAxis()).getRange().getUpperBound() + 1*3600*1000)));
		}
		// TODO show power
		powerTimeSeries.addOrUpdate(new Second(time.toDate()), power.inWatt()/1000.0);
		downregTimeSeries.addOrUpdate(new Second(time.toDate()), downreg.inWatt()/1000.0);
		upregTimeSeries.addOrUpdate(new Second(time.toDate()), upreg.inWatt()/1000.0);
		reqRegTimeSeries.addOrUpdate(new Second(time.toDate()), reqreg.inWatt()/1000.0);
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