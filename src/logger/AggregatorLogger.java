package logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.TimerTask;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import utils.ArrayMath;
import clock.Time;
import aggregator.agent.AggregatorAgent;
import au.com.bytecode.opencsv.CSVWriter;

public class AggregatorLogger extends TimerTask{

	private AggregatorAgent aggregatorAgent;
	
	//writers
	private CSVWriter powerWriter;
	private CSVWriter regWriter;
	private CSVWriter bidWriter;
	private CSVWriter flexWriter;

	//basedir for logger
	private String baseDir;
	
	//temp log variables
	private long[] flexMin_temp;
	private long[] flexMax_temp;
	private long[] flexPower_temp;
	private long[] flexPath_temp;
	
	public AggregatorLogger(AggregatorAgent aggregatorAgent, String baseDir) {
		this.aggregatorAgent = aggregatorAgent;
		this.baseDir = baseDir + "/aggregator";
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
			regWriter = new CSVWriter(new FileWriter(baseDir + "/reg.csv"), ',', CSVWriter.NO_QUOTE_CHARACTER);
			bidWriter = new CSVWriter(new FileWriter(baseDir + "/bid.csv"), ',', CSVWriter.NO_QUOTE_CHARACTER);
			flexWriter = new CSVWriter(new FileWriter(baseDir + "/flex.csv"), ',', CSVWriter.NO_QUOTE_CHARACTER);
		} catch (IOException e) {
			// TODO CREATE BACKUP STRATEGY
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		String[] power = new String[1];
		power[0] = Long.toString(aggregatorAgent.getModuleManager().getEVSessionManager().getTotalPower().inWatt());
		powerWriter.writeNext(power);
		
		String[] reg = new String[1];
		reg[0] = Long.toString(aggregatorAgent.getModuleManager().getEVSessionManager().getTotalUpReg().inWatt());
		regWriter.writeNext(reg);
		
		String[] bids = new String[1];
		bids[0] = Long.toString(aggregatorAgent.getModuleManager().getBidModule().getBids(Time.getInstance().getDateTime(), 1)[0]);
		bidWriter.writeNext(bids);
		
		DateTime syncTime = aggregatorAgent.getScheduler().getOptimizationSyncTime();
		long[] flexMin = aggregatorAgent.getScheduler().getFlexGraph();
		long[] flexMax = aggregatorAgent.getScheduler().getFlexGraphMax();
		long[] flexPower = aggregatorAgent.getScheduler().getFlexMax();
		long[] flexPath = aggregatorAgent.getScheduler().getFlexPath();
		
		if(syncTime != null && flexMin.length != 0 && flexMin.length != 0 && flexMin.length != 0 && flexMin.length != 0 && 
				(!ArrayMath.compare(flexMin, flexMin_temp) || !ArrayMath.compare(flexMax, flexMax_temp) || !ArrayMath.compare(flexPower, flexPower_temp) || !ArrayMath.compare(flexPath, flexPath_temp))) {
			String[] flex = ArrayMath.concat(	new String[]{Long.toString(syncTime.getMillis())},
												new String[]{ArrayMath.toString(flexMin,";")},
												new String[]{ArrayMath.toString(flexMax,";")},
												new String[]{ArrayMath.toString(flexPower,";")},
												new String[]{ArrayMath.toString(flexPath,";")}
											);
			flexWriter.writeNext(flex);
			//store temporarly
			this.flexMin_temp = flexMin;
			this.flexMax_temp = flexMax;
			this.flexPower_temp = flexPower;
			this.flexPath_temp = flexPath;
		}

	}
	
	public void stop() {
		try {
			powerWriter.close();
			regWriter.close();
			bidWriter.close();
			flexWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
