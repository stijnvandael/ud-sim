package communication.network.simple;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import communication.messages.Message;
import communication.network.Client;
import communication.network.MessageListener;

public class SimpleClient implements Client {

	private final String id;
	
	private SimpleServer server;
	private SimpleSession session;
	
	private MessageListener listener;
	
	public SimpleClient(String id, SimpleServer server) {
		this.id = id;
		this.server = server;
	}
	
	@Override
	public void initCommunication() {
		this.session = this.server.createSession(this);
	}

	@Override
	public void register(MessageListener listener) {
		this.listener = listener;
	}
	
	public void receive(Message message) {
		listener.receive(message);
	}

	@Override
	public void send(Message message) {
		Logger.getLogger("communication").log(Level.INFO, "EV -> Aggregtor: " + message.toJSON().toJSONString());
		if(session != null) {
			session.receive(message);	
		}else{
			Logger.getLogger("communication").log(Level.ERROR, "No session opened at server.");
		}
	}

	@Override
	public boolean isConnectedToServer() {
		return session!=null;
	}
	
	public String getId() {
		return id;
	}

	@Override
	public void stop() {
		session.remove();
	}

}
