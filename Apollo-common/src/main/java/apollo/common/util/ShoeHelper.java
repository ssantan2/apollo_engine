package apollo.common.util;

import apollo.common.constant.ShoeConstants;
import apollo.common.shoe.ShoeMake;
import apollo.common.shoe.ShoeModel;
import apollo.common.templates.Make;
import apollo.common.templates.Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that helps internal tests by setting up Makes and Models
 * Created by santana on 8/1/14.
 */
public class ShoeHelper {

    /**
     * list of Makers as a string array
     * @return
     *      string array of Make names
     */
    public static String[] getShoeMakersAsStrings() {
        return new String[] {
                ShoeConstants.ADIDAS,
                ShoeConstants.NIKE,
                ShoeConstants.PUMA,
                ShoeConstants.REEBOK,
        };
    }

    /**
     * get a list of shoe makers based off of the string array of shoe Makers
     * @return
     *      list of Makers
     */
    public static List<Make> getShoeMakers () {
        List<Make> makers = new ArrayList<Make>();

        for(String maker : getShoeMakersAsStrings()) {
            ShoeMake make = new ShoeMake(maker);
            makers.add(make);
        }

        return makers;
    }

    /**
     * method that gets a list of models based on the Make passed in
     * @param make
     *          the make that the models are from
     * @return
     *          the list of models that are from that maker
     */
    public static List<Model> getTestShoeModels(Make make) {
        List<Model> models = new ArrayList<Model>();
        String name = make.getName();

        if(name.equals(ShoeConstants.ADIDAS)) {
            models.add(new ShoeModel(make, ShoeConstants.ROSE3));
            models.add(new ShoeModel(make, ShoeConstants.SUPERSTAR));
            models.add(new ShoeModel(make, ShoeConstants.OREGON));
        }
        else if(name.equals(ShoeConstants.REEBOK)) {
            models.add(new ShoeModel(make, ShoeConstants.QUESTION));
            models.add(new ShoeModel(make, ShoeConstants.ANSWER_V));
            models.add(new ShoeModel(make, ShoeConstants.KAMIKAZE));
        }
        else if(name.equals(ShoeConstants.PUMA)) {
            models.add(new ShoeModel(make, ShoeConstants.DRIFT_CAT));
            models.add(new ShoeModel(make, ShoeConstants.ROMA));
            models.add(new ShoeModel(make, ShoeConstants.ULEMBA));
        }
        else if(name.equals(ShoeConstants.NIKE)) {
            models.add(new ShoeModel(make, ShoeConstants.AIRFORCE_ONE));
            models.add(new ShoeModel(make, ShoeConstants.AIRMAX));
            models.add(new ShoeModel(make, ShoeConstants.JORDAN));
        }

        return models;
    }
}
