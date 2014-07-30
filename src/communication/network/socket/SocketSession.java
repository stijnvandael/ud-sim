package communication.network.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import clock.Time;

import communication.messages.DefectMessage;
import communication.messages.DefectRequestMessage;
import communication.messages.FlexMessage;
import communication.messages.Message;
import communication.messages.StatusMessage;
import communication.network.MessageListener;
import communication.network.Session;

/**
 * Low-level session which handles basic input and output
 * 
 * @author Stijn
 *
 */
public class SocketSession implements Session, Runnable {
	
	private MessageListener listener;
	
	private DateTime lastTimeAlive;
	private boolean alive;
	
    private BufferedReader input;
    private PrintWriter output;
    
    public SocketSession() {
    	this.lastTimeAlive = Time.getInstance().getDateTime();
    }
    
	@Override
	public void register(MessageListener listener) {
		this.listener = listener;
	}
	
	@Override
	public void send(Message message) {
		this.send(message.toJSON().toJSONString());
	}

	/**
	 * Method to connect the session to the socket of the SessionManager
	 * 
	 * @param 	s
	 * 			socket object from SessionManager
	 * @throws IOException
	 */
	protected void setSocket(Socket s) throws IOException {
		this.input = new BufferedReader(new InputStreamReader(s.getInputStream()));
		this.output = new PrintWriter(s.getOutputStream(), true);
		this.alive = true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		while(true) {
			try {
				String s = input.readLine();
				if(s!=null) {
					receive(s);
					lastTimeAlive = Time.getInstance().getDateTime();
				}else{
					// if no response for longer than 2 minutes, remove this session
					if(Time.getInstance().getDateTime().getMillis() - lastTimeAlive.getMillis() > 2*60*1000) {
						alive = false;
						break;
					}
				}
			} catch (Exception e) {
				Logger.getLogger("communication").log(Level.ERROR, "Message parse error: " + e.getMessage());
				e.printStackTrace();
				alive = false;
				break;
			}
		}
	}
	
	/**
	 * Receive string from this session
	 * 
	 * @param 	s
	 * 			string message
	 */
	private void receive(String s) {
		JSONParser parser = new JSONParser();
		JSONObject object;
		Message message = null;
		try {
			object = (JSONObject)parser.parse(s);
			if(object.get("type").equals("status")) {
				message = StatusMessage.fromJSON(object);
			}else
			if(object.get("type").equals("flex")) {
				message = FlexMessage.fromJSON(object);
			}else
			if(object.get("type").equals("defects")) {
				message = DefectMessage.fromJSON(object);
			}else
			if(object.get("type").equals("defectrequest")) {
				message = DefectRequestMessage.fromJSON(object);
			}else{
				throw new ParseException(0);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		listener.receive(message);
	}
	
	/**
	 * Send string to this session
	 * 
	 * @param 	s
	 * 			string message
	 */
	private void send(String s) {
		Logger.getLogger("communication").log(Level.INFO, "Aggregator -> EV : " + s);
		if(isConnectedToClient()) {
			output.println(s);
		}
	}

	/**
	 * Check if this session is still alive.
	 * 
	 * @return
	 */
	@Override
	public boolean isConnectedToClient() {
		return alive;
	}
	
}