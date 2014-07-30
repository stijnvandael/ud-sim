package communication.network.simple;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import communication.messages.Message;
import communication.network.MessageListener;
import communication.network.Session;

public class SimpleSession implements Session {

	private SimpleClient client;
	
	private MessageListener listener;
	
	public SimpleSession(SimpleClient client) {
		this.client = client;
	}
	
	@Override
	public void register(MessageListener listener) {
		this.listener = listener;
	}

	public void receive(Message message) {
		if(listener != null) {
			listener.receive(message);	
		}else{
			Logger.getLogger("communication").log(Level.ERROR, "No Listener registered for session.");
		}
	}
	
	@Override
	public void send(Message message) {
		Logger.getLogger("communication").log(Level.INFO, "Aggregator -> EV: " + message.toJSON().toJSONString());
		if(client != null) {
			client.receive(message);	
		}else{
			Logger.getLogger("communication").log(Level.ERROR, "No client associated with session.");
		}
	}

	@Override
	public boolean isConnectedToClient() {
		return client!=null;
	}

	public void remove() {
		client = null;
		listener = null;
	}

}
