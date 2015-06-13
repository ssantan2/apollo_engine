package apollo.trade.swap.manager;

import apollo.common.engine.Bill;
import apollo.common.engine.BookSnapshot;
import apollo.common.engine.Message;
import apollo.common.templates.Book;
import apollo.common.templates.Make;
import apollo.common.templates.Model;
import apollo.common.templates.Swap;

import java.util.*;

/**
 * Manager class for Message. This class handles the Different Message types and based
 * on the type processes the engine.The three major types are:
 * SWAP - Exchange a shoe you have (bid) for a shoe you want (ask)
 * GOAL - Give a possible trade path from the shoe you have (bid) to the shoe you want (goal)
 * GRAB - Grab a shoe that matches (bid) and is also for sale
 * Created by santana on 7/28/14.
 */
public class MessageManager {
    //map of ModelManagers by their Make
    public Map<Make, ModelManager> makeManager = null;
    private boolean dirtyBook = false;

    /**
     * default constructor will create a clean book
     */
    public MessageManager() {
        makeManager = new HashMap<Make, ModelManager>();
    }

    /**
     * main constructor with BookSnapshot being set based on the snapshot passed in
     * @param snapshot
     *          BookSnapshot of the last instance of the engine. will be used to repopulate the engine
     */
    public MessageManager(BookSnapshot snapshot) {
        rebuildBook(snapshot);
    }

    /**
     * method that processes the engine passed in based on the engine type.
     * @param message
     *          engine to be processed
     * @return
     *          Bill of the engine passed in.
     */
    public Bill processMessage(Message message) {
        List<Swap> chain = null;

        switch(message.getType()) {
            //SWAP: Checks both
            case Message.SWAP:
                chain =  processSwap(message);
                break;

            //GRAB: takes the ask swap and queries the book to see if there are any available that are for sale
            case Message.GRAB:
                chain = processGrab(message);
                break;

            //GOAL: Takes the bid and returns the path of swaps that can occur in the engine
            // right now to get you your goal
            case Message.GOAL:
                chain = processGoal(message);
                break;
        }

        return new Bill(chain, message.getId(), message.getStatistics());
    }

    /**
     * Internal process method for swap engine
     * @param message
     *          SWAP engine
     * @return
     *         List of swaps for this engine
     */
    private List<Swap> processSwap(Message message) {
        Swap bid = message.getBid();
        Swap ask = message.getAsk();
        Swap match = null;
        Make make = null;
        ModelManager modelManager;
        List<Swap> chain = new ArrayList<Swap>();

        //check the bids modelManager for the proper book and possible fill
        if(validate(bid)) {
            make = bid.getModel().getMake();
            modelManager = makeManager.get(make);

            if(modelManager == null) {
                modelManager = new ModelManager(make);
                makeManager.put(make, modelManager);
            }
            match = modelManager.addAndFill(bid, ask);
        }

        //check the ask side to match
        if(match == null && validate(ask)) {
            Make askMake  = ask.getModel().getMake();

            if(!make.equals(askMake)){
                make = ask.getModel().getMake();
                modelManager = makeManager.get(make);

                if(modelManager == null) {
                    modelManager = new ModelManager(make);
                    makeManager.put(make, modelManager);
                }
                match = modelManager.addAndFill(bid, ask);
            }
        }
        //add match to chain
        if(match != null) {
            chain.add(bid);
            chain.add(match);
        }

        dirtyBook = true;
        return chain;
    }

    /**
     * Internal process Grab method
     * @param message
     *      GRAB engine
     * @return
     *      List of swaps for this engine
     */
    private List<Swap> processGrab(Message message) {
        Swap ask = message.getAsk();
        Swap match = null;
        ModelManager modelManager;
        List<Swap> chain = new ArrayList<Swap>();

        if(validate(ask)) {
            Make make = ask.getModel().getMake();
            modelManager = makeManager.get(make);

            if(modelManager == null) {
                modelManager = new ModelManager(make);
                makeManager.put(make, modelManager);
            }
            match = modelManager.grab(ask);
        }

        if(match != null) {
            chain.add(match);
            dirtyBook = true;
        }
        return chain;
    }

    /**
     * internal process goal engine
     * @param message
     *          GOAL engine
     * @return
     *         List of swaps for this engine
     */
    private List<Swap> processGoal(Message message) {
        Swap bid = message.getBid();
        Swap goal = message.getGoal();

        List<Swap> chain = goalSearch(bid, goal, message.getMaxBillLength());
        return chain;
    }

    /**
     * attempts to look for a match on ask that wants bid.
     * @param bid
     *          the swap you have
     * @param ask
     *          the swap you want
     * @return
     *          a swap matching the swap you want
     */
    private Swap match(Swap bid, Swap ask) {
        Swap goalMatch = null;
        Make make;
        ModelManager modelManager;

        if(validate(bid)) {
            make = bid.getModel().getMake();
            modelManager = makeManager.get(make);

            //if model manager doesn't exist for this make, make one
            if(modelManager == null) {
                modelManager = new ModelManager(make);
                makeManager.put(make, modelManager);
            }

            goalMatch = modelManager.match(bid, ask);
        }

        if(goalMatch == null && validate(ask)) {
            make = ask.getModel().getMake();
            modelManager = makeManager.get(make);

            //if model manager doesn't exist for this make, make one
            if(modelManager == null) {
                modelManager = new ModelManager(make);
                makeManager.put(make, modelManager);
            }

            goalMatch = modelManager.match(bid, ask);
        }

        return goalMatch;
    }

    /**
     * Looks for a path in the engine where he can start with trading his bid and end up with the goal
     * @param bid
     *          Swap he has
     * @param goal
     *          Swap he wants ultimately
     * @param TTL
     *          Longest possible length of the goal path.
     * @return
     *          Goal path from bid to swap. Empty if TTL was reached or no match was found
     */
    private List<Swap> goalSearch(Swap bid, Swap goal, int TTL) {
        //sanity check
        if(!validate(bid) || !validate(goal) || TTL < 0) {
            return null;
        }

        List<Swap> chain;

        //recursive base cases
        //CASE 1: if the bid and the goal are the same and there is space in the path still, return that bid
        if(bid.match(goal) != null && TTL > 0) {
            chain = new LinkedList<Swap>();
            chain.add(bid);
            return chain;
        }

        //get a set of all swaps that people who have the goal item want.
        Make make = goal.getModel().getMake();
        ModelManager modelManager = makeManager.get(make);

        Set<Swap> goalDown = modelManager.getAsks(goal);

        //if both sets aren't null we can continue down a level
        if(goalDown != null) {
            for (Swap newGoal : goalDown) {
                chain = goalSearch(bid, newGoal, --TTL);
                if (chain != null) {
                    //we check to see if the last element in the chain is a match to goal, otherwise we have to
                    //throw it out
                    Swap nextBid = ((LinkedList<Swap>) chain).peekLast();
                    Swap goalMatch = match(nextBid, goal);

                    //if it is a match. add the bid to the front and the goal to the back, return that chain
                    if (goalMatch != null) {
                        ((LinkedList<Swap>) chain).addLast(goalMatch);
                        return chain;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Goes through the current state of the model managers and returns the complete books for each model in the engine.
     * @return
     *      the current state of the all the books in the engine
     */
    public BookSnapshot getBookSnapshot() {
        BookSnapshot bookSnapshot = null;

        if(dirtyBook) {
            bookSnapshot = new BookSnapshot();
            Set<Make> makers = makeManager.keySet();
            for(Make make : makers) {
                ModelManager manager = makeManager.get(make);

                Map <Model, Book> bookStore = manager.getBookstore();
                bookSnapshot.add(bookStore);
            }
            dirtyBook = false;
        }

        return bookSnapshot;
    }

    /**
     * Rebuilds the make manager with the snapshot that was passed in.
     * @param snapshot
     */
    private void rebuildBook(BookSnapshot snapshot) {
        makeManager = new HashMap<Make, ModelManager>();

        //rebuild book from snapshot
        if(snapshot != null && !snapshot.isEmpty()) {
            Set<Make> makes = snapshot.getMakers();
            for(Make make : makes) {
                Map<Model, Book> bookMap = snapshot.getBooksForMake(make);
                ModelManager manager = new ModelManager(make, bookMap);
                makeManager.put(make, manager);
            }
        }
    }

    /**
     * checks whether the make passed in has any books.
     * @param make
     *      Make that you want to check the status of
     * @return
     *      True - there are no active books for this make. False - there is activity
     */
    public boolean isEmpty(Make make) {
        ModelManager modelManager = makeManager.get(make);
        Map<Model, Book> bookStore = modelManager.getBookstore();
        List<Book> books = (ArrayList) bookStore.values();

        for(Book book : books) {
            if(!book.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    /**
     * checks to see if evey thing in the engine manager is empty
     * @return
     *      True - for all makes, all of the model's books are empty or none existent.
     */
    public boolean isEmpty() {
        Set<Make> makers = makeManager.keySet();

        for(Make make : makers) {
            if(!isEmpty(make)) {
                return false;
            }
        }
        return true;
    }

    /**
     * flushes every model managers book map that is in memory
     * @return
     *      returns true if no flushes failed
     */
    public boolean flush() {
        boolean flushed = true;

        Set<Make> makes = makeManager.keySet();
        for(Make make : makes) {
            ModelManager modelManager = makeManager.get(make);
            if(!modelManager.flush()) {
                //if one fails. continue flushing but return flush failed
                flushed = false;
            }
        }

        return flushed;
    }

    /**
     * swap validation method. Checks whether the swap is not null and is valid
     * @param swap
     *      The swap that should be validated
     * @return
     *      True - valid swap
     *      False - invalid swap
     */
    private boolean validate(Swap swap) {
        if(swap != null && swap.valid()) {
            return true;
        }
        return false;
    }

}
