package communication.network;


public interface Server {

	public void register(SessionListener listener);

	void initCommunication();
	
}
