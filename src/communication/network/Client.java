package communication.network;

import communication.messages.Message;

public interface Client {

	public void register(MessageListener listener);
	
	public void send(Message message);
	
	public boolean isConnectedToServer();

	public void initCommunication();

	public void stop();
}
