package aggregator.gui.bidpanel;

import java.awt.Color;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.TimerTask;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.joda.time.DateTime;

import aggregator.modules.bids.BidAPI;
import aggregator.modules.bids.BidModule;
import clock.Time;

public class BidPanel extends JPanel implements BidAPI, PropertyChangeListener {
	
	//connected bidmodule
	private BidModule bidModule;
	
	//text fields
	private ArrayList<JFormattedTextField> fields;
	
	public BidPanel(BidModule bidModule) {
		this.bidModule = bidModule;
		this.setMaximumSize(new Dimension(750,150));
		this.setPreferredSize(new Dimension(750,150));
		fields = new ArrayList<JFormattedTextField>();
		init();
	}
	
	public void init() {
		//create fields
		for(int n=0; n < 24; n++) {
			JLabel label = new JLabel("hour " + (n+1) + ":");
			if(n < 10) {
				label.setText("hour " + (n+1) + " :");
			}else{
				label.setText("hour " + (n+1) + ":");
			}
			this.add(label);
			JFormattedTextField field = new JFormattedTextField();
			field.setName(Integer.toString(n));
			field.setEditable(true);
			field.setValue(new Double(0));
			field.setColumns(4);
			this.add("hour ".concat(n + ""), field);
			fields.add(field);
			field.addPropertyChangeListener(this);
		}
		setColorHours(Time.getInstance().getDateTime());
		//create time to indicate first and second hour
		TimerTask t = new TimerTask() {
			public void run() {
				setColorHours(Time.getInstance().getDateTime());
			};
		};
		Time.getInstance().scheduleHourlyTask("aggregator_bids", t);
	}
	
	public void setColorHours(DateTime time) {
		//reset color and previous bids
		for(JFormattedTextField f: fields) {
			f.setBackground(null);
		}
		//set new bids
		int hour = time.getHourOfDay();
		for(int i = 0; i < 24; i++) {
			if(hour+i > 23) {
				hour = hour - 24;
			}
			if(i==0) {
				fields.get(hour+i).setBackground(Color.GREEN);
			}else
			if(i==1) {
				fields.get(hour+i).setBackground(new Color(100, 149, 237));
			}
		}
	}

	@Override
	public void receiveBid(DateTime syncTime, long bid) {
		int hour = syncTime.getHourOfDay();
		fields.get(hour).setValue(bid/1000);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
//		JFormattedTextField l = (JFormattedTextField) evt.getSource();
//		if(l.getValue() instanceof Double) {
//			long bid = (long)((double)((Double)l.getValue()));	
//			int hourChanged = Integer.parseInt(l.getName());
//			long now_s  = Time.getInstance().getDateTime().getMillis();
//			DateTime syncTime = new DateTime(now_s - (now_s%3600000));
//			while(syncTime.getHourOfDay() != hourChanged) {
//				syncTime = syncTime.plus(3600000);
//			}
//			boolean success = bidModule.addBid(syncTime, bid*1000);
//			if(!success) {
//				l.setValue(bidModule.getBid(syncTime)/1000);
//			}
//		}
	}

}