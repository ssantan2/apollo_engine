package apollo.common.shoe;

import org.apache.commons.lang.builder.HashCodeBuilder;

import apollo.common.templates.Swap;
import apollo.common.templates.SwapMapper;

/**
 * Mapper class for shoes. This class (Mapper) is used to make a simple key to the Shoe for comparison filtering.
 * Created by santana on 10/28/14.
 */
public class ShoeSwapMapper extends SwapMapper {


    /**
     * Creates wrapper around the Swap that is passed in
     * @param swap
     */
    public ShoeSwapMapper(Swap swap) {
        super(swap);
    }

    /**
     * overridden equals to compare two swaps via a wrapper method. This filters the compare further
     * @param e
     *
     *      shoe mapper to be compared
     * @return
     *      True if the two mappers equal each other
     */
    @Override
    public boolean equals(Object e) {
        if(swap != null && e instanceof ShoeSwapMapper) {
            ShoeSwapMapper swapMapper = (ShoeSwapMapper)e;
            ShoeSwap tmpSwap = (ShoeSwap)swapMapper.swap;
            if(tmpSwap.getClass().equals(ShoeSwap.class) &&
                    swap.getClass().equals(ShoeSwap.class)) {
                int tmpSize = tmpSwap.getSize();

                if(super.equals(swapMapper) && ((ShoeSwap)swap).getSize() == tmpSize) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * hashCode of shoe
     * @return
     *      hashcode
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).
                append(super.hashCode()).
                append(((ShoeSwap)swap).getSize()).
                toHashCode();
    }
}
