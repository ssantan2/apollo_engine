package apollo.trade.swap.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import apollo.common.shoe.ShoeMake;
import apollo.common.templates.Book;
import apollo.common.templates.Make;
import apollo.common.templates.Model;
import apollo.common.templates.Swap;
import apollo.common.templates.SwapMapper;

/**
 * Manages the book for each model in the engine. Any editing of the book or matching gets done through this class
 * Created by santana on 7/27/14.
 */
public class ModelManager {
    //make of all the models in this model manager
    private Make make = null;
    //map of all the models and books associated with the models
    private Map<Model, Book> bookMap = null;

    /**
     * Constructor that takes only a make and makes a blank book map
     * @param make
     *          The make of the model manager
     */
    public ModelManager(Make make) {
        if(make != null) {
            this.make = make;
        }
        else {
            //if the make passed in is unknown set the model make to unknown
            this.make = new ShoeMake("UNKNOWN");
        }
        bookMap = new HashMap<Model, Book>();
    }

    /**
     * Constructor that takes a make and also a current book map associated with that make
     * @param make
     *          The make of the model manager
     * @param bookMap
     *          The books of all models in for this make
     */
    public ModelManager(Make make, Map<Model, Book> bookMap) {
        this(make);
        if(make != null && bookMap != null) {
            //sets the make and creates a blank bookMap

            Set<Model> modelSet = bookMap.keySet();
            //loops through all models, only adding the ones to the map that are for this make
            for(Model model : modelSet) {
                if(make.equals(model.getMake())) {
                    this.bookMap.put(model, bookMap.get(model));
                }
            }
        }
    }

    /**
     * checks the book for a match for the swap passed in. If there is a match than the matching swap with user information
     * is returned and that swap is removed from the engine (matched)
     * @param ask
     *      The swap that is wanted
     * @return
     *      The swap that matches the one wanted. If no swap matched then return null
     */
    public Swap grab(Swap ask) {
        //checks the validity of the swap and makes sure the makers of this model manager and the swap match
        if(validate(ask)) {
            Model model = ask.getModel();

            Book book = bookMap.get(model);
            if(book == null) {
                book = new Book<SwapMapper, Swap>();
                bookMap.put(model, book);
            }
            return book.grab(ask);
        }
        return null;
    }

    /**
     * attempts to find a swap that matches ask and is looking for bid. This is a read only and does not edit the book
     * (does not match the actual swap and remove it from the engine)
     * @param bid
     *          The swap you have
     * @param ask
     *          The swap you want
     * @return
     *          A swap matching the ask criteria that has the user information in it.
     */
    public Swap match(Swap bid, Swap ask) {
        return internalMatch(bid, ask, false);
    }

    /**
     * attempts to find a swap that matches ask and is looking for bid. If a match is found it flags it,
     * removes it from the engine and returns it. If no match is found it adds that to the book so that in the future
     * if a someone enters the proper bid/ask it will get matched
     * @param bid
     *      the swap you have and want to trade
     * @param ask
     *      the swap you want to trade you bid for
     * @return
     *      The swap that matches the criteria of ask and is looking for bid.
     */
    public Swap addAndFill(Swap bid, Swap ask) {
        Swap match = internalMatch(bid, ask, true);
        if(match == null && bid != null && validate(bid)) {
            Book book = bookMap.get(bid.getModel());
            if(book == null) {
            	book = new Book();
            	bookMap.put(bid.getModel(), book);
            }
            book.addToBook(bid, ask);
        }

        return match;
    }

    /**
     * Internal match process for addAndFill() and match() methods. If fill is true than it actually fills the order,
     * otherwise it just looks for a match and returns that
     * @param bid
     *         the swap you have and want to trade
     * @param ask
     *         the swap you want to trade you bid for
     * @param fill
     *         True - edit book and try to fill this order, if you cannot than put it in the book
     *         False - just try to see if a match exists. not editing the book in any way
     * @return
     *         The swap that matches the criteria of ask and is looking for bid.
     */
    private Swap internalMatch(Swap bid, Swap ask, boolean fill) {
        Swap match = null;
        Model model;
        Book book;

        //first check if ask is not null, is valid and is also a model in this make
        if(validate(ask)) {
            model = ask.getModel();

            book = bookMap.get(model);
            if(book == null) {
                book = new Book<SwapMapper, Swap>();
                bookMap.put(model, book);
            }
            //if fill is true that means we want to actually edit the book and return a hard match if possible
            if(fill) {
                match = book.fillBook(bid, ask);
            }
            //else we just match and see if, at the current state of the book, we have a match
            else {
                match = book.match(bid, ask);
            }
        }

        return match;
    }

    /**
     * gets a list of all the swaps that someone will trade bid for.
     * @param bid
     *      the swap that you have
     * @return
     *      A list of swaps that currently want bid
     */
    public Set<Swap> getAsks(Swap bid) {
        if(validate(bid)) {
            Model model = bid.getModel();

            Book book = bookMap.get(model);
            if(book == null) {
                book = new Book<SwapMapper, Swap>();
                bookMap.put(model, book);
            }
            return book.getAsks(bid);
            }
        return null;
    }

    /**
     * gets a list of all the swaps that match bid that are in this book.
     * @param bid
     *      The bid to be matched
     * @return
     *      A list of actual bids matching that criteria
     */
    public Set<Swap> getBids(Swap bid) {
        Model model = bid.getModel();

        Book book = bookMap.get(model);
        if(book == null) {
            book = new Book<SwapMapper, Swap>();
            bookMap.put(model, book);
        }
        return book.getBids(bid);
    }

    /**
     * gets the current state of the book for all models
     * @return
     *      current state of the model managers books
     */
    public Map<Model, Book> getBookstore() {
        return new HashMap<Model, Book>(bookMap);
    }

    /**
     * flushes each models book so the book is completely empty
     * @return
     *         true if the full flush succeeded
     */
    public boolean flush() {
        Set<Model> models = bookMap.keySet();
        for(Model model : models) {
            Book book = bookMap.get(model);
            book.flush();
        }
        //don't really need this but may do a check later
        return true;
    }


    /**
     * Returns the make of this model manager
     * @return
     *      Make of this instance of model manager
     */
    public Make getMake() {
        return make;
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
        if(swap != null && swap.valid() && swap.getModel().getMake().equals(make)) {
            return true;
        }
        return false;
    }
}
