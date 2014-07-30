package aggregator.modules.bids;

import java.util.Hashtable;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import utils.ArrayMath;
import clock.Time;

public class BidModule {
	
	public static void main(String[] args) {
		BidModule bidModule = new BidModule(1000);
		
		DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
		
		DateTime dt1 = formatter.parseDateTime("04/02/2011 7:59:33");
		DateTime dt2 = formatter.parseDateTime("04/02/2011 8:00:34");
		bidModule.addFirstFreeBid(dt1, 1);
		ArrayMath.printArray(bidModule.getBidNrs(dt2, 2));
		
		dt1 = formatter.parseDateTime("04/02/2011 8:59:47");
		dt2 = formatter.parseDateTime("04/02/2011 9:00:47");
		bidModule.addFirstFreeBid(dt1, 1);
		ArrayMath.printArray(bidModule.getBidNrs(dt2, 2));
		
		dt1 = formatter.parseDateTime("04/02/2011 9:59:45");
		dt2 = formatter.parseDateTime("04/02/2011 10:00:45");
		bidModule.addFirstFreeBid(dt1, 1);
		ArrayMath.printArray(bidModule.getBidNrs(dt2, 2));
		
		
		
		
		//ArrayMath.printArray(bidModule.getBidNrs(bidModule.getCurrentOptimizationQuarter(dt2), 2));
		
	}
	
	private final Duration bidDuration = new Duration(3600*1000);
	private final long bidSize; //Wh
	private final Hashtable<DateTime, Long> bids;
	
	private BidAPI bidAPI;
	
	
	public BidModule(long bidSize) {
		this.bidSize = bidSize;
		bids = new Hashtable<DateTime, Long>();
	}
	
	/**
	 * Add bid to first timeslot where we can add a bid.
	 * 
	 * @param time
	 * @param bid
	 * @return
	 */
	public boolean addFirstFreeBid(DateTime time, int n) {
		DateTime syncFirstFreeBid = this.getBidSynchronizationTime(time.plus(2*3600*1000));
		return addBid(syncFirstFreeBid, n*this.bidSize);
	}
	
	//don't make public, otherwise, extra check for validity of time
	private boolean addBid(DateTime time, long bid) {
		bids.put(time, bid);
		if(bidAPI != null) {
			bidAPI.receiveBid(time, bid);
		}
		return true;
	}

	public long getBid(DateTime t) {
		DateTime bidSyncTime = getBidSynchronizationTime(t);
		if(bids.containsKey(bidSyncTime)) {
			return bids.get(bidSyncTime);
		}else{
			return 0;
		}
	}
	
	/**
	 * Calculates the nr. of the optimization quarter nr. in the current bid
	 * 
	 * @param time
	 * @return 1/2/3/4
	 */
	public int getCurrentOptimizationQuarter(DateTime time) {
		return (int) (Math.floor(time.getMillis()/900000)%4 + 1);
	}
	
	/**
	 * Calculate nr of bids in current market
	 */
	public int[] getBidNrs(DateTime t, int n) {
		long[] bids = getBids(t, n);
		int[] bidsN = new int[bids.length];
		for(int i = 0; i < bidsN.length; i++) {
			bidsN[i] = (int) (bids[i]/this.bidSize);
		}
		return bidsN;
	}
	
	public long[] getBids(DateTime t, int n) {
		long[] bids = new long[n];
		for(int i = 0; i < n; i++) {
			bids[i] = getBid(t);
			t = t.plus(3600*1000);
		}
		return bids;
	}
	
	public void setBidAPI(BidAPI bidAPI) {
		this.bidAPI = bidAPI;
	}
	
	/**
	 * Check if there is a new bid interval in s seconds 
	 */
	public boolean isNewBidInterval(DateTime currentTime, Duration d) {
		if(!getBidSynchronizationTime(currentTime).equals(getBidSynchronizationTime(currentTime.plus(d)))) {
			return true;
		}
		return false;
	}
	
	/**
	* calculate begintime of bid
	*/
	private DateTime getBidSynchronizationTime(DateTime time) {
		return time.minusMillis((int) (time.getMillisOfDay()%bidDuration.getMillis()));
	}

	public long getBidSize() {
		return bidSize;
	}
	
	public String getBids() {
		StringBuffer s = new StringBuffer();
		for(DateTime t: this.bids.keySet()) {
			s.append(t.getMillis() + ":" + this.bids.get(t) + " / ");
		}
		return s.toString();
	}
	
}