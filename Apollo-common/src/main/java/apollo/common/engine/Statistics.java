package apollo.common.engine;

/**
 * metrics for bill class. This class captures the timing through the engine and returns that to the user from the bill
 * set by santana on 11/2/14.
 */
public class Statistics {
    //all times in milliseconds
    //absolute timing stats
    private long engineEntryTime = -1;
    private long messageProcessStartTime = -1;
    private long messageProcessEndTime = -1;
    private long billCreationTime = -1;
    private long billReadTime = -1;

    //latency stats
    private long messageProcessLatency = -1;
    private long endToEndLatency = -1;
    private long engineLatency = -1;


    /**
     * sets the time for when the engine first got placed on the engine queue. useful for end to end metrics
     * @return
     *      True - time was successfully set
     */
    public boolean setEngineTimestamp() {
        if(engineEntryTime == -1) {
            engineEntryTime = System.currentTimeMillis();
            return true;
        }
        System.out.println("Engine entry timestamp already set. Current timestamp is " + engineEntryTime);
        return false;
    }

    /**
     * sets the time for when the engine is off the queue and is getting processed
     * @return
     *      True - time was successfully set
     */
    public boolean setProcessingStartTime() {
        if(messageProcessStartTime == -1) {
            messageProcessStartTime = System.currentTimeMillis();
            return true;
        }
        System.out.println("process start timestamp already set. Current timestamp is " + messageProcessStartTime);
        return false;
    }

    /**
     * sets the time for when the engine finished getting processed
     * @return
     *      True - time was successfully set
     */
    public boolean setProcessingEndTime() {
        if(messageProcessEndTime == -1) {
            messageProcessEndTime = System.currentTimeMillis();
            messageProcessLatency = messageProcessEndTime - messageProcessStartTime;
            return true;
        }
        System.out.println("process end timestamp already set. Current timestamp is " + messageProcessEndTime);
        return false;
    }

    /**
     * sets the time for when the bill was created
     * @return
     *      True - time was successfully set
     */
    public boolean setBillTimestamp() {
        if(billCreationTime == -1) {
            billCreationTime = System.currentTimeMillis();
            engineLatency = billCreationTime - engineEntryTime;
            return true;
        }
        System.out.println("bill timestamp already set. Current timestamp is " +  billCreationTime);
        return false;
    }

    /**
     * sets the time that the bill was finally read and removed from the engine storage
     * @return
     *      True - time was successfully set
     */
    public boolean setBillReadTime() {
        if(billReadTime == -1) {
            billReadTime = System.currentTimeMillis();
            endToEndLatency = billReadTime - engineEntryTime;
            return true;
        }
        System.out.println("bill read timestamp already set. Current timestamp is " +  billReadTime);
        return false;
    }

    /**
     * gets the time for when the engine first got placed on the engine queue. useful for end to end metrics
     * @return
     *      timestamp in epoch
     */
    public long getEngineTimestamp() {
        return engineEntryTime;
    }

    /**
     * gets the time for when the engine is off the queue and is getting processed
     * @return
     *      timestamp in epoch
     */
    public long getProcessingStartTime() {
        return messageProcessStartTime;
    }

    /**
     * gets the time for when the engine finished getting processed
     * @return
     *      timestamp in epoch
     */
    public long getProcessingEndTime() {
        return messageProcessEndTime;
    }

    /**
     * gets the time for when the bill was created
     * @return
     *      timestamp in epoch
     */
    public long getBillTimestamp() {
        return billCreationTime;
    }

    /**
     * gets the time that the bill was finally read and removed from the engine storage
     * @return
     *      timestamp in epoch
     */
    public long getBillReadTime() {
        return billReadTime;
    }

    /**
     * The latency from the start of processing to the end of processing
     * @return
     *      latency in milliseconds
     */
    public long getMessageProcessingLatency() {
        return messageProcessLatency;
    }

    /**
     * gets the time from entering the engine to the bill being created and put on the queue
     * @return
     *      latency in milliseconds
     */
    public long getEngineLatency() {
        return engineLatency;
    }

    /**
     * gets the time from entering the engine to leaving the engine
     * @return
     *      latency in milliseconds
     */
    public long getEndToEndLatency() {
        return endToEndLatency;
    }

    /**
     * string summary of the stats for printing
     * @return
     *      String summary
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Bill Statistics: (All times are in milliseconds)\n");
        builder.append("engine entry time - " + getEngineTimestamp() + "\n");
        builder.append("engine processing start time - " + getProcessingStartTime() + "\n");
        builder.append("engine processing end time - " + getProcessingEndTime() + "\n");
        builder.append("bill creation time - " + getBillTimestamp() + "\n");
        builder.append("bill Read time - " + getBillReadTime() + "\n");
        builder.append("Latencies:\n");
        builder.append("end-to-end latency - " + getEndToEndLatency() + "\n");
        builder.append("engine latency - " + getEngineLatency() + "\n");
        builder.append("engine processing latency - " + getMessageProcessingLatency() + "\n");
        return builder.toString();
    }



}
