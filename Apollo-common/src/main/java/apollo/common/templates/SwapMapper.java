package apollo.common.templates;

import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Created by santana on 10/28/14.
 */
public class SwapMapper {
    protected Swap swap;

    public SwapMapper(Swap swap) {
        this.swap = swap;
    }

    @Override
    public boolean equals(Object e) {
        if(swap != null && e instanceof SwapMapper) {
            SwapMapper swapMapper = (SwapMapper)e;
            Swap tmpSwap = swapMapper.swap;
                if(tmpSwap != null) {
                Model tmpModel = tmpSwap.getModel();

                if(tmpModel.equals(swap.getModel())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).
                append(swap.getModel()).
                toHashCode();
    }

}
