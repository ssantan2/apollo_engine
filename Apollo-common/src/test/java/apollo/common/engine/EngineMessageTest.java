package apollo.common.engine;

import apollo.common.shoe.ShoeSwap;
import apollo.common.templates.Swap;
import apollo.common.util.ApolloHelper;
import apollo.common.util.TestHelper;
import org.junit.Test;

import java.util.UUID;

import static junit.framework.Assert.assertEquals;

/**
 * Test class for Message creation
 * Created by santana on 12/5/14.
 */
public class EngineMessageTest {

    /**
     * Test: Create a kill core message but passing in a null so the message creation fails
     */
    @Test
    public void nullKillMessageTest() {
        Message message = Message.getMessage((UUID) null);

        assertEquals(message.getType(), -1);
        assertEquals(message.getAsk(), null);
        assertEquals(message.getBid(), null);
        assertEquals(message.getGoal(), null);
        assertEquals(message.getId(), null);
        assertEquals(message.getMaxBillLength(), ApolloHelper.TTL);
    }

    /**
     * Test: create a kill core message with a random UUID and checks that the message is correct
     */
    @Test
    public void killMessageTest() {
        UUID id = UUID.randomUUID();
        Message message = Message.getMessage(id);

        assertEquals(message.getType(), Message.KILL_CORE);
        assertEquals(message.getAsk(), null);
        assertEquals(message.getBid(), null);
        assertEquals(message.getGoal(), null);
        assertEquals(message.getId(), id);
        assertEquals(message.getMaxBillLength(), ApolloHelper.TTL);
    }

    /**
     * Test: create a null Grab message and confirm it didn't create successfully
     */
    @Test
    public void nullGrabMessageTest() {
        Message message = Message.getMessage((Swap) null);

        assertEquals(message.getType(), -1);
        assertEquals(message.getAsk(), null);
        assertEquals(message.getBid(), null);
        assertEquals(message.getGoal(), null);
        assertEquals(message.getId(), null);
        assertEquals(message.getMaxBillLength(), ApolloHelper.TTL);
    }

    /**
     * Test: Create a Grab message and confirm it worked
     */
    @Test
    public void grabMessageTest() {
        ShoeSwap ask = TestHelper.getJordan(10, UUID.randomUUID(), false);

        Message message = Message.getMessage(ask);

        assertEquals(message.getType(), Message.GRAB);
        assertEquals(message.getAsk(), ask);
        assertEquals(message.getBid(), null);
        assertEquals(message.getGoal(), null);
        assertEquals(message.getMaxBillLength(), ApolloHelper.TTL);
    }

    /**
     * Test: create a null Goal message with two null inputs
     */
    @Test
    public void nullGoalMessageTest1() {
        Message message = Message.getMessage(null, null, ApolloHelper.TTL);

        assertEquals(message.getType(), -1);
        assertEquals(message.getAsk(), null);
        assertEquals(message.getBid(), null);
        assertEquals(message.getGoal(), null);
        assertEquals(message.getId(), null);
        assertEquals(message.getMaxBillLength(), ApolloHelper.TTL);
    }

    /**
     * Test: create a null Goal message with the first input being null
     */
    @Test
    public void nullGoalMessageTest2() {
        ShoeSwap swap = TestHelper.getJordan(10, UUID.randomUUID(), false);

        Message message = Message.getMessage(null, swap, ApolloHelper.TTL);

        assertEquals(message.getType(), -1);
        assertEquals(message.getAsk(), null);
        assertEquals(message.getBid(), null);
        assertEquals(message.getGoal(), null);
        assertEquals(message.getId(), null);
        assertEquals(message.getMaxBillLength(), ApolloHelper.TTL);
    }

    /**
     * Test: create a null Goal message with the second input being null
     */
    @Test
    public void nullGoalMessageTest3() {
        ShoeSwap swap = TestHelper.getJordan(10, UUID.randomUUID(), false);

        Message message = Message.getMessage(swap, null, ApolloHelper.TTL);

        assertEquals(message.getType(), -1);
        assertEquals(message.getAsk(), null);
        assertEquals(message.getBid(), null);
        assertEquals(message.getGoal(), null);
        assertEquals(message.getId(), null);
        assertEquals(message.getMaxBillLength(), ApolloHelper.TTL);
    }

    /**
     * Test: create a Goal message with a max length of 5
     */
    @Test
    public void goalMessageTest1() {
        ShoeSwap bid = TestHelper.getJordan(10, UUID.randomUUID(), false);
        ShoeSwap goal = TestHelper.getJordan(13, UUID.randomUUID(), false);

        Message message = Message.getMessage(bid, goal, 5);

        assertEquals(message.getType(), Message.GOAL);
        assertEquals(message.getAsk(), null);
        assertEquals(message.getBid(), bid);
        assertEquals(message.getGoal(), goal);
        assertEquals(message.getMaxBillLength(), 5);
    }

    /**
     * Test: create a Goal message with the max length defaulting to TTL since zero isn't acceptable
     */
    @Test
    public void goalMessageTest2() {
        ShoeSwap bid = TestHelper.getJordan(10, UUID.randomUUID(), false);
        ShoeSwap goal = TestHelper.getJordan(13, UUID.randomUUID(), false);

        Message message = Message.getMessage(bid, goal, 0);

        assertEquals(message.getType(), Message.GOAL);
        assertEquals(message.getAsk(), null);
        assertEquals(message.getBid(), bid);
        assertEquals(message.getGoal(), goal);
        assertEquals(message.getMaxBillLength(), ApolloHelper.TTL);
    }

    /**
     * Test: create a Goal message with the max length defaulting to TTL since TTL + 1 isn't acceptable
     */
    @Test
    public void goalMessageTest3() {
        ShoeSwap bid = TestHelper.getJordan(10, UUID.randomUUID(), false);
        ShoeSwap goal = TestHelper.getJordan(13, UUID.randomUUID(), false);

        Message message = Message.getMessage(bid, goal, (ApolloHelper.TTL + 1));

        assertEquals(message.getType(), Message.GOAL);
        assertEquals(message.getAsk(), null);
        assertEquals(message.getBid(), bid);
        assertEquals(message.getGoal(), goal);
        assertEquals(message.getMaxBillLength(), ApolloHelper.TTL);
    }

    /**
     * Test: create a null Swap message with both inputs being null
     */
    @Test
    public void nullSwapMessageTest1() {
        Message message = Message.getMessage(null, null);

        assertEquals(message.getType(), -1);
        assertEquals(message.getAsk(), null);
        assertEquals(message.getBid(), null);
        assertEquals(message.getGoal(), null);
        assertEquals(message.getId(), null);
        assertEquals(message.getMaxBillLength(), ApolloHelper.TTL);
    }

    /**
     * Test: create a null Swap message with the second input being null and not for sale
     */
    @Test
    public void nullSwapMessageTest2() {
        ShoeSwap swap = TestHelper.getJordan(10, UUID.randomUUID(), false);

        Message message = Message.getMessage(swap, null);

        assertEquals(message.getType(), -1);
        assertEquals(message.getAsk(), null);
        assertEquals(message.getBid(), null);
        assertEquals(message.getGoal(), null);
        assertEquals(message.getId(), null);
        assertEquals(message.getMaxBillLength(), ApolloHelper.TTL);
    }

    /**
     * Test: create a null Swap message with the first input being null
     */
    @Test
    public void nullSwapMessageTest3() {
        ShoeSwap swap = TestHelper.getJordan(10, UUID.randomUUID(), false);

        Message message = Message.getMessage(null, swap);

        assertEquals(message.getType(), -1);
        assertEquals(message.getAsk(), null);
        assertEquals(message.getBid(), null);
        assertEquals(message.getGoal(), null);
        assertEquals(message.getId(), null);
        assertEquals(message.getMaxBillLength(), ApolloHelper.TTL);
    }

    /**
     * Test: create a Swap message and confirm it is set correctly
     */
    @Test
    public void swapMessageTest1() {
        ShoeSwap bid = TestHelper.getJordan(10, UUID.randomUUID(), false);
        ShoeSwap ask = TestHelper.getJordan(13, UUID.randomUUID(), false);

        Message message = Message.getMessage(bid, ask);

        assertEquals(message.getType(), Message.SWAP);
        assertEquals(message.getAsk(), ask);
        assertEquals(message.getBid(), bid);
        assertEquals(message.getGoal(), null);
        assertEquals(message.getBid().getPair(), ask);
        assertEquals(message.getAsk().getPair(), bid);
        assertEquals(message.getMaxBillLength(), ApolloHelper.TTL);
    }

    /**
     * Test: create a Swap message with the second input being null but the bid is for sale so its valid
     */
    @Test
    public void swapMessageTest2() {
        ShoeSwap bid = TestHelper.getJordan(10, UUID.randomUUID(), true);

        Message message = Message.getMessage(bid, null);

        assertEquals(message.getType(), Message.SWAP);
        assertEquals(message.getAsk(), null);
        assertEquals(message.getBid(), bid);
        assertEquals(message.getGoal(), null);
        assertEquals(message.getBid().getPair(), null);
        assertEquals(message.getMaxBillLength(), ApolloHelper.TTL);
    }


}
