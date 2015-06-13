package apollo.common.templates;

import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Generic Model class
 * Created by santana on 7/27/14.
 */
public abstract class Model {
    //make of the model
    private Make make = null;
    //name of the model
    private String name = null;
    //yeah of the model
    protected int year = 1900;
    //condition of the model
    protected String condition = null;
    //description of the model
    protected String description = null;


    /**
     * Constructor that creates a Model with the passed in Make and name
     * @param make
     *      the Maker of the model
     * @param name
     *      the name of the model
     */
    public Model(Make make, String name) {
        this(make, name, 1900);
    }

    /**
     * Constructor that creates a Model with the passed in Make, name and year
     * @param make
     *      the Maker of the model
     * @param name
     *      the name of the model
     * @param year
     *      the year of the model
     */
    public Model(Make make, String name, int year) {
        try {
            if(checkMake(make)) {
                this.make = make;
                this.name = name;
                this.year = year;
            }
        }
        catch(Exception e) {
            System.out.println(e);

        }
    }

    /**
     * Equals method that compares the model passed in to this model and returns true if they match
     * @param model
     *      Model that this model is being compared to
     * @return
     */
    @Override
    public boolean equals(Object model) {
        if(model == null || !(model instanceof Model)) {
            return false;
        }

        String modelName = ((Model) model).getName();

        Make make = ((Model) model).getMake();

        if((getMake().equals(make)) && (getName().equals(modelName))) {
                return true;
        }

        return false;
    }

    /**
     * get the hashcode of the model
     * @return
     *      haschode of the model
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).
                append(name).
                append(make).
                toHashCode();
    }

    /**
     * Checks the make of the model and determines if its valid
     * @param make
     *      The make to be checked
     * @return
     *      true if the make is valid
     * @throws Exception
     *      Throws an exception if it is not a valid make
     */
    public abstract boolean checkMake(Make make) throws Exception;

    /**
     * gets the name of the Model
     * @return
     *      returns the name of the Model
     */
    public String getName() {
        return name;
    }

    /**
     * gets the Make of this Model
     * @return
     *      returns the Make of the Model
     */
    public Make getMake() {
        return make;
    }

    /**
     * gets the condition of this Model
     * @return
     *      returns the condition of the Model
     */
    public String getCondition() {
        return condition;
    }

    /**
     * gets the description of this Model
     * @return
     *      returns the description of the Model
     */
    public String getDescription() {
        return description;
    }

    /**
     * sets the description of the model
     * @param description
     *      the description of the model that should be set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Sets the year the model was made
     * @param year
     *      year the model was made
     */
    public void setYear(int year) {
        this.year = year;
    }

    /**
     * gets the year the model was made
     * @return
     *      year the model was made
     */
    public int getYear() {
        return year;
    }


}
