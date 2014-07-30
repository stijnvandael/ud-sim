package communication.network.socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import clock.SimRunnable;
import clock.Time;
import communication.network.Server;
import communication.network.SessionListener;


/**
 * 
 * Low-level session manager for creating sessions, and receiving/sending messages from/to sessions.
 * 
 * @author Stijn
 *
 * @param <T>
 */
public class SocketServer extends SimRunnable implements Server {
	
	private final String id;
	
	private final int port;
	
    private ServerSocket socket;
	private SessionListener listener;
    
	/** Initialize and start session manager
	 * @param 	port 
	 * 			server port number
	 */
	public SocketServer(String id, int port) {
		this.id = id;
		this.port = port;
	}
	
	@Override
	public void initCommunication() {
		try {
			this.socket = new ServerSocket(port);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Time.getInstance().scheduleThread(id, this);
	}
	
	@Override
	public void register(SessionListener listener) {
		this.listener = listener;
	}

	@Override
	protected void step() {
		try {
			Socket s = socket.accept();
			Logger.getLogger("communication").log(Level.INFO, "Incoming session...");
			SocketSession newSession = new SocketSession();
			newSession.setSocket(s);
			listener.createNewSession(newSession);
			new Thread(newSession).start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void stop() {
		try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		socket = null;
	}
	
//	/**
//	 * Add new session to the session manager.
//	 * 
//	 * @param 	s
//	 * 			session object
//	 */
//	private synchronized void addSession(T s) {
//		this.sessions.add(s);
//	}
//	
//	/**
//	 * Send string to all underlying sessions.
//	 * 
//	 * @param 	s
//	 * 			message string
//	 */
//	public synchronized void send(String s) {
//		for(ServerSession session: sessions) {
//			session.send(s);
//		}
//	}
//	
//	/**
//	 * Create new session object (of Session subclass)
//	 * 
//	 * @return new session object
//	 */
//	public abstract T createIncomingSession();
//	
//	/* (non-Javadoc)
//	 * @see java.lang.Iterable#iterator()
//	 */
//	@Override
//	public synchronized Iterator<T> iterator() {
//		return sessions.iterator();
//	}
//
//
//	public ArrayList<T> getSessions() {
//		return sessions;
//	}
//	
	

}
