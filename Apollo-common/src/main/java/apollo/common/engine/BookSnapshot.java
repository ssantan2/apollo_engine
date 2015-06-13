package apollo.common.engine;

import apollo.common.templates.Book;
import apollo.common.templates.Make;
import apollo.common.templates.Model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class creates a book snapshot of the engine for checkpointing as well as state recovery
 * Created by santana on 10/26/14.
 */
public class BookSnapshot {
    //book store data structure that holds all the information
    private Map<Make, Map<Model, Book>> bookstore;
    //set of models in the book snapshot
    private Set<Model> models;
    //boolean value to determine if the snapshot is empty
    private boolean isEmpty = true;
    //timestamp when this was created
    private long createTime = 0;

    /**
     * public constructor that creates a new bookstore, model set and sets the create timestamp
     */
    public BookSnapshot() {
        bookstore = new HashMap<Make, Map<Model, Book>>();
        models = new HashSet<Model>();
        createTime = System.currentTimeMillis();
    }


    /**
     * adds the Map of books for each model in the snapshot object
     * @param modelMap
     *          Map of models to book instances
     */
    public void add(Map<Model, Book> modelMap) {
        if(modelMap != null) {
            //gets the set of all models in the map
            Set<Model> modelSet = modelMap.keySet();
            Set<Model> internalModelSet = new HashSet<Model>();

            //loops through each model
            for(Model model : modelSet) {
                //gets the make of the model and then the map associated with that make
                Make make = model.getMake();
                Map<Model, Book> tempModelMap = bookstore.get(make);

                //checks if the modelMap has already been created for this make
                if(tempModelMap == null) {
                    tempModelMap = new HashMap<Model, Book>();
                    bookstore.put(make, tempModelMap);
                }

                //checks to see if the book is empty or not and adds it if it is
                Book book = modelMap.get(model);
                if(book != null && !book.isEmpty()) {
                    tempModelMap.put(model, book);
                    internalModelSet.add(model);
                    isEmpty = false;
                }
            }

            //adds all models that have an active book to this map.
            models.addAll(internalModelSet);

        }
    }

    /**
     * adds a model and book to the snapshot
     * @param model
     *        The model that matches the book
     * @param book
     *        The book to be saved.
     */
    public void add(Model model, Book book) {
        Map<Model, Book> modelMap = new HashMap<Model, Book>();
        modelMap.put(model, book);
        add(modelMap);
    }

    /**
     * returns the Map of models and book for the given make
     * @param make
     *      The make that you are looking for
     * @return
     *      the Map that contains the book by model for that make
     */
    public Map<Model, Book> getBooksForMake(Make make) {
        return bookstore.get(make);
    }

    /**
     * Gets the book for the model that is given
     * @param model
     *          The model of the book to be returned
     * @return
     *          The book of the model passed in
     */
    public Book getBookForModel(Model model) {
        if(model != null) {
            Make make = model.getMake();
            Map<Model, Book> modelMap = bookstore.get(make);
            if(modelMap != null) {
                return modelMap.get(model);
            }
        }
        return null;
    }

    /**
     * A set of all the makers in the snapshot
     * @return
     *      Set of makers
     */
    public Set<Make> getMakers() {
        return bookstore.keySet();
    }

    /**
     * set of all the models in the snapshot
     * @return
     *      set of all the models.
     */
    public Set<Model> getModels() {
        return new HashSet<Model>(models);
    }

    /**
     * whether or not the book snapshot has any books in it
     * @return
     *      returns true if there is an active book. False othewise
     */
    public boolean isEmpty() {
        return isEmpty;
    }

    /**
     * timestamp when this snapshot was created
     * @return
     */
    public long getTimestamp() {
        return createTime;
    }
}
