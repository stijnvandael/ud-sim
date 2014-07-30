package electricvehicle.modules.communication;

import java.util.ArrayList;

import communication.messages.Message;
import communication.network.Client;
import communication.network.MessageListener;

public class CommunicationModule implements MessageListener {

	private final Client client;	
	private final ArrayList<MessageListener> listeners;
	
	public CommunicationModule(Client client) {
		this.client = client;
		this.client.register(this);
		this.listeners = new ArrayList<MessageListener>();
	}
	
	public void initCommunication() {
		client.initCommunication();
	}
	
	public void send(Message message) {
		this.client.send(message);
	}
		
	public boolean isAlive() {
		return client.isConnectedToServer();
	}
	
	public void addListener(MessageListener listener) {
		this.listeners.add(listener);
	}

	@Override
	public void receive(Message message) {
		for(MessageListener l: listeners) {
			l.receive(message);
		}
	}

	public boolean isConnectedToServer() {
		return client.isConnectedToServer();
	}

	public void stop() {
		client.stop();
	}
	
}
