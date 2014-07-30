package aggregator.modules.bids;

import org.joda.time.DateTime;

/**
 * Interface from fleet manager to BidModule
 * 
 * @author Stijn
 *
 */
public interface BidAPI {

	public void receiveBid(DateTime syncTime, long bid);

}
