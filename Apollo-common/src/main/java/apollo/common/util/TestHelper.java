package apollo.common.util;

import java.util.UUID;

import apollo.common.constant.ShoeConstants;
import apollo.common.shoe.ShoeMake;
import apollo.common.shoe.ShoeModel;
import apollo.common.shoe.ShoeSwap;
import apollo.common.templates.Make;
import apollo.common.templates.Model;

/**
 * Created by santana on 11/15/14.
 */
public class TestHelper {

    public static ShoeSwap getNullModel(int size, UUID uId) {
        Model model = null;

        ShoeSwap swap = new ShoeSwap(model, size, uId);
        return swap;
    }

    public static ShoeSwap getNullMake(int size, UUID uId) {
        Make make = null;
        Model model = new ShoeModel(make, ShoeConstants.JORDAN);

        ShoeSwap swap = new ShoeSwap(model, size, uId);
        return swap;
    }

    public static ShoeSwap getJordan(int size, UUID uId, boolean sell) {
        Make make = new ShoeMake(ShoeConstants.NIKE);
        Model model = new ShoeModel(make, ShoeConstants.JORDAN);

        ShoeSwap swap = new ShoeSwap(model, size, uId, sell);
        return swap;
    }

    public static ShoeSwap getAirMax(int size, UUID uId, boolean sell) {
        Make make = new ShoeMake(ShoeConstants.NIKE);
        Model model = new ShoeModel(make, ShoeConstants.AIRMAX);

        ShoeSwap swap = new ShoeSwap(model, size, uId);
        return swap;
    }

    public static ShoeSwap getAirforceOne(int size, UUID uId, boolean sell) {
        Make make = new ShoeMake(ShoeConstants.NIKE);
        Model model = new ShoeModel(make, ShoeConstants.AIRFORCE_ONE);

        ShoeSwap swap = new ShoeSwap(model, size, uId);
        return swap;
    }



    public static ShoeSwap getRose(int size, UUID uId, boolean sell) {
        Make make = new ShoeMake(ShoeConstants.ADIDAS);
        Model model = new ShoeModel(make, ShoeConstants.ROSE3);

        ShoeSwap swap = new ShoeSwap(model, size, uId);
        return swap;
    }

    public static ShoeSwap getAnswerV(int size, UUID uId, boolean sell) {
        Make make = new ShoeMake(ShoeConstants.REEBOK);
        Model model = new ShoeModel(make, ShoeConstants.ANSWER_V);

        ShoeSwap swap = new ShoeSwap(model, size, uId);
        return swap;
    }

}
