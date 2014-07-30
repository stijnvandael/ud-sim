package aggregator.modules.communication;

import java.util.ArrayList;
import java.util.Iterator;

import org.joda.time.DateTime;

import units.EnergyValue;
import units.PowerValue;
import utils.ArrayMath;
import communication.messages.Message;
import communication.network.Server;
import communication.network.Session;
import communication.network.SessionListener;

/**
 * Session manager which manages EV sessions and aggregates their underlying information
 * 
 * @author Stijn
 *
 */
public class CommunicationModule implements SessionListener {

	private final Server server;

	private final ArrayList<EVInformation> evsInfo;
	
	/**
	 * Create new EV session manager at a specified port number
	 * 
	 * @param 	port
	 * 			port number for socket
	 */
	public CommunicationModule(Server server) {
		this.server = server;
		this.server.register(this);
		this.evsInfo = new ArrayList<EVInformation>();
	}
	
	public void init() {
		server.initCommunication();
	}
	
	@Override
	public void createNewSession(Session session) {
		EVInformation evInfo = new EVInformation(session);
		evsInfo.add(evInfo);
	}
	
	public synchronized void sendMessage(Message message) {
		for(EVInformation info: evsInfo) {
			info.getSession().send(message);
		}
	}
	
	public PowerValue getTotalPower() {
		long totalPower = 0;
		Iterator<EVInformation> iterator = evsInfo.iterator();
		while(iterator.hasNext()) {
			EVInformation evSession = iterator.next();
			if(evSession.getSession().isConnectedToClient()) {
				totalPower = totalPower + evSession.getPower();	
			}else{
				iterator.remove();
			}
		}
		return new PowerValue(totalPower);
	}
	
	public PowerValue getTotalPOP() {
		long totalPOP = 0;
		Iterator<EVInformation> iterator = evsInfo.iterator();
		while(iterator.hasNext()) {
			EVInformation evSession = iterator.next();
			if(evSession.getSession().isConnectedToClient()) {
				totalPOP = totalPOP + evSession.getPop();
			}else{
				iterator.remove();
			}
		}
		return new PowerValue(totalPOP);
	}
	
	public PowerValue getTotalDownReg() {
		long totalDownReg = 0;
		Iterator<EVInformation> iterator = evsInfo.iterator();
		while(iterator.hasNext()) {
			EVInformation evSession = iterator.next();
			if(evSession.getSession().isConnectedToClient()) {
				totalDownReg = totalDownReg + evSession.getDownRegulation();
			}else{
				iterator.remove();
			}
		}
		return new PowerValue(totalDownReg);
	}

	public PowerValue getTotalUpReg() {
		long totalUpReg = 0;
		Iterator<EVInformation> iterator = evsInfo.iterator();
		while(iterator.hasNext()) {
			EVInformation evSession = iterator.next();
			if(evSession.getSession().isConnectedToClient()) {
				totalUpReg = totalUpReg + evSession.getUpRegulation();
			}else{
				iterator.remove();
			}
		}
		return new PowerValue(totalUpReg);
	}
	
	public ArrayList<FlexGraph> getFlexGraphsInBins(DateTime syncTimePoint) {
		ArrayList<FlexGraph> bins = new ArrayList<FlexGraph>();
		long syncTime = syncTimePoint.getMillis()/1000;
		Iterator<EVInformation> iterator = evsInfo.iterator();
		while(iterator.hasNext()) {
			EVInformation evSession = iterator.next();
			if(evSession.getSession().isConnectedToClient()) {
				if(evSession.getScheduleSyncTime() == syncTime) {
					FlexGraph flexGraph = evSession.getFlexGraph();
					boolean inbin = false;
					for(FlexGraph f: bins) {
						if(flexGraph.inSameBin(f)) {
							f.aggregate(flexGraph);
							inbin = true;
							break;
						}
					}
					if(!inbin) {
						bins.add(flexGraph);
					}
				}
			}else{
				iterator.remove();
			}
		}
		return bins;
	}
	
	public ArrayList<FlexGraph> getFlexGraphs(DateTime syncTimePoint) {
		ArrayList<FlexGraph> bins = new ArrayList<FlexGraph>();		
		long syncTime = syncTimePoint.getMillis()/1000;
		Iterator<EVInformation> iterator = evsInfo.iterator();
		while(iterator.hasNext()) {
			EVInformation evSession = iterator.next();
			if(evSession.getSession().isConnectedToClient()) {
				if(evSession.getScheduleSyncTime() == syncTime) {
					FlexGraph flexGraph = evSession.getFlexGraph();
					bins.add(flexGraph);	
				}
			}else{
				iterator.remove();
			}
		}
		return bins;
	}

	public FlexGraph getTotalFlexGraph(DateTime syncTimePoint) {
		FlexGraph totalFlexGraph = new FlexGraph();
		long syncTime = syncTimePoint.getMillis()/1000;
		Iterator<EVInformation> iterator = evsInfo.iterator();
		while(iterator.hasNext()) {
			EVInformation evSession = iterator.next();
			if(evSession.getSession().isConnectedToClient()) {
				if(evSession.getScheduleSyncTime() == syncTime) {
					FlexGraph flexGraph = evSession.getFlexGraph();
					totalFlexGraph.aggregate(flexGraph);
				}
			}else{
				iterator.remove();
			}
		}
		return totalFlexGraph;
	}

}