package apollo.common.shoe;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertNull;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;

import apollo.common.constant.ShoeConstants;
import apollo.common.templates.Book;
import apollo.common.templates.Make;
import apollo.common.templates.Model;
import apollo.common.util.ShoeHelper;

/**
 * Test basic functionality of the book and apollo-commons
 * Created by santana on 8/1/14.
 */
public class ShoeBookTest {
    //book for the test
    public Book<ShoeSwapMapper, ShoeSwap> book = null;
    //user id of the test user
    public UUID userId = UUID.randomUUID();

    /**
     * Set up for the tests. this test fills the book with a bid/ask for size 10/12 sure for each model
     * that is in the ShoeHelper
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        //create new book
        book = new Book<ShoeSwapMapper, ShoeSwap>();
        //get a list of all Shoe Makers
        List<Make> makeList = ShoeHelper.getShoeMakers();
        for(Make make : makeList) {
            //get the list of all models associated with this make
            List<Model> models = ShoeHelper.getTestShoeModels(make);
            for(Model model : models) {
                //create bid for this model, size 10
                ShoeSwap bid = new ShoeSwap(model, 10, userId, true);
                //create ask for this model, size 12
                ShoeSwap ask = new ShoeSwap(model, 12, userId, true);

                //set pairs
                bid.setPair(ask);
                ask.setPair(bid);

                //enter the bid/ask into the book
                book.addToBook(bid, ask);
            }
        }
    }

    /**
     * Test the fill functionality
     * @throws Exception
     */
    @org.junit.Test
    public void testFill() throws Exception {
        Make make = new ShoeMake(ShoeConstants.NIKE);
        Model model = new ShoeModel(make, ShoeConstants.JORDAN);
        UUID uId = UUID.randomUUID();

        //create bid ask that will match what is in the book
        ShoeSwap bid = new ShoeSwap(model, 12, uId);
        ShoeSwap ask = new ShoeSwap(model, 10, uId);

        //set pairs
        bid.setPair(ask);
        ask.setPair(bid);

        ShoeSwap match = book.fillBook(bid, ask);

        //confirm they match
        assertNotNull(match);
        assertEquals(match.getModel(), model);
        assertEquals(match.getSize(), 10);
        assertEquals(match.getUserId(), userId);
    }

    /**
     * Test fill functionality that guarantees a null match
     * @throws Exception
     */
    @org.junit.Test
    public void testFillNull() throws Exception {
        Make make = new ShoeMake(ShoeConstants.NIKE);
        Model model = new ShoeModel(make, ShoeConstants.JORDAN);
        UUID uId = UUID.randomUUID();

        //create bid ask that will match what is in the book
        ShoeSwap bid = new ShoeSwap(model, 10, uId);
        ShoeSwap ask = new ShoeSwap(model, 12, uId);

        //set pairs
        bid.setPair(ask);
        ask.setPair(bid);

        ShoeSwap match = book.fillBook(bid, ask);
        assertNull(match);
    }

    /**
     * test the grab functionality
     * @throws Exception
     */
    @org.junit.Test
    public void testGrab() throws Exception {
        Make make = new ShoeMake(ShoeConstants.NIKE);
        Model model = new ShoeModel(make, ShoeConstants.JORDAN);
        UUID uId = UUID.randomUUID();

        //create bid ask that will match what is in the book
        ShoeSwap ask = new ShoeSwap(model, 10, uId);

        //confirm it returned back a correct object
        ShoeSwap match = book.grab(ask);
        assertNotNull(match);
        assertEquals(match.getModel(), model);
        assertEquals(match.getSize(), 10);
        //user id is the user id of the test class
        assertEquals(match.getUserId(), userId);
        assertTrue(match.isForSale());

    }

    /**
     * test grab functionality that guarantees a null return
     * @throws Exception
     */
    @org.junit.Test
    public void testGrabNull() throws Exception {
        Make make = new ShoeMake(ShoeConstants.NIKE);
        Model model = new ShoeModel(make, ShoeConstants.JORDAN);
        UUID uId = UUID.randomUUID();

        //create bid ask that will match what is in the book
        ShoeSwap ask = new ShoeSwap(model, 12, uId);

        ShoeSwap match = book.grab(ask);
        assertNull(match);
    }

    /**
     * tests the get all asks functionality for s specific bid
     * @throws Exception
     */
    @org.junit.Test
    public void testGetAsks() throws Exception {
        Make make = new ShoeMake(ShoeConstants.NIKE);
        Model model = new ShoeModel(make, ShoeConstants.JORDAN);
        UUID uId = UUID.randomUUID();
        UUID uId2 = UUID.randomUUID();

        //create an extra entry in the book for that bid/ask combo
        ShoeSwap bid = new ShoeSwap(model, 10, uId2);
        ShoeSwap ask = new ShoeSwap(model, 12, uId2);

        //set pairs
        bid.setPair(ask);
        ask.setPair(bid);

        book.addToBook(bid, ask);

        //create bid ask that will match what is in the book
        bid = new ShoeSwap(model, 10, uId);

        Set<ShoeSwap> matches = book.getAsks(bid);

        assertNotNull(matches);
        //two entries should be returned. one for the set up and one for above
        assertEquals(matches.size(), 2);

        Iterator<ShoeSwap> matchIterator = matches.iterator();

        ShoeSwap match = matchIterator.next();

        assertNotNull(match);
        assertEquals(match.getModel(), model);
        assertEquals(match.getSize(), 12);
        assertEquals(match.getUserId(), userId);

        match = matchIterator.next();

        assertNotNull(match);
        assertEquals(match.getModel(), model);
        assertEquals(match.getSize(), 12);
        assertEquals(match.getUserId(), uId2);
    }

    /**
     * tests the get all bids functionality for s specific bid
     * @throws Exception
     */
    @org.junit.Test
    public void testGetBids() throws Exception {
        Make make = new ShoeMake(ShoeConstants.NIKE);
        Model model = new ShoeModel(make, ShoeConstants.JORDAN);
        UUID uId = UUID.randomUUID();

        //create an extra entry in the book for that bid/ask combo
        ShoeSwap bid = new ShoeSwap(model, 10, uId);
        ShoeSwap ask = new ShoeSwap(model, 12, uId);

        //set pairs
        bid.setPair(ask);
        ask.setPair(bid);

        book.addToBook(bid, ask);

        Set<ShoeSwap> matches = book.getBids(ask);

        assertNotNull(matches);
        //should get two back, one for the setup and one for above
        assertEquals(matches.size(), 2);

        Iterator<ShoeSwap> matchIterator = matches.iterator();

        ShoeSwap match = matchIterator.next();

        assertNotNull(match);
        assertEquals(match.getModel(), model);
        assertEquals(match.getSize(), 10);
        assertEquals(match.getUserId(), userId);

        match = matchIterator.next();

        assertNotNull(match);
        assertEquals(match.getModel(), model);
        assertEquals(match.getSize(), 10);
        assertEquals(match.getUserId(), uId);
    }

    /**
     * test get asks that should return a null.
     * @throws Exception
     */
    @org.junit.Test
    public void testGetAsksNull() throws Exception {
        Make make = new ShoeMake(ShoeConstants.NIKE);
        Model model = new ShoeModel(make, ShoeConstants.JORDAN);
        UUID uId = UUID.randomUUID();

        //create bid ask that will match what is in the book
        ShoeSwap bid = new ShoeSwap(model, 12, uId);

        Set<ShoeSwap> matches = book.getAsks(bid);

        assertTrue(matches.isEmpty());
    }

    /**
     * test match functionality
     * @throws Exception
     */
    @org.junit.Test
    public void testMatch() throws Exception {
        Make make = new ShoeMake(ShoeConstants.NIKE);
        Model model = new ShoeModel(make, ShoeConstants.JORDAN);
        UUID uId = UUID.randomUUID();

        //create bid ask that will match what is in the book
        ShoeSwap bid = new ShoeSwap(model, 12, uId);
        ShoeSwap ask = new ShoeSwap(model, 10, uId);

        //set pairs
        bid.setPair(ask);
        ask.setPair(bid);

        ShoeSwap match = book.match(bid, ask);

        //confirm they match
        assertNotNull(match);
        assertEquals(match.getModel(), model);
        assertEquals(match.getSize(), 10);
        assertEquals(match.getUserId(), userId);
    }

    /**
     * Test match functionality where the match is false;
     * @throws Exception
     */
    @org.junit.Test
    public void testMatchFalse() throws Exception {
        Make make = new ShoeMake(ShoeConstants.NIKE);
        Model model = new ShoeModel(make, ShoeConstants.JORDAN);
        UUID uId = UUID.randomUUID();

        //create bid ask that will match what is in the book
        ShoeSwap bid = new ShoeSwap(model, 10, uId);
        ShoeSwap ask = new ShoeSwap(model, 12, uId);

        //set pairs
        bid.setPair(ask);
        ask.setPair(bid);

        assertNull(book.match(bid, ask));
    }

}
