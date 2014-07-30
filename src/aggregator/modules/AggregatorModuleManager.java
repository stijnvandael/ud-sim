package aggregator.modules;

import aggregator.modules.bids.BidModule;
import aggregator.modules.communication.CommunicationModule;
import aggregator.modules.pjm.PJMModule;

import communication.network.Server;

public class AggregatorModuleManager {
	
	private final String id;
	
	private final CommunicationModule communicationModule;
	private final PJMModule pjmModule;
	private final BidModule bidModule;
	
	public AggregatorModuleManager(String id, Server server, String pjmProfile, long bidSize) {
		this.id = id;
		//create modules
		this.communicationModule = new CommunicationModule(server);
		this.pjmModule = new PJMModule(pjmProfile);
		this.bidModule = new BidModule(bidSize);
	}
	
	public void init() {
		this.communicationModule.init();
	}

	public CommunicationModule getEVSessionManager() {
		return communicationModule;
	}

	public PJMModule getPjmModule() {
		return pjmModule;
	}

	public BidModule getBidModule() {
		return bidModule;
	}
	
	public String getId() {
		return id;
	}
	
}
