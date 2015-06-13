package apollo.trade.swap.service;


import apollo.common.engine.Bill;
import apollo.common.engine.BookSnapshot;
import apollo.common.engine.Message;
import apollo.common.engine.Statistics;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static apollo.trade.swap.service.EngineCore.*;

/**
 * Static engine class that wraps the core thread and gives static access to the engine functionality. interface used
 * by all clients
 * Created by santana on 7/27/14.
 */
public class Engine {

    //map of old bills from the engine in case anyone looks for them
    private static Map<UUID, Bill> billOrganizer = new ConcurrentHashMap<UUID, Bill>();

    //the core of the engine
    private static EngineCore core = new EngineCore(null);

    //book snapshot
    private static BookSnapshot bookSnapshot = null;

    /**
     * default constructor
     */
    private Engine() {

    }

    /**
     * static start method. if engine is down this method will restart it
     */
    public static synchronized void start() {
        start(null);
    }
    
    /**
     * static start method with book snapshot passed in. if engine is down this method will restart it
     * @param snapshot
     * 			Snapshot of the book that should be loaded in if this engine has not started yet
     * 
     */
    public static synchronized void start(BookSnapshot snapshot) {
        //if core was nullified for some reason
        if(core == null) {
            System.out.println("WARNING: Core vanished Recreating...");
            core = new EngineCore(bookSnapshot);
        }

        //if engine is dead, restart
        if(down()) {
            System.out.println("Engine died. Recreating...");
            core = new EngineCore(bookSnapshot);
        }
        
    	//if snapshot passed in is not null then recreate engine with this snapshot (only if engine is warming up)
        if(warmingUp() && snapshot != null) {
        	System.out.println("Recreating engine with snapshot...");
        	bookSnapshot = snapshot;
            core = new EngineCore(bookSnapshot);
        }
        //if engine is new, thread start
        if(warmingUp()) {
            System.out.println("Starting engine...");
            new Thread(core).start();
        }

        //engine is running so leave it alone
        if(running()) {
            System.out.println("Engine running");
        }
    }

    /**
     * stops the core and sets the book snapshot
     */
    public static synchronized void stop() {
        stopCore();
        bookSnapshot = getBookSnapshot();
        System.out.println("Engine shut down and snapshot created");
    }

    /**
     * restarts the engine core and flushes the entire book so the engine core starts clean
     */
    public static synchronized void flush() {
        System.out.println("Flushing all books in the engine...");

        core.flush();
        bookSnapshot = null;
        System.out.println("Engine and snapshot cleaned");
    }

    /**
     * internal method that stops the current core of the engine and cleans out the bill queue
     */
    private static void stopCore() {
        if(running()) {
            System.out.println("Stopping engine...");
            core.stop();

            //let engine die before dumping bills to cache
            while(!down());

            //dump all bills from core to engine
            Bill bill = core.quickBill();
            while(bill != null) {
                billOrganizer.put(bill.getId(), bill);
                bill = core.quickBill();
            }
        }
    }

    /**
     * send a engine to the engine
     * @param message
     *      Message to be sent to the engine
     * @return
     *      UUID of the engine
     */
    public static synchronized UUID send(Message message) {
        if(message != null && message.isValid()) {
            return core.input(message);
        }
        return null;
    }

    /**
     * returns the bill for the id passed in
     * @param id
     *      id of the bill that should be retrieved
     * @return
     *      the Bill with the matching id
     */
    public static synchronized Bill getBill(UUID id) {
        Bill bill = null;

        if(id != null) {
            bill = billOrganizer.remove(id);

            //no bill in the organizer
            if(bill == null) {
                do {
                    bill = core.nextBill();
                    if(bill != null && bill.getId().equals(id)) {
                        break;
                    }
                    else {
                        billOrganizer.put(bill.getId(), bill);
                    }
                } while(bill != null);
            }
        }

        //set the bill read time in the stats
        if(bill != null) {
            Statistics stats = bill.getStatistics();
            if(stats != null) {
                stats.setBillReadTime();
            }
        }
        return bill;
    }

    /**
     * gets the snapshot of the current book from the core
     * @return
     *      snapshot of the current state of the book
     */
    public static synchronized BookSnapshot getBookSnapshot() {
        BookSnapshot snapshot = core.getBookSnapshot();
        if(snapshot != null) {
            bookSnapshot = snapshot;
        }
        return bookSnapshot;
    }

    /**
     * state check: is engine running?
     * @return
     *      true if engine is running
     */
    public static synchronized boolean running() {
        if(core.state() == CORE_STATE.RUNNING) {
            return true;
        }
        return false;
    }

    /**
     * state check: is engine ready to start?
     * @return
     *      true if engine is warming up
     */
    public static synchronized boolean warmingUp() {
        if(core.state() == CORE_STATE.WARMING) {
            return true;
        }
        return false;
    }

    /**
     * state check: is engine idle?
     * @return
     *      true if engine is idle
     */
    public static synchronized boolean idle() {
        if(core.state() == CORE_STATE.IDLE) {
            return true;
        }
        return false;
    }

    /**
     * state check: is engine dead?
     * @return
     *      true if engine is dead
     */    
    public static synchronized boolean down() {
        if(core.state() == CORE_STATE.DEAD) {
            return true;
        }
        return false;
    }
}
