package electricvehicle.gui;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.Timer;

import org.joda.time.DateTime;

import clock.Time;
import clock.Time.SimulationMode;
import electricvehicle.agent.EVAgent;
import electricvehicle.gui.graphs.EnergyGraph;
import electricvehicle.gui.graphs.PowerGraph;
 
/**
 * EV Gui
 * 
 * @author stijnvandael
 *
 */
public class EVGui implements ActionListener {
    
    private EVAgent evAgent;
	
	private EnergyGraph flexGraph;
	private PowerGraph powerGraph;
    
    public EVGui(EVAgent evAgent) {
    	this.evAgent = evAgent;
    	show();
    }
 
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private void show() {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        try {
			javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
			    public void run() {
			        //Create and set up the window.
			        JFrame frame = new JFrame("BMW Mini-E");
			        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			 
			        //Set up the content pane.
			        addComponentsToPane(frame.getContentPane());
			 
			        //Display the window.
			        frame.pack();
			        frame.setSize(800, 720);
			        frame.setVisible(true);
			    }
			});
			//run Swing timer to execute updates in Swing event thread
			if(Time.getInstance().simMode.equals(SimulationMode.REALTIME)) {
				Timer timer = new Timer(2000/(int)Time.speedUpFactor, this);
				timer.setInitialDelay(0);
				timer.start(); 
			}else
			if(Time.getInstance().simMode.equals(SimulationMode.SIMTIME)) {
				Timer timer = new Timer(1, this);
				timer.setInitialDelay(0);
				timer.start(); 
			}
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

    	//flexgraph component
    	flexGraph = new  EnergyGraph(evAgent.getRegulationController().getEv().getBatteryModule().getCurrentEnergy(), evAgent.getRegulationController().getEv().getBatteryModule().getCapacity(), evAgent.getRegulationController().getEv().getBatteryModule().getMinEnergy(), evAgent.getRegulationController().getEv().getBatteryModule().getMaxEnergy(), evAgent.getRegulationController().getEv().getBatteryModule().getPmin(), evAgent.getRegulationController().getEv().getRequiredEnergy(), evAgent.getRegulationController().getEv().getArrivalTime(), evAgent.getRegulationController().getEv().getDepartureTime(), evAgent.getRegulationController().getDeltaE());
        pane.add(flexGraph);
        
        //powergraph component
    	powerGraph = new  PowerGraph(evAgent.getRegulationController().getEv().getBatteryModule().getPmin(), evAgent.getRegulationController().getEv().getArrivalTime(), evAgent.getRegulationController().getEv().getDepartureTime());
        pane.add(powerGraph);
    }

    /**
     * UPDATE METHOD (called by Swing timer)
     * 
     */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		DateTime time = Time.getInstance().getDateTime();
		flexGraph.stateChanged(time, evAgent.getRegulationController().getEv().getBatteryModule().getCurrentEnergy(), evAgent.getRegulationController().getEv().getBatteryModule().getCurrentPower(), evAgent.getRegulationController().getDownRegulation(), evAgent.getRegulationController().getUpRegulation());
		powerGraph.stateChanged(time, evAgent.getRegulationController().getEv().getBatteryModule().getCurrentEnergy(), evAgent.getRegulationController().getEv().getBatteryModule().getCurrentPower(), evAgent.getRegulationController().getDownRegulation(), evAgent.getRegulationController().getUpRegulation(), evAgent.getRegulationController().getReqRegulation());
		flexGraph.planningChanged(evAgent.getScheduler().getFlexMin(), evAgent.getScheduler().getFlexMax(), evAgent.getScheduler().getFlexMin(), evAgent.getScheduler().generateSolutionPath(), evAgent.getScheduler().getSyncTime(), evAgent.getScheduler().getIntervalDuration(), evAgent.getScheduler().getSyncEnergy());
	}
 
}
