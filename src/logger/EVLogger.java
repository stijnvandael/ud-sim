package logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.TimerTask;

import au.com.bytecode.opencsv.CSVWriter;
import electricvehicle.agent.EVAgent;

public class EVLogger extends TimerTask{

	private EVAgent evAgent;
	
	//writers
	private CSVWriter powerWriter;
	private CSVWriter socWriter;

	//basedir for logger
	private String baseDir;
	
	//temp log variables
	private long[] power_temp;
	private long[] soc_temp;
	
	public EVLogger(EVAgent evAgent, String baseDir) {
		this.evAgent = evAgent;
		this.baseDir = baseDir + "/" + evAgent.getId();
		init();
	}
	
	public void init() {
		//logging
		 try {
			//create aggregator directory
			File aggregatorDir = new File(baseDir);
			if(!aggregatorDir.exists()) {
				aggregatorDir.mkdir();
			}
			//create individual files
			powerWriter = new CSVWriter(new FileWriter(baseDir + "/power.csv"), ',', CSVWriter.NO_QUOTE_CHARACTER);
			socWriter = new CSVWriter(new FileWriter(baseDir + "/soc.csv"), ',', CSVWriter.NO_QUOTE_CHARACTER);
		} catch (IOException e) {
			// TODO CREATE BACKUP STRATEGY
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		String[] power = new String[1];
		power[0] = Long.toString(evAgent.getModuleManager().getBatteryModule().getCurrentPower().inWatt());
		powerWriter.writeNext(power);
		
		String[] soc = new String[1];
		soc[0] = Long.toString(evAgent.getModuleManager().getBatteryModule().getCurrentEnergy().inWH());
		socWriter.writeNext(soc);
	}
	
	public void stop() {
		try {
			powerWriter.close();
			socWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
