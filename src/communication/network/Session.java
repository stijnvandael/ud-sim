package communication.network;

import communication.messages.Message;

public interface Session {
	
	public void register(MessageListener listener);
	
	public void send(Message message);

	boolean isConnectedToClient();
	
}
