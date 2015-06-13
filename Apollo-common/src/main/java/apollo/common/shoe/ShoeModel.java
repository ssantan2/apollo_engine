package apollo.common.shoe;

import apollo.common.constant.ShoeConstants;
import apollo.common.exception.IllegalShoeException;
import apollo.common.templates.Make;
import apollo.common.templates.Model;

/**
 * Shoe Model object
 * Created by santana on 7/27/14.
 */
public class ShoeModel extends Model {

    /**
     * Shoe Model constructor that takes a Make and a model name
     * @param make
     *         Make of the shoe
     * @param name
     *         name of the model
     */
    public ShoeModel(Make make, String name) {
        super(make, name);
    }

    /**
     * Shoe Model constructor that takes the Make, name and year of the shoe
     * @param make
     *          Make of the shoe
     * @param name
     *          name of the model
     * @param year
     *          year of the model
     */
    public ShoeModel(Make make, String name, int year) {
        super(make, name, year);
    }

    /**
     * This method will need to be removed since we should not be checking against a static list
     * @param make
     *          Make to check against the "db"
     * @return
     * @throws Exception
     */

    public boolean checkMake(Make make) throws Exception {
        if(make == null) {
            throw new IllegalShoeException("Make is null");
        }
        else if(make.getName() == null || make.getName().isEmpty()) {
            throw new IllegalShoeException("Make name is empty");
        }
        else {
            return true;
        }
    }


    /**
     * This method sets whether or not the shoe is dead stock or not
     * @param deadStock
     *              True if dead stock, false otherwise.
     */
    public void isDeadStock(boolean deadStock) {
        if(deadStock) {
            condition = ShoeConstants.DEADSTOCK;
        }
        else {
            condition = ShoeConstants.VN_DEADSTOCK;
        }
    }


}
