package communication.network;

import communication.messages.Message;

public interface MessageListener {

	public void receive(Message message);
	
}
