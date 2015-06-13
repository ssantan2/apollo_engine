package apollo.common.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import apollo.common.templates.Swap;

/**
 * Object returned from the engine. Each engine, when processed by the engine, gets a Bill. The bill
 * contains the chain of swaps that were traded (first is always your bid) or can be traded and the id
 * matches that of the engine that was passed in. It also contains stats for metrics purposes
 * Created by santana on 7/28/14.
 */
public class Bill {
    //trade chain of swaps
    private List<Swap> chain = null;

    //id of the bill. this should match the engine id
    private UUID id = null;

    //metrics for the bill such as latency, create date, etc
    private Statistics statistics = null;

    /**
     * Constructor for bill that takes a chain and an id
     * @param chain
     *          The swap chain for the engine passed in
     * @param id
     *          ID of the engine that this bill is for
     * @param statistics
     *          statistics for this engine/bill combo
     */
    public Bill(List<Swap> chain, UUID id, Statistics statistics) {
        if(chain != null) {
            this.chain = chain;
        }
        else {
            this.chain = new ArrayList<Swap>();
        }
        this.id = id;
        this.statistics = statistics;

        //sets the bill creation timestamp
        if(this.statistics != null) {
            this.statistics.setBillTimestamp();
        }
    }

    /**
     * gets the swap at the specified index
     * @param index
     *          Index of the chain
     * @return
     *          Swap at the index
     */
    public Swap get(int index) {
        if(index >= 0 && index < chain.size()) {
            return chain.get(index);
        }
        return null;
    }

    /**
     * size of the swap chain
     * @return
     *      size
     */
    public int size() {
        return chain.size();
    }
    
    /**
     * returns the goal chain
     * @return
     *      the goal chain
     */
    public List<Swap> goalChain() {
        return new ArrayList<Swap>(chain);
    }

    /**
     * gets the ID of the Bill
     * @return
     *      id of bill
     */
    public UUID getId() {
        return id;
    }

    /**
     * get create time of this bill
     * @return
     *      long form of the create time in milliseconds from epoch
     */
    public long getTimestamp() {
        long timestamp = -1;
        if(statistics != null) {
            timestamp = statistics.getBillTimestamp();
        }
        return timestamp;
    }

    /**
     * returns the statistics object instead of wrapping the stats. may want to wrap it at a later time
     * @return
     *     the statistics object
     */
    public Statistics getStatistics() {
        return statistics;
    }

}
