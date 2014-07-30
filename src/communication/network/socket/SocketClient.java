package communication.network.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import clock.Time;
import clock.SimRunnable;
import communication.messages.DefectMessage;
import communication.messages.Message;
import communication.messages.RegulationMessage;
import communication.messages.ScheduleMessage;
import communication.network.Client;
import communication.network.MessageListener;

public class SocketClient extends SimRunnable implements Client {
	
	private final String id;
	
	private MessageListener listener;
	
    private DateTime lastTimeAlive;
    private boolean alive;
    
    private final String ipAddress;
	private final int port;
    
    private BufferedReader input;
    private PrintWriter output;
    
    private Socket s;
    
	public SocketClient(String id, String ipAddress, int port) {
		this.id = id;
		this.ipAddress = ipAddress;
		this.port = port;
		this.alive = true;
		this.lastTimeAlive = Time.getInstance().getDateTime();
	}
	
	@Override
	public void initCommunication() {
		Time.getInstance().scheduleThread(id, this);
	}
	
	public void register(MessageListener listener) {
		this.listener = listener;
	}
	
	public void send(Message message) {
		if(output!= null) {
			Logger.getLogger("communication").log(Level.INFO, "EV -> aggregator : " + message.toJSON().toJSONString());
			output.println(message.toJSON().toJSONString());
		}else{
			Logger.getLogger("communication").log(Level.ERROR, "EV -> aggregator : No socket connection with aggregator.");
		}
	}
	
	@Override
	protected void step() {
		alive = false;
		lastTimeAlive = Time.getInstance().getDateTime();
		//continuously try to connect
		try {
			s = new Socket(ipAddress, port);
			input = new BufferedReader(new InputStreamReader(s.getInputStream()));
			output = new PrintWriter(s.getOutputStream(), true);
		
			// continuously check for messages
			while(s != null) {
				String answer;
				try{
					answer = input.readLine();
				} catch (SocketException e) {
					return;
				}
				if(answer!=null) {
					JSONParser parser = new JSONParser();
					JSONObject object = (JSONObject)parser.parse(answer);
					if(object.get("type").equals("reg")) {
						RegulationMessage regMessage = RegulationMessage.fromJSON(object);
						listener.receive(regMessage);
					}else
					if(object.get("type").equals("schedule")) {
						ScheduleMessage schedMessage = ScheduleMessage.fromJSON(object);
						listener.receive(schedMessage);
					}else
					if(object.get("type").equals("defects")) {
						DefectMessage defectMessage = DefectMessage.fromJSON(object);
						listener.receive(defectMessage);
					}
					alive = true;
					lastTimeAlive = Time.getInstance().getDateTime();
				}else{
					//if no response for longer than 2 minutes, remove this connection
					if(Time.getInstance().getDateTime().getMillis() - lastTimeAlive.getMillis() > 2*60*1000) {
						alive = false;
						break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Logger.getLogger("communication").log(Level.INFO, "Cannot connect to aggregator. Retrying...");
			//connection failed -- wait for a while until reconnect
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	@Override
	public boolean isConnectedToServer() {
		return alive;
	}

	@Override
	public void stop() {
		if(s != null) {
			try {
				s.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			s = null;	
		}
	}
	
}
