package apollo.common.engine;

import apollo.common.templates.Swap;
import apollo.common.util.ApolloHelper;

import java.util.UUID;

/**
 * Message class is used to create messages based on swaps that the engine can decipher. There are four types
 * of messages that this class creates currently:
 *
 * SWAP: a engine that has a bid and an ask. If there is a matching ask/bid combo already in the engine than this will
 * match it and send the swap matching your ask back
 *
 * GRAB: a engine that only has a swap that is the one the user wants to buy outright. It will return a swap that is
 * available for purchase if one exists in the engine
 *
 * GOAL: a engine that takes a swap you currently have (bid) and one that you ultimately want (goal) and determines
 * if there is a set of bid/ask combos that will lead your bid to your goal, it also takes in a TTL which determines
 * the maximum depth your goal will go before giving up
 *
 * KILL_CORE: this is a engine created internally by the engine to kill the engine cleanly. You cannot create this
 * engine externally
 *
 * Created by santana on 7/28/14.
 */
public class Message {
    //TYPE 1 = SWAP
    //TYPE 2 = GOAL
    //TYPE 3 = GRAB
    //TYPE 4 = KILL_CORE
    public static final int SWAP = 1;
    public static final int GOAL = 2;
    public static final int GRAB = 3;
    public static final int KILL_CORE = 4;

    //id of engine
    private UUID id = null;

    //the instance type; either SWAP, GOAL, GRAB or KILL_CORE
    private int type = -1;

    //bid, ask and goal swap
    private Swap bidSwap = null;
    private Swap askSwap = null;
    private Swap goalSwap = null;

    //maximum bill length, default set to the TTL constant
    private int maxBillLength = ApolloHelper.TTL;

    //is this a valid engine
    private boolean valid = false;

    //statistic object used for the bill
    private Statistics statistics = null;

    /**
     * private constructor used for creating the kill core engine
     * @param id
     *          ID of the engine
     * @param type
     *          type should be KILL_CORE
     */
    private Message(UUID id, int type) {
        if(id != null) {
            this.id = id;
            this.type = type;
            valid = true;
        }
    }

    /**
     * private base constructor. All get engine methods call this constructor internally to create the engine object
     * @param bidSwap
     *          the swap you own
     * @param askSwap
     *          the swap you want to trade your bid for
     * @param goalSwap
     *          the swap you ultimately want
     * @param type
     *          the engine type (one of the four types)
     * @param maxBillLength
     *          maximum length of chain
     */
    private Message(Swap bidSwap, Swap askSwap, Swap goalSwap, int type, int maxBillLength) {
        if(validate(bidSwap, askSwap, goalSwap, type)) {
            this.bidSwap = bidSwap;
            this.askSwap = askSwap;
            this.goalSwap = goalSwap;

            //any swaps that are added to this engine will need to be paired together
            syncPairs();

            this.type = type;

            //sets the engine id to a random UUID
            id = UUID.randomUUID();

            //if the swaps are valid than this is a valid engine
            valid = true;

            //cannot set max bill length to anything less than zero or greater than TTL
            if(maxBillLength > 0 && maxBillLength <= ApolloHelper.TTL) {
                this.maxBillLength = maxBillLength;
            }
            else {
                System.out.println("max bill length must be between 1 and " + ApolloHelper.TTL + ". Will default to "
                                    + ApolloHelper.TTL);
            }
        }
    }

    /**
     * Creates a SWAP Message
     * @param bidSwap
     *          The swap you have
     * @param askSwap
     *          the the swap you want to trade your bid for
     * @return
     *          the SWAP engine to send to the engine
     */
    public static Message getMessage(Swap bidSwap, Swap askSwap) {
        Message message = new Message(bidSwap, askSwap, null, SWAP, ApolloHelper.TTL);
        return message;
    }

    /**
     * Creates a GOAL Message
     * @param bidSwap
     *          the swap you own
     * @param goalSwap
     *          the swap you ultimately want
     * @return
     */
    public static Message getMessage(Swap bidSwap, Swap goalSwap, int maxBillLength) {
        Message message = new Message(bidSwap, null, goalSwap, GOAL, maxBillLength);
        return message;
    }

    /**
     * Creates a GRAB Message
     * @param askSwap
     *          The swap you want
     * @return
     *          the GRAB engine you send to the engine
     */
    public static Message getMessage(Swap askSwap) {
        Message message = new Message(null, askSwap, null, GRAB, ApolloHelper.TTL);
        return message;
    }

    /**
     * Creates a KILL_CORE engine
     * @param engineID
     *          The UUID of the engine that is running
     * @return
     */
    public static Message getMessage(UUID engineID) {
        Message message = new Message(engineID, KILL_CORE);
        return message;
    }

    /**
     * this method syncs all the swaps in the instance so that they have references to their pairs
     */
    private void syncPairs() {
        //if the bid is not null than set the pair to ask or goal (depending on which is valid )
        if(bidSwap != null) {
            if(askSwap != null) {
                bidSwap.setPair(askSwap);
                askSwap.setPair(bidSwap);
            }
            else if(goalSwap != null) {
                bidSwap.setPair(goalSwap);
                goalSwap.setPair(bidSwap);
            }
        }
    }

    /**
     * validates the swaps depending on the engine type that is set. If type is:
     *
     * SWAP: bid must be valid and if ask is not null than it also must be valid otherwise if ask is null than bid must
     * be for sale
     *
     * GRAB: ask must be valid
     *
     * GOAL: bid and goal must be valid
     *
     *  @param bid
     *          the swap you own
     * @param ask
     *          the swap you want to trade the bid for
     * @param goal
     *          the swap you ultimately want
     * @param type
     *         the the engine type (one of the four types)
     * @return
     *          true if the swaps are valid for the type that is passed in
     */
    private boolean validate(Swap bid, Swap ask, Swap goal, int type) {
        switch(type) {
            case SWAP:
                if(((bid != null) && bid.valid())) {
                    if(ask != null && ask.valid()) {
                        return true;
                    }
                    else if(ask == null && bid.isForSale()) {
                        return true;
                    }
                }
                break;
            case GOAL:
                if((bid != null && bid.valid())
                        && (goal != null && goal.valid())) {
                    return true;
                }
                break;
            case GRAB:
                if(ask != null && ask.valid()) {
                    return true;
                }
                break;
        }
        return false;
    }

    /**
     * is the engine valid
     * @return
     *      true if valid
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * type of the engine
     * @return
     *      returns type based on Message.GOAL, Message.GRAB, Message.SWAP, Message.KILL_CORE
     */
    public int getType() {
        return type;
    }

    /**
     * get the bid swap
     * @return
     *      returns the bid swap
     */
    public Swap getBid() {
        return bidSwap;
    }

    /**
     * gets the goal swap
     * @return
     *      returns the goal swap
     */
    public Swap getGoal() {
        return goalSwap;
    }

    /**
     * gets the ask swap
     * @return
     *      returns the ask swap
     */
    public Swap getAsk() {
        return askSwap;
    }

    /**
     * gets the engine ID
     * @return
     *      returns the engine ID
     */
    public UUID getId() {
        return id;
    }

    /**
     * gets the maximum bill length for this engine. Default is ApolloHelper.TTL
     * @return
     *      return the max length for bill
     */
    public int getMaxBillLength() {
        return maxBillLength;
    }

    /**
     * set the statistics object so the engine can carry it
     * @param stats
     *          statistics object used for metrics
     * @return
     *          true if the object was set successfully
     */
    public boolean setStatistics(Statistics stats) {
        if(stats != null && statistics == null) {
            statistics = stats;
            return true;
        }
        return false;
    }

    /**
     * get the statistics object
     * @return
     *      returns the statistics object
     */
    public Statistics getStatistics() {
        return statistics;
    }
}
