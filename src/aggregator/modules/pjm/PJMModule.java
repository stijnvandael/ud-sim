package aggregator.modules.pjm;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.TimerTask;

import org.apache.commons.lang3.ArrayUtils;

import units.PowerValue;
import au.com.bytecode.opencsv.CSVReader;
import clock.Time;

public class PJMModule {

	public static void main(String[] args) {
		//csv file containing data
		String strFile = "data/pjmprofile.csv";
		PJMModule pjm = new PJMModule(strFile);
		pjm.start();
	}

	private final ArrayList<PJMListener> listeners;
	private int[] regulationSignal;
	private PowerValue maxReg;
	
	public PJMModule(String filename) {
		listeners = new ArrayList<PJMListener>();
		regulationSignal = new int[0];
		readFile(filename);
		maxReg = new PowerValue(0);
		this.start();
	}

	public void start() {
        TimerTask task = new TimerTask() {
        	int n = 0;
        	@Override
        	public void run() {
    			if(n == regulationSignal.length) {
					n = 0;
				}
    			sendRegulationSignal(n);
				n++;
        	}
        };
        Time.getInstance().scheduleAtFixedRate("aggregator",task, 1000, 0);
	}
	
	public synchronized void sendRegulationSignal(int n) {
		for(PJMListener listener: listeners) {
			listener.receiveRegulationSignal((int) ((regulationSignal[n]*maxReg.inWatt())/100.0));
		}
	}
	
	public void setMaxRegulation(PowerValue maxReg) {
		this.maxReg = maxReg;
	}
	
	public synchronized void addListener(PJMListener listener) {
		this.listeners.add(listener);
	}
	
	private void readFile(String fileName) {
		try {
			CSVReader reader = new CSVReader(new FileReader(fileName));
			String [] nextLine;
			while ((nextLine = reader.readNext()) != null) {
				regulationSignal = ArrayUtils.addAll(regulationSignal, convert(nextLine));
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private int[] convert(String[] array) {
		int[] results = new int[array.length];
		for (int i = 0; i < array.length; i++) {
		    try {
		        results[i] = Integer.parseInt(array[i]);
		    } catch (NumberFormatException nfe) {};
		}
		return results;
	}
	
}
