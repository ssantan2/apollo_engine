package apollo.common.templates;

import java.util.UUID;

import org.apache.commons.lang.builder.HashCodeBuilder;

import apollo.common.util.Mapper;

/**
 * Class which is the base model of a Swappable item that can be managed by the book
 * Created by santana on 7/27/14.
 */
public class Swap {

    //model
    protected Model model = null;
    //user id of owner
    protected UUID userId = null;
    //flag to determine if this swap was matched already or not
    private boolean matched = false;
    //id of the bid that this was matched with
    private UUID matchId = null;
    //this swap is eligible to be bought outright
    private boolean forSale = false;
    //reference of complimentary ask/bid if it exists
    private Swap pair = null;



    /**
     * Constructor that takes a model and a user ID
     * @param model
     *      Model of the swap
     * @param userId
     *      user id of owner of the swap
     */
    public Swap(Model model, UUID userId) {
        this(model, userId, null);
    }

    /**
     * Constructor that takes a model, user ID and a pair ID
     * @param model
     *      Model of the swap
     * @param userId
     *      user id of owner of the swap
     * @param pair
     *      the complimentary swap
     */
    public Swap(Model model, UUID userId, Swap pair) {
        this(model, userId, pair, false);
    }

    /**
     * Constructor that takes a Model, user ID, pair ID and if this swap is for sale
     * @param model
     *      Model of the swap
     * @param userId
     *      user id of owner of the swap
     * @param pair
     *      the complimentary swap
     * @param forSale
     *      True if this swap is for sale. False if it is trade only
     */
    public Swap(Model model, UUID userId, Swap pair, boolean forSale) {
        this.model = model;
        this.userId = userId;
        this.pair = pair;
        this.forSale = forSale;
    }

    /**
     * compares the swap object passed in with this object
     * @param e
     *      swap object to be compared to
     * @return
     *      True if the objects are equal
     */
    @Override
    public boolean equals(Object e) {
        if(e instanceof Swap) {
            Swap swap = (Swap)e;
            Model tmpModel = swap.getModel();

            if(tmpModel.equals(model) && userId.equals(swap.getUserId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * hashCode of swap object
     * @return
     *      integer hashCode of swap object
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).
                append(model).
                toHashCode();
    }

    /**
     * Compares the swap object passed in with this object. If the objects match and neither object has
     * been matched previously, then a new matchID is made and returned
     * @param swap
     *      Swap object that is being matched
     * @return
     *      UUID of the match
     */
    public UUID match(Swap swap) {
        //if swap is not null, swap is not matched and swap either doesn't have a pairID or its pairID doesn't match this swap, it is valid
        if(swap != null && (swap.getPair() == null || !(swap.getPair().equals(pair))) && !matched) {
            if(compare(swap)) {
                return UUID.randomUUID();
            }
        }
        return null;
    }


    /**
     * Checks whether this swap is valid or not. Validity is determined by basic parameters.
     * the model is not null. the user id is positive and this hasn't been matched before
     * @return
     *      True if all three conditions are true
     */
    public boolean valid() {
        if(model != null && userId != null && !matched) {
            return true;
        }
        return false;
    }

    /**
     * Does a filtered compare of the swap and this object. The filter is handled by the Mapper class
     * @param compareSwap
     *          the swap to be compared
     * @return
     *          True if they are the same based on the mapper
     */
    protected boolean compare(Swap compareSwap) {
        SwapMapper mapper = Mapper.getMapper(this);
        SwapMapper inboundMapper = Mapper.getMapper(compareSwap);

        return mapper.equals(inboundMapper);
    }

    /**
     * returns the model of this swap
     * @return
     *      the Model of this swap
     */
    public Model getModel() {
        return model;
    }

    /**
     * returns the user id of this swap
     * @return
     *      the user id of this swap
     */
    public UUID getUserId() {
        return userId;
    }

    /**
     * returns whether this swap has been matched yet
     * @return
     *      true if the swap has been matched
     */
    public boolean isMatched() {
        return matched;
    }

    /**
     * gets the pair of this swap
     * @return
     *      the pair of this swap
     */
    public Swap getPair() {
        return pair;
    }

    /**
     * gets the match id of this swap
     * @return
     *      the match id of this swap
     */
    public UUID getMatchId() {
        return matchId;
    }

    /**
     * sets the match ID of this swap. This can only be set one time
     * @param matchId
     *      the match id to set this swap with
     */
    public boolean setMatchId(UUID matchId) {
        if(matchId != null && !matched) {
            this.matchId = matchId;
            matched = true;
            if(pair != null) {
                pair.setMatchId(matchId);
            }
            return true;
        }
        else if(this.matchId != null && !this.matchId.equals(matchId)) {
            System.out.println("WARNING: Match ID " + this.matchId + " already set. ID " + matchId + " will be discarded" );
        }
        return false;
    }

    /**
     * sets the pair for this swap. This can only be set one time
     * @param pair
     *      The pair to set this swap with
     */
    public boolean setPair(Swap pair) {
        if(pair != null && this.pair == null) {
            this.pair = pair;
            return true;
        }
        System.out.println("WARNING: Pair already set, passed in pair will get discarded" );
        return false;
    }

    /**
     * Method to set a swap that was originally not for sale as now for sale
     */
    public void forSale() {
        forSale = true;
    }

    /**
     * Is this swap for sale or is it trade only
     * @return
     *          True - this swap is eligible for sale. False - trade only
     */
    public boolean isForSale() {
        return forSale;
    }
}
