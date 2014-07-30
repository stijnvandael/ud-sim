package communication.network.simple;

import java.util.Hashtable;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import communication.network.Server;
import communication.network.SessionListener;

public class SimpleServer implements Server {

	private final Hashtable<String, SimpleClient> clients;
	
	private SessionListener listener;
	
	public SimpleServer() {
		this.clients = new Hashtable<String, SimpleClient>();
	}
	
	@Override
	public void initCommunication() {
		// TODO Auto-generated method stub
	}
	
	public SimpleSession createSession(SimpleClient simpleClient) {
		Logger.getLogger("communication").log(Level.INFO, "Incoming session...");
		SimpleSession simpleSession = new SimpleSession(simpleClient);
		listener.createNewSession(simpleSession);
		return simpleSession;
	}
	
	@Override
	public void register(SessionListener listener) {
		this.listener = listener;
	}
	
	public void add(SimpleClient client) {
		clients.put(client.getId(), client);
	}

}