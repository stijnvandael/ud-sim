package aggregator.gui;

import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.Timer;

import org.joda.time.DateTime;

import units.PowerValue;
import aggregator.agent.AggregatorAgent;
import aggregator.gui.bidpanel.BidPanel;
import aggregator.gui.graphs.EnergyGraph;
import aggregator.gui.graphs.PowerGraph;
import aggregator.modules.pjm.PJMListener;
import clock.Time;
 
/**
 * Aggregator Gui
 * 
 * @author stijnvandael
 *
 */
public class AggregatorGui  implements ActionListener, PJMListener {
	
    public static void main(String[] args) throws InterruptedException {
//		EnergyValue currentEnergy = new EnergyValue(5000);
//		EnergyValue maxEnergy = new EnergyValue(35000);
//		PowerValue maxChargePower = new PowerValue(-12000);
//		PowerValue maxDischargePower = new PowerValue(12000);
//		EnergyValue requiredEnergy = new EnergyValue(35000);
//		DateTime arrivalTime = Time.getInstance().getDateTime();
//		DateTime departureTime = arrivalTime.plus(8*60*60*1000);
//		EnergyValue deltaE = new EnergyValue(5000);
//		EVGui evgui = new EVGui( currentEnergy,  maxEnergy,  maxChargePower, maxDischargePower, requiredEnergy, arrivalTime, departureTime, deltaE);
////		for(int t = 0; t < 60; t++) {
//			Thread.sleep(1000);
////			evgui.stateChanged(Time.getInstance().getDateTime(), currentEnergy, new PowerValue(1000), new PowerValue(-12000), new PowerValue(12000));
////		}
    }
    
    private AggregatorAgent aggregatorAgent;
	
	private EnergyGraph flexGraph;
	private PowerGraph powerGraph;
	private BidPanel bidPanel;
    
    public AggregatorGui(AggregatorAgent aggregatorAgent) {
    	this.aggregatorAgent = aggregatorAgent;
    	this.aggregatorAgent.getModuleManager().getPjmModule().addListener(this);
    	show();
    }
 
    private void show() {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        try {
			javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
			    public void run() {
			        //Create and set up the window.
			        JFrame frame = new JFrame("Udel Aggregator");
			        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			 
			        //Set up the content pane.
			        addComponentsToPane(frame.getContentPane());
			 
			        //Display the window.
			        frame.pack();
			        frame.setSize(900, 800);
			        frame.setVisible(true);
			    }
			});
			//run Swing timer to execute updates in Swing event thread
			Timer timer = new Timer((int) (2000/Time.speedUpFactor), this);
			timer.setInitialDelay(1);
			timer.start(); 
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    }
    
	
    private void addComponentsToPane(Container pane) {
    	pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));

    	//digital clock
    	final JLabel timeLabel = new JLabel("test");
    	final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
    	timeLabel.setFont(new Font("Dialog", Font.PLAIN, 24));
    	java.util.Timer timer = new java.util.Timer();
    	pane.add(timeLabel);
    	timer.schedule(new java.util.TimerTask() {
			public void run() {
				timeLabel.setText(sdf.format(Time.getInstance().getDateTime().toDate()));
			};
		}, 1, Math.max((500)/Time.speedUpFactor, 1));;
		
    	//flexgraph component
    	flexGraph = new  EnergyGraph();
        pane.add(flexGraph);
    	//JPanel jpanel = new JPanel();
    	//jpanel.setLayout(new BoxLayout(jpanel, BoxLayout.Y_AXIS));
        //powergraph component
    	powerGraph = new  PowerGraph();
    	pane.add(powerGraph);
        //bid panel
        bidPanel = new BidPanel(aggregatorAgent.getModuleManager().getBidModule());
        this.aggregatorAgent.getModuleManager().getBidModule().setBidAPI(bidPanel);
        pane.add(bidPanel);
        //jpanel.setMaximumSize(new Dimension(200,200));
        //pane.add(jpanel);

    }

	@Override
	public void receiveRegulationSignal(int regSignal) {
		DateTime time = Time.getInstance().getDateTime();
		powerGraph.updateRegSignal(time, new PowerValue(regSignal));
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		//planning information
		flexGraph.update(this.aggregatorAgent.getScheduler().getOptimizationSyncTime(), this.aggregatorAgent.getScheduler().getFlexGraph(),  this.aggregatorAgent.getScheduler().getFlexGraphMax(),  this.aggregatorAgent.getScheduler().getFlexMax(), this.aggregatorAgent.getScheduler().getFlexPath());	
		//status information
		powerGraph.update(Time.getInstance().getDateTime(),  this.aggregatorAgent.getModuleManager().getEVSessionManager().getTotalPower(),  this.aggregatorAgent.getModuleManager().getEVSessionManager().getTotalPOP(),  this.aggregatorAgent.getModuleManager().getEVSessionManager().getTotalDownReg(),  this.aggregatorAgent.getModuleManager().getEVSessionManager().getTotalUpReg());
	}
 
}
