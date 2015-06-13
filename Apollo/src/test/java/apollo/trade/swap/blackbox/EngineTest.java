package apollo.trade.swap.blackbox;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.TestCase.assertNotSame;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import apollo.common.engine.Bill;
import apollo.common.engine.BookSnapshot;
import apollo.common.engine.Message;
import apollo.common.engine.Statistics;
import apollo.common.shoe.ShoeSwap;
import apollo.common.templates.Book;
import apollo.common.templates.Make;
import apollo.common.templates.Model;
import apollo.common.templates.Swap;
import apollo.common.util.ApolloHelper;
import apollo.common.util.ShoeHelper;
import apollo.common.util.TestHelper;
import apollo.trade.swap.service.Engine;

/**
 * Engine test class: This test all basic engine functionality from completing bids across makes to null inputs
 * Created by santana on 8/2/14.
 */
public class EngineTest {
    //test user id
    private UUID userId = null;
    //bid map needed for comparison
    private Map<Model, Set<Swap>> bidMap = null;

    //main test shoe size
    private final int testBidSize = 10;
    private final int testAskSize = 12;

    /**
     * pre-loads a list of levels in the book
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        //flushes the engine
        Engine.start();

        userId = UUID.randomUUID();
        bidMap = new HashMap<Model, Set<Swap>>();

        //gets list of shoe makers
        List<Make> makeList = ShoeHelper.getShoeMakers();
        for(Make make : makeList) {
            //gets list of shoe models
            List<Model> models = ShoeHelper.getTestShoeModels(make);
            for(Model model : models) {
                //creates bid and ask that is for sale as well
                ShoeSwap bid = new ShoeSwap(model, testBidSize, userId, true);
                ShoeSwap ask = new ShoeSwap(model, testAskSize, userId, true);
                //get SWAP engine
                Message message = Message.getMessage(bid, ask);

                //send to engine
                UUID id =  Engine.send(message);
                Engine.getBill(id);

                Set<Swap> bids = bidMap.get(model);

                if(bids == null) {
                    bids = new HashSet<Swap>();
                    bidMap.put(model, bids);
                }

                bids.add(bid);
            }
        }

        /* Make sure the book snapshot is in the right state for the tests by running book snapshot test  */
        testBookSnapshot();
    }

    @After
    public void tearDown() throws Exception {
        Engine.flush();
    }

    /**
     * Test: all null related input into the engine
     * @throws  Exception
     */
    @Test
    public void testEngineInput() throws Exception {
        //write test names so we know which test had which output
        String functionName = "null input";
        System.out.println("Starting test " + functionName);

        UUID uId = UUID.randomUUID();

        //create bid ask that will match what is in the book
        ShoeSwap bid = TestHelper.getJordan(testAskSize, uId, false);
        ShoeSwap ask = TestHelper.getJordan(testBidSize, uId, false);

        //sending null engine
        Engine.send(null);

        //testing null kill_core engine
        Message message = Message.getMessage((UUID) null);
        assertEquals(message.getType(), -1);
        assertEquals(message.getAsk(), null);
        assertEquals(message.getBid(), null);
        assertEquals(message.getGoal(), null);
        assertEquals(message.getMaxBillLength(), ApolloHelper.TTL);
        UUID id = Engine.send(message);

        assertNull(id);

        //testing null grab engine
        message = Message.getMessage((Swap) null);
        assertEquals(message.getType(), -1);
        assertEquals(message.getAsk(), null);
        assertEquals(message.getBid(), null);
        assertEquals(message.getGoal(), null);
        assertEquals(message.getMaxBillLength(), ApolloHelper.TTL);
        id = Engine.send(message);

        assertNull(id);

        //testing double null SWAP engine
        message = Message.getMessage(null, null);
        assertEquals(message.getType(), -1);
        assertEquals(message.getAsk(), null);
        assertEquals(message.getBid(), null);
        assertEquals(message.getGoal(), null);
        assertEquals(message.getMaxBillLength(), ApolloHelper.TTL);
        id = Engine.send(message);

        assertNull(id);

        //testing null bid SWAP engine
        message = Message.getMessage(null, ask);
        assertEquals(message.getType(), -1);
        assertEquals(message.getAsk(), null);
        assertEquals(message.getBid(), null);
        assertEquals(message.getGoal(), null);
        assertEquals(message.getMaxBillLength(), ApolloHelper.TTL);
        id = Engine.send(message);

        assertNull(id);

        //testing null ask SWAP engine where the bid is not for sale
        message = Message.getMessage(bid, null);
        assertEquals(message.getType(), -1);
        assertEquals(message.getAsk(), null);
        assertEquals(message.getBid(), null);
        assertEquals(message.getGoal(), null);
        assertEquals(message.getMaxBillLength(), ApolloHelper.TTL);
        id = Engine.send(message);

        assertNull(id);

        //testing double null GOAL engine
        message = Message.getMessage(null, null, 4);
        assertEquals(message.getType(), -1);
        assertEquals(message.getAsk(), null);
        assertEquals(message.getBid(), null);
        assertEquals(message.getGoal(), null);
        assertEquals(message.getMaxBillLength(), ApolloHelper.TTL);
        id = Engine.send(message);

        assertNull(id);

        //testing null bid GOAL engine
        message = Message.getMessage(null, ask, -1);
        assertEquals(message.getType(), -1);
        assertEquals(message.getAsk(), null);
        assertEquals(message.getBid(), null);
        assertEquals(message.getGoal(), null);
        assertEquals(message.getMaxBillLength(), ApolloHelper.TTL);
        id = Engine.send(message);

        assertNull(id);

        //testing null ask GOAL engine
        message = Message.getMessage(bid, null, 3);
        assertEquals(message.getType(), -1);
        assertEquals(message.getAsk(), null);
        assertEquals(message.getBid(), null);
        assertEquals(message.getGoal(), null);
        assertEquals(message.getMaxBillLength(), ApolloHelper.TTL);
        id = Engine.send(message);

        assertNull(id);
    }

    /**
     * Test: tries to fill an order with a series of bad messages
     * @throws Exception
     */
    @Test
    public void testEngineFillNull1() throws Exception {
        //write test names so we know which test had which output
        String functionName = "null fill 1";
        System.out.println("Starting test: " + functionName);

        UUID uId = UUID.randomUUID();

        //create ask with null model
        ShoeSwap bid = TestHelper.getJordan(testAskSize, uId, false);
        ShoeSwap ask = TestHelper.getNullModel(testBidSize, uId);

        Message message = Message.getMessage(bid,ask);

        UUID id = Engine.send(message);

        assertNull(id);
    }

    @Test
    public void testEngineFillNull2() throws Exception {
        //write test names so we know which test had which output
        String functionName = "null fill 2";
        System.out.println("Starting test: " + functionName);

        UUID uId = UUID.randomUUID();

        //create bid with null model
        ShoeSwap bid = TestHelper.getNullModel(testAskSize, uId);
        ShoeSwap ask = TestHelper.getJordan(testBidSize, uId, false);

        Message message = Message.getMessage(bid,ask);

        UUID id = Engine.send(message);

        assertNull(id);
    }

    @Test
    public void testEngineFillNull3() throws Exception {
        //write test names so we know which test had which output
        String functionName = "null fill 3";
        System.out.println("Starting test: " + functionName);

        UUID uId = UUID.randomUUID();

        //double null model
        ShoeSwap bid = TestHelper.getNullModel(testAskSize, uId);
        ShoeSwap ask = TestHelper.getNullModel(testBidSize, uId);

        Message message = Message.getMessage(bid,ask);

        UUID id = Engine.send(message);

        assertNull(id);
    }

    /**
     * Test: user enters shoe swap that matches and a shoe is returned
     *
     * @throws Exception
     */
    @org.junit.Test
    public void testEngineFill() throws Exception {
        //write test names so we know which test had which output
        String functionName = "engine fill";
        System.out.println("Starting test: " + functionName);

        UUID uId = UUID.randomUUID();

        //create bid ask that will match what is in the book
        ShoeSwap bid = TestHelper.getJordan(testAskSize, uId, false);
        ShoeSwap ask = TestHelper.getJordan(testBidSize, uId, false);

        Message message = Message.getMessage(bid,ask);

        UUID id = Engine.send(message);

        Bill bill = Engine.getBill(id);

        assertNotNull(bill);
        assertEquals(bill.size(), 2);

        //my shoe I placed as a bid
        ShoeSwap myBid = (ShoeSwap)bill.get(0);
        assertNotNull(myBid);
        assertEquals(myBid.getModel(), bid.getModel());
        assertEquals(myBid.getSize(), bid.getSize());
        assertEquals(myBid.getUserId(), bid.getUserId());
        assertTrue(myBid.isMatched());

        UUID matchId = myBid.getMatchId();
        Swap pair = myBid.getPair();

        //the user of the shoe who matches the one i want
        ShoeSwap match = (ShoeSwap)bill.get(1);
        assertNotNull(match);
        assertEquals(match.getModel(), ask.getModel());
        assertEquals(match.getSize(), ask.getSize());
        assertEquals(match.getUserId(), userId);
        assertTrue(match.isMatched());
        assertEquals(matchId, match.getMatchId());
        assertNotSame(pair, match.getPair());

        Statistics stats = bill.getStatistics();
        System.out.println(stats.toString());
    }

    /**
     * Test: user enters shoe swap that matches across models and a shoe is returned
     *
     * @throws Exception
     */
    @org.junit.Test
    public void testEngineFillCross1() throws Exception {
        //write test names so we know which test had which output
        String functionName = "engine fill cross 1";
        System.out.println("Starting test: " + functionName);

        UUID uId1 = UUID.randomUUID();
        UUID uId2 = UUID.randomUUID();

        //create bid ask to put in the book that is separate from the others
        ShoeSwap bid = TestHelper.getAirMax(13, uId1, false);
        ShoeSwap ask = TestHelper.getJordan(13, uId1, false);

        Message message = Message.getMessage(bid,ask);

        UUID id = Engine.send(message);

        Bill bill = Engine.getBill(id);

        //no match
        assertNotNull(bill);
        assertEquals(bill.size(), 0);

        //create bid / ask that will match what we just added
        bid = TestHelper.getJordan(13, uId2, false);
        ask = TestHelper.getAirMax(13, uId2, false);

        message = Message.getMessage(bid,ask);

        id = Engine.send(message);

        bill = Engine.getBill(id);

        assertNotNull(bill);
        assertEquals(bill.size(), 2);

        //my shoe I placed as a bid
        ShoeSwap myBid = (ShoeSwap)bill.get(0);
        assertNotNull(myBid);
        assertEquals(myBid.getModel(), bid.getModel());
        assertEquals(myBid.getSize(), bid.getSize());
        assertEquals(myBid.getUserId(), bid.getUserId());
        assertTrue(myBid.isMatched());

        UUID matchId = myBid.getMatchId();
        Swap pair = myBid.getPair();

        //the user of the shoe who matches the one i want
        ShoeSwap match = (ShoeSwap)bill.get(1);
        assertNotNull(match);
        assertEquals(match.getModel(), ask.getModel());
        assertEquals(match.getSize(), ask.getSize());
        assertEquals(match.getUserId(), uId1);
        assertTrue(match.isMatched());
        assertEquals(matchId, match.getMatchId());
        assertNotSame(pair, match.getPair());

        Statistics stats = bill.getStatistics();
        System.out.println(stats.toString());
    }

    /**
     * Test: user enters shoe swap that matches across models and makes and a shoe is returned
     *
     * @throws Exception
     */
    @org.junit.Test
    public void testEngineFillCross2() throws Exception {
        //write test names so we know which test had which output
        String functionName = "engine fill cross 2";
        System.out.println("Starting test: " + functionName);

        UUID uId1 = UUID.randomUUID();
        UUID uId2 = UUID.randomUUID();

        //create a bid / ask combo to put in the book
        ShoeSwap bid = TestHelper.getRose(13, uId1, false);
        ShoeSwap ask = TestHelper.getJordan(13, uId1, false);

        Message message = Message.getMessage(bid,ask);

        UUID id = Engine.send(message);

        Bill bill = Engine.getBill(id);

        assertNotNull(bill);
        assertEquals(bill.size(), 0);

        //create bid / ask that will match what we entered above
        bid = TestHelper.getJordan(13, uId2, false);
        ask = TestHelper.getRose(13, uId2, false);

        message = Message.getMessage(bid,ask);

        id = Engine.send(message);

        bill = Engine.getBill(id);

        assertNotNull(bill);
        assertEquals(bill.size(), 2);

        //my shoe I placed as a bid
        ShoeSwap myBid = (ShoeSwap)bill.get(0);
        assertNotNull(myBid);
        assertEquals(myBid.getModel(), bid.getModel());
        assertEquals(myBid.getSize(), bid.getSize());
        assertEquals(myBid.getUserId(), bid.getUserId());
        assertTrue(myBid.isMatched());

        UUID matchId = myBid.getMatchId();
        Swap pair = myBid.getPair();

        //the user of the shoe who matches the one i want
        ShoeSwap match = (ShoeSwap)bill.get(1);
        assertNotNull(match);
        assertEquals(match.getModel(), ask.getModel());
        assertEquals(match.getSize(), ask.getSize());
        assertEquals(match.getUserId(), uId1);
        assertTrue(match.isMatched());
        assertEquals(matchId, match.getMatchId());
        assertNotSame(pair, match.getPair());

        Statistics stats = bill.getStatistics();
        System.out.println(stats.toString());
    }

    /**
     * Test: user enters shoe swap that matches and a shoe is not returned
     *
     * @throws Exception
     */
    @org.junit.Test
    public void testEngineFillNoMatch() throws Exception {
        //write test names so we know which test had which output
        String functionName = "engine fill no match";
        System.out.println("Starting test: " + functionName);

        UUID uId = UUID.randomUUID();

        //create bid ask that will not match what is in the book
        ShoeSwap bid = TestHelper.getJordan(testBidSize, uId, false);
        ShoeSwap ask = TestHelper.getJordan(testAskSize, uId, false);

        Message message = Message.getMessage(bid,ask);

        UUID id = Engine.send(message);

        Bill bill = Engine.getBill(id);

        assertNotNull(bill);
        assertEquals(bill.size(), 0);

        Statistics stats = bill.getStatistics();
        System.out.println(stats.toString());
    }

    /**
     * Test: tries to grab an order with bad messages
     *
     * @throws Exception
     */
    @org.junit.Test
    public void testEngineGrabNull() throws Exception {
        //write test names so we know which test had which output
        String functionName = "null grab";
        System.out.println("Starting test: " + functionName);

        UUID uId = UUID.randomUUID();

        //create a swap with a null model
        ShoeSwap ask = TestHelper.getNullModel(testAskSize, uId);

        Message message = Message.getMessage(ask);

        UUID id = Engine.send(message);

        assertNull(id);
    }


    /**
     * Test: enter an ask into the engine and receive a match
     * @throws Exception
     */
    @org.junit.Test
    public void testEngineGrab() throws Exception {
        //write test names so we know which test had which output
        String functionName = "engine grab";
        System.out.println("Starting test: " + functionName);

        UUID uId = UUID.randomUUID();

        //create a swap that will match what is in the book
        ShoeSwap ask = TestHelper.getJordan(testBidSize, uId, false);

        Message message = Message.getMessage(ask);

        Engine.send(message);

        Bill bill = Engine.getBill(message.getId());

        assertNotNull(bill);
        assertEquals(bill.size(), 1);

        //the user of the shoe who matches the one i want
        ShoeSwap match = (ShoeSwap)bill.get(0);
        assertNotNull(match);
        assertEquals(match.getModel(), ask.getModel());
        assertEquals(match.getSize(), ask.getSize());
        assertEquals(match.getUserId(), userId);
        assertTrue(match.isMatched());
        assertTrue(match.isForSale());
        assertNotNull(match.getMatchId());
        assertNotNull(match.getPair());

        Statistics stats = bill.getStatistics();
        System.out.println(stats.toString());
    }

    /**
     * Test: enters a swap engine with a null ask and then enters a grab to find that in the engine and receive a match
     * @throws Exception
     */
    @org.junit.Test
    public void testEngineGrabNoAsk() throws Exception {
        //write test names so we know which test had which output
        String functionName = "engine grab no ask";
        System.out.println("Starting test: " + functionName);

        UUID uId1 = UUID.randomUUID();
        UUID uId2 = UUID.randomUUID();

        //create bid to enter in the book and the ask for querying that bid after
        ShoeSwap bid = TestHelper.getJordan(13, uId1, true);
        ShoeSwap ask = TestHelper.getJordan(13, uId2, false);

        //entered a swap with a null ask
        Message message = Message.getMessage(bid, null);
        Engine.send(message);

        Bill bill = Engine.getBill(message.getId());

        assertNotNull(bill);
        assertEquals(bill.size(), 0);

        //now send the grab engine
        message = Message.getMessage(ask);

        Engine.send(message);

        bill = Engine.getBill(message.getId());

        assertNotNull(bill);
        assertEquals(bill.size(), 1);

        //the user of the shoe who matches the one i want
        ShoeSwap match = (ShoeSwap)bill.get(0);
        assertNotNull(match);
        assertEquals(match.getModel(), bid.getModel());
        assertEquals(match.getSize(), bid.getSize());
        assertEquals(match.getUserId(), bid.getUserId());
        assertTrue(match.isMatched());
        assertTrue(match.isForSale());
        assertNotNull(match.getMatchId());
        assertNull(match.getPair());

        Statistics stats = bill.getStatistics();
        System.out.println(stats.toString());
    }

    /**
     * Test: enters a swap engine with a bid that is not for sale and then tries to grab it but fails
     * @throws Exception
     */
    @org.junit.Test
    public void testEngineGrabNotForSale() throws Exception {
        //write test names so we know which test had which output
        String functionName = "engine grab not for sale";
        System.out.println("Starting test: " + functionName);

        UUID uId1 = UUID.randomUUID();
        UUID uId2 = UUID.randomUUID();

        //bid is not for sale
        ShoeSwap bid = TestHelper.getJordan(13, uId1, false);
        ShoeSwap ask = TestHelper.getJordan(14, uId2, false);

        Message message = Message.getMessage(bid, ask);
        Engine.send(message);

        Bill bill = Engine.getBill(message.getId());

        assertNotNull(bill);
        assertEquals(bill.size(), 0);

        //make new bid
        bid = TestHelper.getJordan(13, uId2, false);

        //tries to grab bid but cannot since
        message = Message.getMessage(bid);

        Engine.send(message);

        bill = Engine.getBill(message.getId());

        assertNotNull(bill);
        assertEquals(bill.size(), 0);

        Statistics stats = bill.getStatistics();
        System.out.println(stats.toString());
    }


    /**
     * Test: enter an ask and receive no match
     * @throws Exception
     */
    @org.junit.Test
    public void testEngineGrabNoMatch() throws Exception {
        //write test names so we know which test had which output
        String functionName = "engine grab no match";
        System.out.println("Starting test: " + functionName);

        UUID uId = UUID.randomUUID();

        //create an ask for which there is no match in the engine
        ShoeSwap ask = TestHelper.getJordan(testAskSize, uId, false);

        Message message = Message.getMessage(ask);

        UUID id = Engine.send(message);

        Bill bill = Engine.getBill(id);

        assertNotNull(bill);
        assertEquals(bill.size(), 0);

        Statistics stats = bill.getStatistics();
        System.out.println(stats.toString());
    }

    /**
     * Test: series of bad messages into the goal
     * @throws Exception
     */
    @org.junit.Test
    public void testEngineGoalNull1() throws Exception {
        //write test names so we know which test had which output
        String functionName = "null goal 1";
        System.out.println("Starting test: " + functionName);

        UUID uId = UUID.randomUUID();

        //create bid with a null model and a normal goal
        ShoeSwap bid = TestHelper.getNullModel(testAskSize, uId);
        ShoeSwap goal = TestHelper.getJordan(testBidSize, uId, false);

        Message message = Message.getMessage(bid, goal, ApolloHelper.TTL);

        assertEquals(message.getMaxBillLength(), ApolloHelper.TTL);

        UUID id = Engine.send(message);

        assertNull(id);
    }

    @org.junit.Test
    public void testEngineGoalNull2() throws Exception {
        //write test names so we know which test had which output
        String functionName = "null goal 2";
        System.out.println("Starting test: " + functionName);

        UUID uId = UUID.randomUUID();

        //create bid and goal with a null model
        ShoeSwap bid = TestHelper.getJordan(testAskSize, uId, false);
        ShoeSwap goal = TestHelper.getNullModel(testBidSize, uId);

        Message message = Message.getMessage(bid, goal, 2);
        assertEquals(message.getMaxBillLength(), ApolloHelper.TTL);

        UUID id = Engine.send(message);

        assertNull(id);
    }

    @org.junit.Test
    public void testEngineGoalNull3() throws Exception {
        //write test names so we know which test had which output
        String functionName = "null goal 3";
        System.out.println("Starting test: " + functionName);

        UUID uId = UUID.randomUUID();

        //double null model
        ShoeSwap bid = TestHelper.getNullModel(testAskSize, uId);
        ShoeSwap goal = TestHelper.getNullModel(testBidSize, uId);

        Message message = Message.getMessage(bid, goal, 2);

        assertEquals(message.getMaxBillLength(), ApolloHelper.TTL);

        UUID id = Engine.send(message);

        assertNull(id);
    }

    /**
     * Test: send a goal engine into the engine and return a match
     * @throws Exception
     */
    @org.junit.Test
    public void testEngineGoal() throws Exception {
        //write test names so we know which test had which output
        String functionName = "engine goal";
        System.out.println("Starting test: " + functionName);

        UUID uId = UUID.randomUUID();

        //create bid ask that will immediately match what is in the book
        ShoeSwap bid = TestHelper.getJordan(testAskSize, uId, false);
        ShoeSwap goal = TestHelper.getJordan(testBidSize, uId, false);

        Message message = Message.getMessage(bid, goal, 2);

        assertEquals(message.getMaxBillLength(), 2);

        UUID id = Engine.send(message);

        Bill bill = Engine.getBill(id);

        assertNotNull(bill);
        assertEquals(bill.size(), 2);

        //my shoe I placed as a bid
        ShoeSwap myBid = (ShoeSwap)bill.get(0);
        assertNotNull(myBid);
        assertEquals(myBid.getModel(), bid.getModel());
        assertEquals(myBid.getSize(), bid.getSize());
        assertEquals(myBid.getUserId(), bid.getUserId());
        assertFalse(myBid.isMatched());

        //the user of the shoe who matches the one i want
        ShoeSwap match = (ShoeSwap)bill.get(1);
        assertNotNull(match);
        assertEquals(match.getModel(), goal.getModel());
        assertEquals(match.getSize(), goal.getSize());
        assertEquals(match.getUserId(), userId);
        assertFalse(match.isMatched());

        Statistics stats = bill.getStatistics();
        System.out.println(stats.toString());
    }

    /**
     * Test: send a goal engine into the engine where the bid and ask are different models and return a match
     * @throws Exception
     */
    @org.junit.Test
    public void testEngineGoalCross1() throws Exception {
        //write test names so we know which test had which output
        String functionName = "engine goal cross 1";
        System.out.println("Starting test: " + functionName);

        UUID uId1 = UUID.randomUUID();
        UUID uId2 = UUID.randomUUID();
        Deque<ShoeSwap> expectedChain = new LinkedList<ShoeSwap>();

        //create a bid and ask that have different models but same make, send to engine
        ShoeSwap bid = TestHelper.getAirMax(13, uId1, false);
        ShoeSwap ask = TestHelper.getJordan(13, uId1, false);

        //adding bid the original bid to the expected chain
        expectedChain.add(bid);

        //send bid/ask to set the state
        Message message = Message.getMessage(bid, ask);
        UUID id = Engine.send(message);
        Bill bill = Engine.getBill(id);

        assertNotNull(bill);
        TestCase.assertEquals(bill.size(), 0);

        bid = TestHelper.getJordan(13, uId2, false);
        ShoeSwap goal = TestHelper.getAirMax(13, uId2, false);

        //adding new bid to front of chain since this is what the "user" owns
        expectedChain.addFirst(bid);

        //create goal engine that should match the bid/ask that was just entered
        message = Message.getMessage(bid, goal, 2);

        assertEquals(message.getMaxBillLength(), 2);

        id = Engine.send(message);

        bill = Engine.getBill(id);

        assertNotNull(bill);
        assertEquals(bill.size(), 2);

        //my shoe I placed as a bid
        ShoeSwap myBid = (ShoeSwap)bill.get(0);
        ShoeSwap expected = expectedChain.pollFirst();

        assertNotNull(myBid);

        assertEquals(myBid.getModel(),expected.getModel());
        assertEquals(myBid.getSize(), expected.getSize());
        assertEquals(myBid.getUserId(), expected.getUserId());
        assertTrue(!myBid.isMatched());

        //the user of the shoe who matches the one i want
        ShoeSwap match = (ShoeSwap)bill.get(1);
        expected = expectedChain.pollFirst();

        assertNotNull(match);
        assertEquals(match.getModel(), expected.getModel());
        assertEquals(match.getSize(), expected.getSize());
        assertEquals(match.getUserId(), expected.getUserId());
        assertTrue(!match.isMatched());

        Statistics stats = bill.getStatistics();
        System.out.println(stats.toString());
    }

    /**
     * Test: send a goal engine into the engine where the bid, ask and goal are different and return a match
     * @throws Exception
     */
    @org.junit.Test
    public void testEngineGoalCross2() throws Exception {
        //write test names so we know which test had which output
        String functionName = "engine goal cross 2";
        System.out.println("Starting test: " + functionName);

        UUID uId1 = UUID.randomUUID();
        UUID uId2 = UUID.randomUUID();
        UUID uId3 = UUID.randomUUID();
        Deque<ShoeSwap> expectedChain = new LinkedList<ShoeSwap>();

        //create bid / ask where the models are different but the make is the same
        ShoeSwap bid = TestHelper.getAirMax(13, uId1, false);
        ShoeSwap ask = TestHelper.getJordan(13, uId1, false);

        //add bid to the expected chain
        expectedChain.add(bid);

        Message message = Message.getMessage(bid, ask);

        UUID id = Engine.send(message);

        Bill bill = Engine.getBill(id);

        assertNotNull(bill);
        assertEquals(bill.size(), 0);

        //create and send another bid / ask to continue the length of the goal chain
        bid = TestHelper.getAirforceOne(13, uId2, false);
        ask = TestHelper.getAirMax(13, uId2, false);

        //add next bid to the expected chain
        expectedChain.add(bid);

        message = Message.getMessage(bid, ask);

        id = Engine.send(message);

        bill = Engine.getBill(id);

        assertNotNull(bill);
        assertEquals(bill.size(), 0);

        //create a bid that is the first ask and a goal that is the last bid so the chain should be three
        bid = TestHelper.getJordan(13, uId3, false);
        ShoeSwap goal = TestHelper.getAirforceOne(13, uId3, false);

        //add the original bid to the head of the expected chain, the expected chain should now match the bill list
        expectedChain.addFirst(bid);

        message = Message.getMessage(bid, goal, ApolloHelper.TTL);

        assertEquals(message.getMaxBillLength(), ApolloHelper.TTL);

        id = Engine.send(message);

        bill = Engine.getBill(id);

        assertNotNull(bill);
        assertEquals(bill.size(), 3);

        //my shoe I placed as a bid
        ShoeSwap myBid = (ShoeSwap)bill.get(0);
        ShoeSwap expected = expectedChain.pollFirst();

        assertNotNull(myBid);
        assertEquals(myBid.getModel(), expected.getModel());
        assertEquals(myBid.getSize(), expected.getSize());
        assertEquals(myBid.getUserId(), expected.getUserId());
        assertFalse(myBid.isMatched());

        //the user of the shoe who matches the one that will get me to the one i want
        ShoeSwap match = (ShoeSwap)bill.get(1);
        expected = expectedChain.pollFirst();

        assertNotNull(match);
        assertEquals(match.getModel(), expected.getModel());
        assertEquals(match.getSize(), expected.getSize());
        assertEquals(match.getUserId(), expected.getUserId());
        assertFalse(match.isMatched());

        //the user of the shoe who matches the one i want
        match = (ShoeSwap)bill.get(2);
        expected = expectedChain.pollFirst();

        assertNotNull(match);
        assertEquals(match.getModel(), expected.getModel());
        assertEquals(match.getSize(), expected.getSize());
        assertEquals(match.getUserId(), expected.getUserId());
        assertFalse(match.isMatched());

        Statistics stats = bill.getStatistics();
        System.out.println(stats.toString());
    }

    /**
     * Test: send a goal engine into the engine where the bid and ask are different models and return a match
     * @throws Exception
     */
    @org.junit.Test
    public void testEngineGoalCross3() throws Exception {
        //write test names so we know which test had which output
        String functionName = "engine goal cross 3";
        System.out.println("Starting test: " + functionName);

        UUID uId1 = UUID.randomUUID();
        UUID uId2 = UUID.randomUUID();
        Deque<ShoeSwap> expectedChain = new LinkedList<ShoeSwap>();

        //create a bid and ask that have different models and makes, send to engine
        ShoeSwap bid = TestHelper.getAnswerV(13, uId1, false);
        ShoeSwap ask = TestHelper.getJordan(13, uId1, false);

        //adding bid the original bid to the expected chain
        expectedChain.add(bid);

        //send bid/ask to set the state
        Message message = Message.getMessage(bid, ask);
        UUID id = Engine.send(message);
        Bill bill = Engine.getBill(id);

        assertNotNull(bill);
        TestCase.assertEquals(bill.size(), 0);

        bid = TestHelper.getJordan(13, uId2, false);
        ShoeSwap goal = TestHelper.getAnswerV(13, uId2, false);

        //adding new bid to front of chain since this is what the "user" owns
        expectedChain.addFirst(bid);

        //create goal engine that should match the bid/ask that was just entered
        message = Message.getMessage(bid, goal, 2);

        assertEquals(message.getMaxBillLength(), 2);

        id = Engine.send(message);

        bill = Engine.getBill(id);

        assertNotNull(bill);
        assertEquals(bill.size(), 2);

        //my shoe I placed as a bid
        ShoeSwap myBid = (ShoeSwap)bill.get(0);
        ShoeSwap expected = expectedChain.pollFirst();

        assertNotNull(myBid);

        assertEquals(myBid.getModel(),expected.getModel());
        assertEquals(myBid.getSize(), expected.getSize());
        assertEquals(myBid.getUserId(), expected.getUserId());
        assertTrue(!myBid.isMatched());

        //the user of the shoe who matches the one i want
        ShoeSwap match = (ShoeSwap)bill.get(1);
        expected = expectedChain.pollFirst();

        assertNotNull(match);
        assertEquals(match.getModel(), expected.getModel());
        assertEquals(match.getSize(), expected.getSize());
        assertEquals(match.getUserId(), expected.getUserId());
        assertTrue(!match.isMatched());

        Statistics stats = bill.getStatistics();
        System.out.println(stats.toString());
    }

    /**
     * Test: send a goal engine into the engine where the bid, ask and goal are different and return a match
     * @throws Exception
     */
    @org.junit.Test
    public void testEngineGoalCross4() throws Exception {
        //write test names so we know which test had which output
        String functionName = "engine goal cross 4";
        System.out.println("Starting test: " + functionName);

        UUID uId1 = UUID.randomUUID();
        UUID uId2 = UUID.randomUUID();
        UUID uId3 = UUID.randomUUID();
        Deque<ShoeSwap> expectedChain = new LinkedList<ShoeSwap>();

        //create bid / ask where the models are different and the make is different
        ShoeSwap bid = TestHelper.getRose(13, uId1, false);
        ShoeSwap ask = TestHelper.getJordan(13, uId1, false);

        //add bid to the expected chain
        expectedChain.add(bid);

        Message message = Message.getMessage(bid, ask);

        UUID id = Engine.send(message);

        Bill bill = Engine.getBill(id);

        assertNotNull(bill);
        assertEquals(bill.size(), 0);

        //create and send another bid / ask to continue the length of the goal chain
        bid = TestHelper.getAnswerV(13, uId2, false);
        ask = TestHelper.getRose(13, uId2, false);

        //add next bid to the expected chain
        expectedChain.add(bid);

        message = Message.getMessage(bid, ask);

        id = Engine.send(message);

        bill = Engine.getBill(id);

        assertNotNull(bill);
        assertEquals(bill.size(), 0);

        //create a bid that is the first ask and a goal that is the last bid so the chain should be three
        bid = TestHelper.getJordan(13, uId3, false);
        ShoeSwap goal = TestHelper.getAnswerV(13, uId3, false);

        //add the original bid to the head of the expected chain, the expected chain should now match the bill list
        expectedChain.addFirst(bid);

        message = Message.getMessage(bid, goal, ApolloHelper.TTL);

        assertEquals(message.getMaxBillLength(), ApolloHelper.TTL);

        id = Engine.send(message);

        bill = Engine.getBill(id);

        assertNotNull(bill);
        assertEquals(bill.size(), 3);

        //my shoe I placed as a bid
        ShoeSwap myBid = (ShoeSwap)bill.get(0);
        ShoeSwap expected = expectedChain.pollFirst();

        assertNotNull(myBid);
        assertEquals(myBid.getModel(), expected.getModel());
        assertEquals(myBid.getSize(), expected.getSize());
        assertEquals(myBid.getUserId(), expected.getUserId());
        assertFalse(myBid.isMatched());

        //the user of the shoe who matches the one that will get me to the one i want
        ShoeSwap match = (ShoeSwap)bill.get(1);
        expected = expectedChain.pollFirst();

        assertNotNull(match);
        assertEquals(match.getModel(), expected.getModel());
        assertEquals(match.getSize(), expected.getSize());
        assertEquals(match.getUserId(), expected.getUserId());
        assertFalse(match.isMatched());

        //the user of the shoe who matches the one i want
        match = (ShoeSwap)bill.get(2);
        expected = expectedChain.pollFirst();

        assertNotNull(match);
        assertEquals(match.getModel(), expected.getModel());
        assertEquals(match.getSize(), expected.getSize());
        assertEquals(match.getUserId(), expected.getUserId());
        assertFalse(match.isMatched());

        Statistics stats = bill.getStatistics();
        System.out.println(stats.toString());
    }


    /**
     * Test: send a goal engine where the path is right on the TTL so it should return
     * @throws Exception
     */
    @org.junit.Test
    public void testEngineGoalMatchTTL() throws Exception {
        //write test names so we know which test had which output
        String functionName = "engine goal match TTL";
        System.out.println("Starting test: " + functionName);

        UUID uId1 = UUID.randomUUID();
        UUID uId2 = UUID.randomUUID();
        UUID uId3 = UUID.randomUUID();
        UUID uId4 = UUID.randomUUID();
        Deque<ShoeSwap> expectedChain = new LinkedList<ShoeSwap>();

        ShoeSwap bid = null;
        ShoeSwap ask = null;
        int size1 = 0;
        int size2 = 1;

        //for however long TTL is create a chain that can be returned with from a goal engine.
        //the chain should be model1 --> model2 --> model3 --> model1 --> model2 --> model3 --> model1 , etc
        //until TTL is hit
        for(int i = 0; i < (ApolloHelper.TTL-1); i++) {
            size1++;
            size2++;
            switch(i % 3) {
                case 0:
                    bid = TestHelper.getRose(size2, uId2, false);
                    ask = TestHelper.getJordan(size1, uId2, false);
                    break;
                case 1:
                    bid = TestHelper.getAnswerV(size2, uId3, false);
                    ask = TestHelper.getRose(size1, uId3, false);
                    break;
                case 2:
                    bid = TestHelper.getJordan(size2, uId1, false);
                    ask = TestHelper.getAnswerV(size1, uId1, false);
                    break;
            }

            Message message = Message.getMessage(bid, ask);

            UUID id = Engine.send(message);

            Bill bill = Engine.getBill(id);

            //confirm no matches
            assertNotNull(bill);
            assertEquals(bill.size(), 0);

            expectedChain.add(bid);
        }

        //now depending on TTL we need to determine what the goal will actually end up being
        bid = TestHelper.getJordan(1, uId4, false);
        expectedChain.addFirst(bid);

        ShoeSwap realGoal = expectedChain.getLast();

        //make a new shoe based off that goal above
        ShoeSwap goal = new ShoeSwap(realGoal.getModel(), realGoal.getSize(), uId4);

        //send goal engine
        Message message = Message.getMessage(bid, goal, ApolloHelper.TTL);

        assertEquals(message.getMaxBillLength(), ApolloHelper.TTL);

        UUID id = Engine.send(message);

        Bill bill = Engine.getBill(id);

        assertNotNull(bill);
        assertEquals(bill.size(), ApolloHelper.TTL);

        //confirm that the goal path is what we expected
        //my shoe I placed as a bid should be the first entry and model1
        ShoeSwap myBid = (ShoeSwap)bill.get(0);
        ShoeSwap expected = expectedChain.pollFirst();

        assertNotNull(myBid);
        assertEquals(myBid.getModel(), expected.getModel());
        assertEquals(myBid.getSize(), expected.getSize());
        assertEquals(myBid.getUserId(), expected.getUserId());
        assertTrue(!myBid.isMatched());

        Statistics stats = bill.getStatistics();
        System.out.println(stats.toString());

        ShoeSwap swap;

        //loop through TTL amount of types, checking each swap to confirm its the right now
        for(int i = 1; i < bill.size(); i++) {
            swap = (ShoeSwap)bill.get(i);
            expected = expectedChain.pollFirst();

            assertNotNull(swap);
            assertEquals(swap.getModel(), expected.getModel());
            assertEquals(swap.getSize(), expected.getSize());
            assertEquals(swap.getUserId(), expected.getUserId());
            assertTrue(!swap.isMatched());
        }

        //now we will actually do a match to prove that with the goal information we can reach that goal
        size1 = 0;
        size2 = 1;
        UUID matchUser = null;

        //now execute a successful match on everyone one
        for(int i = 0; i < (ApolloHelper.TTL-1); i++) {
            size1++;
            size2++;
            switch(i % 3) {
                case 0:
                    bid = TestHelper.getJordan(size1, uId4, false);
                    ask = TestHelper.getRose(size2, uId4, false);
                    matchUser = uId2;
                    break;
                case 1:
                    bid = TestHelper.getRose(size1, uId4, false);
                    ask = TestHelper.getAnswerV(size2, uId4, false);
                    matchUser = uId3;
                    break;
                case 2:
                    bid = TestHelper.getAnswerV(size1, uId4, false);
                    ask = TestHelper.getJordan(size2, uId4, false);
                    matchUser = uId1;
                    break;
            }

            //send the engine
            message = Message.getMessage(bid, ask);

            id = Engine.send(message);

            bill = Engine.getBill(id);

            //confirm matches this time
            assertNotNull(bill);
            assertEquals(bill.size(), 2);

            //my shoe I placed as a bid for this iteration
            myBid = (ShoeSwap)bill.get(0);
            assertNotNull(myBid);
            assertEquals(myBid.getModel(), bid.getModel());
            assertEquals(myBid.getSize(), bid.getSize());
            assertEquals(myBid.getUserId(), bid.getUserId());
            assertTrue(myBid.isMatched());

            UUID matchId = bid.getMatchId();
            Swap pair = bid.getPair();

            //the user of the shoe who matches the one i want for this iteration
            ask = (ShoeSwap)bill.get(1);
            assertNotNull(ask);
            assertEquals(ask.getModel(), ask.getModel());
            assertEquals(ask.getSize(), ask.getSize());
            assertEquals(ask.getUserId(), matchUser);
            assertTrue(ask.isMatched());
            assertEquals(matchId, ask.getMatchId());
            assertNotSame(pair, ask.getPair());
        }
    }


    /**
     * Test: send a goal where the path is over the TTL
     * @throws Exception
     */
    @org.junit.Test
    public void testEngineGoalOverTTL() throws Exception {
        //write test names so we know which test had which output
        String functionName = "engine goal over TTL";
        System.out.println("Starting test: " + functionName);

        UUID uId1 = UUID.randomUUID();
        UUID uId2 = UUID.randomUUID();
        UUID uId3 = UUID.randomUUID();
        UUID uId4 = UUID.randomUUID();
        Deque<ShoeSwap> expectedChain = new LinkedList<ShoeSwap>();

        ShoeSwap bid = null;
        ShoeSwap ask = null;

        int size1 = 0;
        int size2 = 1;

        //for however long TTL + 1 is create a chain that can be returned with from a goal engine.
        //the chain should be model1 --> model2 --> model3 --> model1 --> model2 --> model3 --> model1 , etc
        //until TTL is hit
        for(int i = 0; i < ApolloHelper.TTL; i++) {
            size1++;
            size2++;
            switch(i % 3) {
                case 0:
                    bid = TestHelper.getRose(size2, uId2, false);
                    ask = TestHelper.getJordan(size1, uId2, false);
                    break;
                case 1:
                    bid = TestHelper.getAnswerV(size2, uId3, false);
                    ask = TestHelper.getRose(size1, uId3, false);
                    break;
                case 2:
                    bid = TestHelper.getJordan(size2, uId1, false);
                    ask = TestHelper.getAnswerV(size1, uId1, false);
                    break;
            }

            Message message = Message.getMessage(bid, ask);

            UUID id = Engine.send(message);

            Bill bill = Engine.getBill(id);

            //confirm no matches
            assertNotNull(bill);
            assertEquals(bill.size(), 0);

            expectedChain.add(bid);
        }

        bid = TestHelper.getJordan(1, uId4, false);
        expectedChain.addFirst(bid);

        ShoeSwap realGoal = expectedChain.getLast();

        //make a new shoe based off that goal above
        ShoeSwap goal = new ShoeSwap(realGoal.getModel(), realGoal.getSize(), uId4);

        Message message = Message.getMessage(bid, goal, ApolloHelper.TTL);

        assertEquals(message.getMaxBillLength(), ApolloHelper.TTL);

        //send goal engine
        UUID id = Engine.send(message);

        Bill bill = Engine.getBill(id);

        assertNotNull(bill);
        //no response since it went too long
        assertEquals(bill.size(), 0);

        Statistics stats = bill.getStatistics();
        System.out.println(stats.toString());
    }

    /**
     * Test: send a goal engine into the engine and return no match
     * @throws Exception
     */
    @org.junit.Test
    public void testEngineGoalNoMatch() throws Exception {
        //write test names so we know which test had which output
        String functionName = "engine goal no match";
        System.out.println("Starting test: " + functionName);

        UUID uId = UUID.randomUUID();

        //create bid / goal to send to the engine
        ShoeSwap bid = TestHelper.getJordan(testAskSize, uId, false);
        ShoeSwap goal = TestHelper.getJordan(13, uId, false);

        Message message = Message.getMessage(bid, goal, 0);

        assertEquals(message.getMaxBillLength(), ApolloHelper.TTL);

        UUID id = Engine.send(message);

        Bill bill = Engine.getBill(id);

        assertNotNull(bill);
        //since there is no size 13 in the engine this returns nothing back
        assertEquals(bill.size(), 0);

        Statistics stats = bill.getStatistics();
        System.out.println(stats.toString());
    }

    /**
     * Test: processes book snapshot
     * @throws Exception
     */
    @org.junit.Test
    public void testBookSnapshot() throws Exception {
        //write test names so we know which test had which output
        String functionName = "book snapshot";
        System.out.println("Starting test: " + functionName);

        //gets a book snapshot
        BookSnapshot bookSnapshot = Engine.getBookSnapshot();

        //confirms it was created and has a timestamp
        assertTrue(bookSnapshot.getTimestamp() > 0);
        assertNotNull(bookSnapshot);

        //sets up the current list of makers and models so we can compare every level of the book to what we should
        //expect
        Set<Make> bookMakeSet = bookSnapshot.getMakers();
        Set<Model> bookModelSet = bookSnapshot.getModels();

        //gets a list of shoe makers
        List<Make> makeList = ShoeHelper.getShoeMakers();
        List<Model> modelList = new ArrayList<Model>(bookModelSet.size());

        //compare the static shoe maker list to what is in the book snapshot
        assertEquals(makeList.size(), bookMakeSet.size());
        assertTrue(makeList.containsAll(bookMakeSet));

        for(Make make : makeList) {
            //static is of shoe models
            List<Model> models = ShoeHelper.getTestShoeModels(make);
            modelList.addAll(models);
            for(Model model : models) {
                //for each model, get the book from the snapshot
                Book book = bookSnapshot.getBookForModel(model);

                Set<Swap> bids = book.getAllBids();
                //get the bids that were entered in the setUp() for that model
                Set<Swap> testBids = bidMap.get(model);

                //confirm they are the same size and that they contain the same bids
                assertEquals(bids.size(), testBids.size());
                assertTrue(testBids.containsAll(bids));
            }
        }

        //finally make sure all the models match between book snapshot and test model list
        assertEquals(modelList.size(), bookModelSet.size());
        assertTrue(modelList.containsAll(bookModelSet));
    }

    /**
     * Test: processes book snapshot
     * @throws Exception
     */
    @org.junit.Test
    public void testBookSnapshotEngineDead() throws Exception {
        //write test names so we know which test had which output
        String functionName = "book snapshot engine dead";
        System.out.println("Starting test: " + functionName);

        BookSnapshot bookSnapshot = Engine.getBookSnapshot();
        long timestamp = bookSnapshot.getTimestamp();

        //run the above test
        testBookSnapshot();

        //stop engine
        Engine.stop();

        //run above test again
        testBookSnapshot();

        //confirm the book snapshot hasnt changed since the engine has died
        bookSnapshot = Engine.getBookSnapshot();
        TestCase.assertEquals(timestamp, bookSnapshot.getTimestamp());
    }

    /**
     * Test: processes book snapshot after the engine has restarted
     * @throws Exception
     */
    @org.junit.Test
    public void testBookSnapshotEngineRestarted() throws Exception {
        //write test names so we know which test had which output
        String functionName = "book snapshot engine restarted";
        System.out.println("Starting test: " + functionName);

        BookSnapshot bookSnapshot = Engine.getBookSnapshot();
        long timestamp = bookSnapshot.getTimestamp();

        //run the full bookshot comparison test
        testBookSnapshot();

        //restart engine
        Engine.stop();
        Engine.start();

        //run the full bookshot comparison test again
        testBookSnapshot();

        //confirm timestamps are the same so book snapshot hasn't changed
        bookSnapshot = Engine.getBookSnapshot();
        TestCase.assertEquals(timestamp, bookSnapshot.getTimestamp());
    }


    /**
     * Test: tries to get book snapshot but is empty
     * @throws Exception
     */
    @org.junit.Test
    public void testBookSnapshotEmpty() throws Exception {
        //write test names so we know which test had which output
        String functionName = "book snapshot empty";
        System.out.println("Starting test: " + functionName);

        UUID uId = UUID.randomUUID();

        //get the book snapshot
        BookSnapshot bookSnapshot = Engine.getBookSnapshot();

        //confirm it has data
        assertNotNull(bookSnapshot);
        assertFalse(bookSnapshot.isEmpty());

        //go through each make and model and match all the bids entered in the setUp()
        List<Make> makeList = ShoeHelper.getShoeMakers();
        for(Make make : makeList) {
            List<Model> models = ShoeHelper.getTestShoeModels(make);
            for(Model model : models) {
                ShoeSwap bid = new ShoeSwap(model, testAskSize, uId);
                ShoeSwap ask = new ShoeSwap(model, testBidSize, uId);
                Message message = Message.getMessage(bid, ask);

                UUID id = Engine.send(message);
                Bill bill = Engine.getBill(id);

                assertNotNull(bill);
                assertEquals(bill.size(), 2);

                //my shoe I placed as a bid
                bid = (ShoeSwap)bill.get(0);
                assertNotNull(bid);
                assertEquals(bid.getModel(), model);
                assertEquals(bid.getSize(), testAskSize);
                assertEquals(bid.getUserId(), uId);
                assertTrue(bid.isMatched());

                UUID matchId = bid.getMatchId();
                Swap pair = bid.getPair();

                //the user of the shoe who matches the one i want
                ask = (ShoeSwap)bill.get(1);
                assertNotNull(ask);
                assertEquals(ask.getModel(), model);
                assertEquals(ask.getSize(), testBidSize);
                assertEquals(ask.getUserId(), userId);
                assertTrue(ask.isMatched());
                assertEquals(matchId, ask.getMatchId());
                assertNotSame(pair, ask.getPair());
            }
        }

        //get book snapshot again
        bookSnapshot = Engine.getBookSnapshot();

        //should be empty
        assertNotNull(bookSnapshot);
        assertTrue(bookSnapshot.isEmpty());
    }

}
