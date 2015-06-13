package apollo.common.shoe;

import java.util.UUID;

import org.apache.commons.lang.builder.HashCodeBuilder;

import apollo.common.constant.ShoeConstants;
import apollo.common.templates.Model;
import apollo.common.templates.Swap;
import apollo.common.util.Mapper;

/**
 * Swap object for Shoes. This is the Class that the book manages
 * Created by santana on 7/27/14.
 */
public class ShoeSwap extends Swap {
	
	//root size of shoe
	private int size = 0;
	
    //sizes of the shoe
    //private int[] sizes = new int[]{0};
    //size range. this will be used to check various sizes for comparison
    //value of the shoe
    private double worth = 0;

    /**
     * Shoe constructor that takes a model and a shoe size
     * @param model
     *          Model of the shoe
     * @param size
     *          Size of the shoe
     */
    public ShoeSwap(Model model, int size) {
        this(model, size, null, null);
    }

    /**
     * Shoe constructor that takes a mode, a shoe size and a user ID.
     * @param model
     *          Model of the shoe
     * @param size
     *          size of the shoe
     * @param userId
     *          userId who owns the shoe.
     */
    public ShoeSwap(Model model, int size, UUID userId) {
        this(model, size, userId, null);
    }

    /**
     * Shoe constructor that takes a mode, a shoe size, user ID and a for sale option.
     * @param model
     *          Model of the shoe
     * @param size
     *          size of the shoe
     * @param userId
     *          userId who owns the shoe.
     */
    public ShoeSwap(Model model, int size, UUID userId, boolean forSale) {
        this(model, size, userId, null, forSale);
    }

    /**
     * Shoe constructor is used by all other constructors. It takes
     * a model, Shoe size, userId, and a pairId
     * @param model
     *          Model of the shoe
     * @param size
     *          Size of the shoe
     * @param userId
     *          User ID who owns the shoe
     * @param pair
     *          the pair this shoe goes with (matching pair, the ask to this bid).
     */
    public ShoeSwap(Model model, int size, UUID userId, Swap pair) {
        super(model,userId, pair);
        if(size > ShoeConstants.MAX_SHOE_SIZE) {
        	this.size = ShoeConstants.MAX_SHOE_SIZE;
        }
        else if(size < ShoeConstants.MIN_SHOE_SIZE) {
        	this.size = ShoeConstants.MIN_SHOE_SIZE;
        }
        else {
        	this.size = size;
        }
        setRange(0);
    }

    /**
     * Shoe constructor is used by all other constructors. It takes
     * a model, Shoe size, userId, and a pairId
     * @param model
     *          Model of the shoe
     * @param size
     *          Size of the shoe
     * @param userId
     *          User ID who owns the shoe
     * @param pair
     *          the pair this shoe goes with (matching pair, the ask to this bid).
     * @param forSale
     *          True if this shoe is for sale, false if it is trade only
     */
    public ShoeSwap(Model model, int size, UUID userId, Swap pair, boolean forSale) {
        super(model,userId, pair, forSale);
        this.size = size;
        setRange(0);        		
    }

    /**
     * Overridden equals that compares two shoes. it will match if the model and size and userID is the same
     * @param e
     *          Shoe swap to compare this to
     * @return
     *          True is the swap is the same
     */
    @Override
    public boolean equals(Object e) {
        if(e instanceof ShoeSwap) {
            ShoeSwap swap = (ShoeSwap)e;
            int tmpSize = swap.getSize();

            if(super.equals(swap) && size == tmpSize) {
                return true;
            }
        }
        return false;
    }

    /**
     * hashCode for this object
     * @return
     *      integer hashcode for this object
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).
                append(super.hashCode()).
                append(size).
                toHashCode();
    }

    /**
     * validity check for this Shoe. If the swap is valid and the size is valid it returns true.
     * @return
     *          True is the swap is valid and the size is valid
     */
    public boolean valid() {
        if(super.valid() && size >= ShoeConstants.MIN_SHOE_SIZE && size <= ShoeConstants.MAX_SHOE_SIZE) {
            return true;
        }

        return false;
    }

    /**
     * Compares another shoe but at a simpler level
     * @param compareSwap
     *          The shoe you are being compared to.
     * @return
     *          True if the shoe passed in matches this shoe.
     */
    @Override
    protected boolean compare(Swap compareSwap) {
        ShoeSwapMapper mapper = Mapper.getMapper(this);
        ShoeSwapMapper inboundMapper = Mapper.getMapper(compareSwap);

        return mapper.equals(inboundMapper);
    }

    /**
     * gets the model of this shoe
     * @return
     *      model of this shoe
     */
    public ShoeModel getModel() {
        return (ShoeModel)model;
    }

    /**
     * Gets the sizes of this shoe
     * @return
     *      an array of sizes of this shoe. For bids, length should always be one
     */
    public int getSize() {
        return size;
    }


    /**
     * the value of this shoe
     * @return
     *      The cash value of this shoe
     */
    public double getWorth() {
        return worth;
    }

    /**
     * sets the value of this shoe in cash
     * @param worth
     *          cash value
     */
    public void setWorth(double worth) {
        this.worth = worth;
    }
    
    /**
     * sets the range of sizes from the initial size that we are willing to match on where the initial size is the middle.
     *  all bids should have a range of zero. there is a range limit of max shoe size / 2
     *  @param range
     *  		number of sizes away from the root size
     */
    public void setRange(int range) {
   
    	
    }

}
