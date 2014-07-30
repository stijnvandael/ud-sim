package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CSVReader {
	
	public static void main(String[] args) {
		try {
			CSVReader r = new CSVReader("data/evprofile_test.csv");
			List<EVTuple> tuples = r.readTuples();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String fileName;
	
	public CSVReader(String fileName) {
		this.fileName = fileName;
	}
	
	public List<EVTuple> readTuples() throws IOException {
		InputStream is = new FileInputStream(new File(fileName));
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		 
		List<EVTuple> persons = br.lines()
		    .map(mapToPerson)
		    .collect(Collectors.toList());
		br.close();
		
		return persons;
	}

	public Function<String, EVTuple> mapToPerson = (line) -> {
	  String[] p = line.split(",");
	  return new EVTuple(p[0], Integer.parseInt(p[1]), Integer.parseInt(p[2]), Integer.parseInt(p[3]), Integer.parseInt(p[4]), Integer.parseInt(p[5]), Integer.parseInt(p[6]), Integer.parseInt(p[7]));
	};
	
}

class EVTuple {
	
	private final String evName;
	private final int arrivalTime;
	private final int departureTime;
	private final int currentEnergy;
	private final int requestedEnergy;
	private final int minimumEnergy;
	private final int maximumEnergy;
	private final int maximumPower;
	
	public EVTuple(String evName, int arrivalTime, int departureTime, int currentEnergy, int requestedEnergy, int minimumEnergy, int maximumEnergy, int maximumPower) {
		this.evName = evName;
		this.arrivalTime = arrivalTime;
		this.departureTime = departureTime;
		this.currentEnergy = currentEnergy;
		this.requestedEnergy = requestedEnergy;
		this.minimumEnergy = minimumEnergy;
		this.maximumEnergy = maximumEnergy;
		this.maximumPower = maximumPower;
	}
	
	public String getEVName() {
		return evName;
	}

	public int getArrivalTime() {
		return arrivalTime;
	}

	public int getDepartureTime() {
		return departureTime;
	}
	
	public int getCurrentEnergy() {
		return currentEnergy;
	}

	public int getRequestedEnergy() {
		return requestedEnergy;
	}

	public String getEvName() {
		return evName;
	}

	public int getMinimumEnergy() {
		return minimumEnergy;
	}

	public int getMaximumEnergy() {
		return maximumEnergy;
	}

	public int getMaximumPower() {
		return maximumPower;
	}

}
