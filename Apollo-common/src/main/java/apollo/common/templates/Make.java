package apollo.common.templates;

import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Generic Make class
 * Created by santana on 7/27/14.
 */
public abstract class Make {
    //name of the make
    private String name = null;

    /**
     * Constructor that takes in name of Make object being created
     * @param name
     *          Name of the object being created
     */
    public Make(String name) {
        this.name = name;
    }


    /**
     * Overridden equals to compare makes by name
     * @param make
     *          Make object to be compared
     * @return
     *         True - if these Make objects are the same
     */
   @Override
   public boolean equals(Object make) {
       if(make == null || !(make instanceof Make)) {
           return false;
       }

       String name = ((Make) make).getName();
       if(getName().equals(name)) {
           return true;
       }

       return false;
   }

    /**
     * hashcode for make object
     * @return
     *      hashcode
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).
                append(name).
                toHashCode();
    }

    /**
     * get name for make object
     * @return
     *        Make's name
     */
    public String getName() {
        return name;
    }
}
