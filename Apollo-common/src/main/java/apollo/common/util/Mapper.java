package apollo.common.util;

import apollo.common.shoe.ShoeSwap;
import apollo.common.templates.Swap;
import apollo.common.templates.SwapMapper;
import apollo.common.shoe.ShoeSwapMapper;

/**
 * Mapper class that creates mappers based on swaps that are passed in
 * Created by santana on 10/28/14.
 */
public class Mapper {

    /**
     * default constructor
     */
    public Mapper() {

    }

    /**
     * Mapper method that takes a swap and based on the class returns a Mapper class for it.
     * @param swap
     *      Swap to wrap in a mapper
     * @param <T>
     *      Mapper object that extends SwapMapper
     * @return
     */
    public static <T extends SwapMapper> T getMapper(Swap swap) {
        if(swap != null && swap.getClass().equals(ShoeSwap.class)) {
            return (T) new ShoeSwapMapper(swap);
        }

        return null;
    }
}
