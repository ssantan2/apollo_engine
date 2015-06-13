package apollo.common.templates;

import java.util.Set;

/**
 * Book Class manages the book of bids and asks.
 * A book can manage any set of bid and asks (1-tier) So it is up to users to manage upper levels.
 * Created by santana on 7/28/14.
 */
public class Book<V extends SwapMapper, T extends Swap> {
    //Map of a set of bids that are asking for the swap that is mapped (some has a swap and they want to what people are offering for it)
    private volatile BookMap<V, T> bids = null;

    //Map of a set of bids that match the mapping of that bid (someone wants a swap and they want to see what are available)
    private volatile BookMap<V, T> asks = null;

    //Map of a set of swaps that people are asking for in exchange for the bid (key)
    private volatile BookMap<V, T> goals = null;

    //amount of bids that have been placed in this book
    private volatile int swapSize = 0;


    /**
     * Book constructor that initializes the ask, bid and goal maps
     */
    public Book() {
        bids = new BookMap<V, T>();
        asks = new BookMap<V, T>();
        goals = new BookMap<V, T>();
    }

    /**
     * Searches for a swap matching the ask criteria that wants what the bid is.
     * If a match is found. it removes the match from the engine and send the matching swap back, linking two to each other.
     * If nothing exists, return null
     * @param bid
     *          The swap the person has
     * @param ask
     *          The swap the person wants
     * @return
     *          an actual swap that matches the ask
     */
    public T fillBook(T bid, T ask) {
        T match = bids.get(bid, ask);
        if(match != null) {
        	removeFromBook(bid, match);
        }
        return match;
    }

    /**
     *  Searches for a swap matching the ask criteria that wants what the bid is, without editing the book.
     * If nothing exists, it returns null.
     * @param bid
     *          The swap the person has
     * @param ask
     *          The swap the person wants
     * @return
     *          an actual swap that matches the ask
     */
    public T match(T bid, T ask) {
    	return bids.check(bid, ask);
    	
 
    }

    /**
     * Searches for a swap matching the ask criteria.
     * If a match is found it will flag that swap, remove it from the book and return it.
     * If nothing exists, it returns null.
     * @param ask
     *          The swap that you want
     * @return
     *          A match to the swap you are looking for
     */
    public T grab(T ask) {
    	return asks.get(ask);
    }

    /**
     * gets a list of all the swaps that someone will trade bid for.
     * @param bid
     *          The swap that is looking to be sold
     * @return
     *          set of swaps that people want who have bid
     */
    public Set<T> getAsks(T bid) {
        return goals.getSet(bid);
    }

    /**
     * gets a list of all the swaps that match bid that are in this book.
     * @param bid
     *          The swap that matches the criteria you are looking for
     * @return
     *          set of swaps that people are selling that match the bid criteria
     */
    public Set<T> getBids(T bid) {
        return bids.getSet(bid);
    }

    /**
     * get all bids that are in this book.
     * @return
     *      A set of all bids in this book.
     */
    public Set<T> getAllBids() {
        return bids.getAll();
    }

    /**
     * Adds the bid and ask to the book
     * @param bid
     *          The swap that someone is willing to trade
     * @param ask
     *          The swap that someone wants in return.
     */
    public void addToBook(T bid, T ask) {
    	//only add to the asks book if the bid is for sale
    	if(bid.isForSale()) {
    		addToMap(bid, bid, asks);
    	}
        addToMap(ask, bid, bids);
        addToMap(bid, ask, goals);
        swapSize++;
    }

    /**
     * removes the bid and ask from the book
     * @param bid
     *          The swap that someone is willing to trade
     * @param ask
     *          The swap that someone wants in return.
     */
    private void removeFromBook(T bid, T ask) {
        removeFromMap(ask, ask, asks);
        removeFromMap(bid, ask, bids);
        removeFromMap(ask, bid, goals);
        swapSize--;
    }

    /**
     * Adds the specific key swap and value swap to the map for the book
     * @param key
     *      The swap you want to map the entry swap to.
     * @param entry
     *      The swap that you are storing in the map
     * @param map
     *      the map you are adding the key/value pair to
     */
    private void addToMap(T key, T entry, BookMap<V,T> map) {
        if(key != null && entry != null && map != null) {
            map.put(key, entry);
        }
    }

    /**
     * removes the specific key swap and value swap to the map for the book
     * @param key
     *      The swap you want to map the entry swap to.
     * @param entry
     *      The swap that you are removing in the map
     * @param map
     *      the map you are removing the key/value pair to
     */
    private void removeFromMap(T key, T entry,BookMap<V, T> map) {
        if(key != null && entry != null && map != null) {
            map.remove(key, entry);
        }
    }

    /**
     * Returns whether or not the book has no bids
     * @return
     *      True - If book is empty
     */
    public boolean isEmpty() {
        if(swapSize == 0) {
            return true;
        }
        return false;
    }

    /**
     * how many bids are in the book.
     * @return
     *      the amount of bids in the book.
     */
    public int size() {
        return swapSize;
    }

    /**
     * clears all the maps and sets the swapSize to zero
     */
    public void flush() {
        bids.clear();
        asks.clear();
        goals.clear();
        swapSize = 0;
    }

}
