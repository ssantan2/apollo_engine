package apollo.trade.swap.blackbox;


import apollo.common.engine.Bill;
import apollo.common.engine.Message;
import apollo.common.shoe.ShoeSwap;
import apollo.common.util.TestHelper;
import apollo.trade.swap.service.Engine;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


/**
 * This class is to test multiple scenarios as opposed to basic functionality. This is more for concurrency checking
 * Created by santana on 11/15/14.
 */
public class ScenarioTest {
	//thread timeout in milliseconds 
	private long timeout = 1000;
	
	//did the first test pass?
    private boolean first = false;
    //did the second test pass?
    private boolean second = false;

	
	@Before
	public void setUp() throws Exception {
		Engine.start();
	}
	
	@After
	public void tearDown() throws Exception {
		Engine.flush();
	}
	
    /**
     * Test: two users enter the same bid and two more users get the bids. both swapping
     */
    @Test
    public void doubleBidTest() throws Exception {
        UUID uId1 = UUID.randomUUID();
        UUID uId2 = UUID.randomUUID();
        UUID uId3 = UUID.randomUUID();
        UUID uId4 = UUID.randomUUID();
        
        first = false;
        second = false;

        //user bids and asks
        ShoeSwap u1Bid = TestHelper.getJordan(13, uId1, false);
        ShoeSwap u2Bid = TestHelper.getJordan(13, uId2, false);
        final ShoeSwap u3Bid = TestHelper.getAnswerV(10, uId3, false);
        final ShoeSwap u4Bid = TestHelper.getAnswerV(10, uId1, false);

        ShoeSwap u1Ask = TestHelper.getAnswerV(10, uId1, false);
        ShoeSwap u2Ask = TestHelper.getAnswerV(10, uId2, false);
        final ShoeSwap u3Ask = TestHelper.getJordan(13, uId3, false);
        final ShoeSwap u4Ask = TestHelper.getJordan(13, uId4, false);

        //enter the original two in the engine
        Message message = Message.getMessage(u1Bid, u1Ask);

        UUID id = Engine.send(message);

        Bill bill = Engine.getBill(id);

        //no match
        assertNotNull(bill);
        assertEquals(bill.size(), 0);

        message = Message.getMessage(u2Bid, u2Ask);

        id = Engine.send(message);

        bill = Engine.getBill(id);

        //no match
        assertNotNull(bill);
        assertEquals(bill.size(), 0);
        
        //create the two threads that will act as two independent swaps. both should complete
        Thread firstSwap = new Thread(new Runnable() {
        	public void run() {
        		System.out.println("Starting first swap...");
        		
                Message message = Message.getMessage(u3Bid, u3Ask);

                UUID id = Engine.send(message);

                Bill bill = Engine.getBill(id);
                
        		System.out.println("Got first bill...");

                //a match
                assertNotNull(bill);
                assertEquals(bill.size(), 2);
                first = true;
                
        		System.out.println("first swap completed");
        	}
        });

        Thread secondSwap = new Thread(new Runnable() {
        	public void run() {
        		System.out.println("Starting second swap...");

                Message message = Message.getMessage(u4Bid, u4Ask);

                UUID id = Engine.send(message);

                Bill bill = Engine.getBill(id);
                
        		System.out.println("Got second bill...");

                //a match
                assertNotNull(bill);
                assertEquals(bill.size(), 2);
                second = true;
                
        		System.out.println("second swap completed");
        	}
        });
        
        firstSwap.start();
        secondSwap.start();
        
        firstSwap.join(timeout);
        secondSwap.join(timeout);
        
        //check if the two threads passed after the timeout
        assertTrue(first);
        assertTrue(second);
    }

    /**
     * Test: two users enter the same bid but different asks and two more users get the bids. both swapping
     */
    @Test
    public void doubleBidDifferentAskTest() throws Exception {
        UUID uId1 = UUID.randomUUID();
        UUID uId2 = UUID.randomUUID();
        UUID uId3 = UUID.randomUUID();
        UUID uId4 = UUID.randomUUID();

        first = false;
        second = false;
        
        //user bids and asks
        ShoeSwap u1Bid = TestHelper.getJordan(13, uId1, false);
        ShoeSwap u2Bid = TestHelper.getJordan(13, uId2, false);
        final ShoeSwap u3Bid = TestHelper.getAnswerV(10, uId3, false);
        final ShoeSwap u4Bid = TestHelper.getRose(9, uId1, false);

        ShoeSwap u1Ask = TestHelper.getAnswerV(10, uId1, false);
        ShoeSwap u2Ask = TestHelper.getRose(9, uId2, false);
        final ShoeSwap u3Ask = TestHelper.getJordan(13, uId3, false);
        final ShoeSwap u4Ask = TestHelper.getJordan(13, uId4, false);

        //enter the original two in the engine
        Message message = Message.getMessage(u1Bid, u1Ask);

        UUID id = Engine.send(message);

        Bill bill = Engine.getBill(id);

        //no match
        assertNotNull(bill);
        assertEquals(bill.size(), 0);

        message = Message.getMessage(u2Bid, u2Ask);

        id = Engine.send(message);

        bill = Engine.getBill(id);

        //no match
        assertNotNull(bill);
        assertEquals(bill.size(), 0);
        
        //two independent swaps but with different bids. both should complete
        Thread firstSwap = new Thread(new Runnable() {
        	public void run() {
        		System.out.println("Starting first swap...");

                Message message = Message.getMessage(u3Bid, u3Ask);

                UUID id = Engine.send(message);

                Bill bill = Engine.getBill(id);
                
        		System.out.println("Got first bill...");

                //a match
                assertNotNull(bill);
                assertEquals(bill.size(), 2);
                first = true;

                System.out.println("first swap completed");

        	}
        });

        Thread secondSwap = new Thread(new Runnable() {
        	public void run() {
        		System.out.println("Starting second swap...");

                Message message = Message.getMessage(u4Bid, u4Ask);

                UUID id = Engine.send(message);

                Bill bill = Engine.getBill(id);
                
        		System.out.println("Got second bill...");

                //a match
                assertNotNull(bill);
                assertEquals(bill.size(), 2);
                second = true;
                
                System.out.println("second swap completed");

        	}
        });
        
        firstSwap.start();
        secondSwap.start();
        
        firstSwap.join(timeout);
        secondSwap.join(timeout);
        
        //check if the two threads passed after the timeout
        assertTrue(first);
        assertTrue(second);
    }

	/**
	 *	Test: Two more users get the bids. One grabbing and one swapping
     */
    @Test
    public void doubleBidGrabOneTest() throws Exception {
        UUID uId1 = UUID.randomUUID();
        UUID uId2 = UUID.randomUUID();
        UUID uId3 = UUID.randomUUID();
        UUID uId4 = UUID.randomUUID();
        
        first = false;
        second = false;

        //user bids and asks
        ShoeSwap u1Bid = TestHelper.getJordan(13, uId1, false);
        ShoeSwap u2Bid = TestHelper.getJordan(13, uId2, true);
        final ShoeSwap u3Bid = TestHelper.getAnswerV(10, uId3, false);

        ShoeSwap u1Ask = TestHelper.getAnswerV(10, uId1, false);
        ShoeSwap u2Ask = TestHelper.getAnswerV(10, uId2, false);
        final ShoeSwap u3Ask = TestHelper.getJordan(13, uId3, false);
        final ShoeSwap u4Ask = TestHelper.getJordan(13, uId4, false);

        //enter the original two in the engine
        Message message = Message.getMessage(u1Bid, u1Ask);

        UUID id = Engine.send(message);

        Bill bill = Engine.getBill(id);

        //no match
        assertNotNull(bill);
        assertEquals(bill.size(), 0);

        message = Message.getMessage(u2Bid, u2Ask);

        id = Engine.send(message);

        bill = Engine.getBill(id);

        //no match
        assertNotNull(bill);
        assertEquals(bill.size(), 0);
        
        //two independent messages, one a grab and one a swap, both complete
        Thread firstSwap = new Thread(new Runnable() {
        	public void run() {
        		System.out.println("Starting first swap...");

                Message message = Message.getMessage(u3Bid, u3Ask);

                UUID id = Engine.send(message);

                Bill bill = Engine.getBill(id);
                
        		System.out.println("Got first bill...");

                //a match
                assertNotNull(bill);
                assertEquals(bill.size(), 2);
                first = true;
                
                System.out.println("first swap completed");
        	}
        });

        Thread secondGrab = new Thread(new Runnable() {
        	public void run() {
        		System.out.println("Starting second grab...");

                Message message = Message.getMessage(u4Ask);

                UUID id = Engine.send(message);

                Bill bill = Engine.getBill(id);
                
        		System.out.println("Got second bill...");

                //a grab match
                assertNotNull(bill);
                assertEquals(bill.size(), 1);
                second = true;
                
                System.out.println("second grab completed");
        	}
        });
        
        firstSwap.start();
        secondGrab.start();
        
        firstSwap.join(timeout);
        secondGrab.join(timeout);
        
        //check if the two threads passed after the timeout
        assertTrue(first);
        assertTrue(second);
    }

    /**
     * Test: two users enter the same bid and two more users get the bids. One can only grab and one is swapped
     */
    @Test
    public void doubleBidGrabOneTest2() throws Exception {
        UUID uId1 = UUID.randomUUID();
        UUID uId2 = UUID.randomUUID();
        UUID uId3 = UUID.randomUUID();
        UUID uId4 = UUID.randomUUID();
        
        first = false;
        second = false;

        //user bids and asks
        ShoeSwap u1Bid = TestHelper.getJordan(13, uId1, false);
        ShoeSwap u2Bid = TestHelper.getJordan(13, uId2, true);
        final ShoeSwap u3Bid = TestHelper.getAnswerV(10, uId3, false);

        ShoeSwap u1Ask = TestHelper.getAnswerV(10, uId1, false);
        ShoeSwap u2Ask = null;
        final ShoeSwap u3Ask = TestHelper.getJordan(13, uId3, false);
        final ShoeSwap u4Ask = TestHelper.getJordan(13, uId4, false);

        //enter the original two in the engine
        Message message = Message.getMessage(u1Bid, u1Ask);

        UUID id = Engine.send(message);

        Bill bill = Engine.getBill(id);

        //no match
        assertNotNull(bill);
        assertEquals(bill.size(), 0);

        //null ask so only a grab
        message = Message.getMessage(u2Bid, u2Ask);

        id = Engine.send(message);

        bill = Engine.getBill(id);

        //no match
        assertNotNull(bill);
        assertEquals(bill.size(), 0);
        
        //two independent messages, one grab only and one swap
        Thread firstSwap = new Thread(new Runnable() {
        	public void run() {
        		System.out.println("Starting first swap...");

                Message message = Message.getMessage(u3Bid, u3Ask);

                UUID id = Engine.send(message);

                Bill bill = Engine.getBill(id);

        		System.out.println("Got first bill...");

                //a match
                assertNotNull(bill);
                assertEquals(bill.size(), 2);
                first = true;
                
                System.out.println("first swap completed");
        	}
        });

        Thread secondGrab = new Thread(new Runnable() {
        	public void run() {
        		System.out.println("Starting second grab...");

                Message message = Message.getMessage(u4Ask);

                UUID id = Engine.send(message);

                Bill bill = Engine.getBill(id);

        		System.out.println("Got second bill...");

                //a grab match
                assertNotNull(bill);
                assertEquals(bill.size(), 1);
                second = true;
                
                System.out.println("second grab completed");
        	}
        });
        
        firstSwap.start();
        secondGrab.start();
        
        firstSwap.join(timeout);
        secondGrab.join(timeout);
        
        //check if the two threads passed after the timeout
        assertTrue(first);
        assertTrue(second);
    }

    /**
     * Test: two users enter the same bid and two more users get the bids. both try to swap but one is grab only
     */
    @Test
    public void doubleBidSwapBothFailTest() throws Exception {
        UUID uId1 = UUID.randomUUID();
        UUID uId2 = UUID.randomUUID();
        UUID uId3 = UUID.randomUUID();
        UUID uId4 = UUID.randomUUID();
        
        first = false;
        second = false;

        //user bids and asks
        ShoeSwap u1Bid = TestHelper.getJordan(13, uId1, false);
        ShoeSwap u2Bid = TestHelper.getJordan(13, uId2, true);
        final ShoeSwap u3Bid = TestHelper.getAnswerV(10, uId3, false);
        final ShoeSwap u4Bid = TestHelper.getRose(9, uId1, false);

        ShoeSwap u1Ask = TestHelper.getAnswerV(10, uId1, false);
        ShoeSwap u2Ask = null;
        final ShoeSwap u3Ask = TestHelper.getJordan(13, uId3, false);
        final ShoeSwap u4Ask = TestHelper.getJordan(13, uId4, false);

        //enter the original two in the engine
        Message message = Message.getMessage(u1Bid, u1Ask);

        UUID id = Engine.send(message);

        Bill bill = Engine.getBill(id);

        //no match
        assertNotNull(bill);
        assertEquals(bill.size(), 0);

        //null ask so only a grab
        message = Message.getMessage(u2Bid, u2Ask);

        id = Engine.send(message);

        bill = Engine.getBill(id);

        //no match
        assertNotNull(bill);
        assertEquals(bill.size(), 0);
        
        //two independent messages, both swaps, second one fails cause only grab is left in the system
        Thread firstSwap = new Thread(new Runnable() {
        	public void run() {
        		System.out.println("Starting first swap...");

                Message message = Message.getMessage(u3Bid, u3Ask);

                UUID id = Engine.send(message);

                Bill bill = Engine.getBill(id);

        		System.out.println("Got first bill...");

                //a match
                assertNotNull(bill);
                assertEquals(bill.size(), 2);
                first = true;
                
                System.out.println("first swap completed");
        	}
        });

        Thread secondSwap = new Thread(new Runnable() {
        	public void run() {
        		System.out.println("Starting second swap...");

                Message message = Message.getMessage(u4Bid, u4Ask);

                UUID id = Engine.send(message);

                Bill bill = Engine.getBill(id);
                
        		System.out.println("Got second bill...");

                //no match
                assertNotNull(bill);
                assertEquals(bill.size(), 0);
                second = true;
                
                System.out.println("second swap completed");
        	}
        });
        
        firstSwap.start();
        secondSwap.start();
        
        firstSwap.join(timeout);
        secondSwap.join(timeout);
                
        //check if the two threads passed after the timeout
        assertTrue(first);
        assertTrue(second);
    }

}
