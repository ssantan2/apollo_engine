package apollo.trade.swap.service;

import apollo.common.engine.Bill;
import apollo.common.engine.BookSnapshot;
import apollo.common.engine.Message;
import apollo.common.engine.Statistics;
import apollo.trade.swap.manager.MessageManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * The core of the engine. This thread does all the work whereas the engine class does the managing of the thread and
 * wraps the core methods
 * Created by santana on 10/18/14.
 */
public class EngineCore implements Runnable {

    //enums for core state
    public enum CORE_STATE {
        WARMING, DEAD, RUNNING, IDLE

    }

    //the inbound engine queue
    private volatile BlockingQueue<Message> messageQueue;

    //the outbound bill queue
    private volatile BlockingQueue<Bill> billQueue;

    //the engine manager that processes all the messages
    private volatile MessageManager messageManager;

    //the state of the core
    private volatile CORE_STATE state;

    //core ID
    private UUID id;


    /**
     * Core constructor that starts core and creates the queues, creates the engine manager
     * and sets the state to WARMING
     * @param snapshot
     *      last state of the book so the core can reset itself where it left off last
     */
    public EngineCore(BookSnapshot snapshot) {
        state = CORE_STATE.WARMING;

        id = UUID.randomUUID();

        messageQueue = new LinkedBlockingQueue<Message>(1000);
        billQueue = new LinkedBlockingQueue<Bill>(1000);

        messageManager = new MessageManager(snapshot);
    }

    /**
     * main core running logic. While core is running it will take a engine off the queue and process it. Once finished
     * it will create a bill and put it on the bill queue for the engine to take. If kill core engine gets received,
     * the engine will kill itself, drain the messages left in the queue and process them, adding the bills to the
     * outbound queue
     */
    public void run() {
        state = CORE_STATE.RUNNING;
        Statistics stats;

        //never break unless kill core engine gets received
        while(true) {
            try {
                //take engine off queue
                Message message = messageQueue.take();
                //get stats from engine
                stats = message.getStatistics();
                //if kill core is received. break loop
                if(message.getType() == Message.KILL_CORE && message.getId().equals(id) ) {
                    break;
                }
                //start processing
                stats.setProcessingStartTime();
                Bill bill = messageManager.processMessage(message);
                stats.setProcessingEndTime();
                //end processing

                //if bill is set than add to queue
                if(bill != null) {
                    billQueue.put(bill);
                }
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //set state to idle when engine is about to die and needs to be cleaned
        state = CORE_STATE.IDLE;

        //start processing of left over messages
        List<Message> messages = new ArrayList<Message>();

        messageQueue.drainTo(messages);

        for(Message message : messages) {
            stats.setProcessingStartTime();
            Bill bill = messageManager.processMessage(message);
            stats.setProcessingEndTime();
            //end processing

            //if bill is set than add to queue
            if(bill != null) {
                billQueue.add(bill);
            }
        }

        //core is now dead
        state = CORE_STATE.DEAD;
    }

    /**
     * non blocking input of engine to queue
     * @param message
     *      Message to be added to queue
     * @return
     *      UUID of engine
     */
    public UUID quickInput(Message message) {
        if(message != null) {
            Statistics stats = new Statistics();
            stats.setEngineTimestamp();
            message.setStatistics(stats);

            if((state == CORE_STATE.RUNNING || state == CORE_STATE.WARMING) && messageQueue.offer(message)) {
                stats.setEngineTimestamp();
                return message.getId();
            }
        }
        return null;

    }

    /**
     * blocking input of engine to queue
     * @param message
     *      Message to be added to queue
     * @return
     *      UUID of engine
     */
    public UUID input(Message message) {
        if(message != null) {
            //create stats object and set the engine timestamp. add to engine for later processing
            Statistics stats = new Statistics();
            stats.setEngineTimestamp();
            message.setStatistics(stats);

            if((state == CORE_STATE.RUNNING || state == CORE_STATE.WARMING)) {
              try {
                    messageQueue.put(message);
                    return message.getId();
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
        return null;

    }

    /**
     *  blocking retrieval of bill. will wait until a bill is available
     * @return
     *      the next bill on the queue
     */
    public Bill nextBill() {
        Bill bill = null;
        try {
            bill =  billQueue.take();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            bill = null;
        }
        return bill;
    }

    /**
     * non block retrieval of bill
     * @return
     *      next bill on the queue. null if no bills exist
     */
    public Bill quickBill() {
        Bill bill = null;
        try {
            //times out after 2 seconds
            bill = billQueue.poll(2, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            bill = null;
        }
        return bill;
    }

    /**
     * state of the core
     * @return
     *      returns state of the core
     */
    public CORE_STATE state() {
        return state;
    }

    /**
     * stops the core
     * @return
     *      true if the core stop engine was added to queue
     */
    public boolean stop() {
        Message message = Message.getMessage(id);
        try {
            messageQueue.put(message);
            return true;
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * flushes the book for every model in the engine. It also locks the messageQueue so no new messages get added and
     * or pulled from the queue while the flush is happening
     * @return
     *      returns true if the flush was successful
     */
    public boolean flush() {
        boolean flushed;
        synchronized (messageQueue) {
            flushed = messageManager.flush();
        }

        return flushed;
    }

    /**
     * gets the book snapshot from the engine manager
     * @return
     *      current state of the book
     */
    public BookSnapshot getBookSnapshot() {
       return messageManager.getBookSnapshot();
    }
}
