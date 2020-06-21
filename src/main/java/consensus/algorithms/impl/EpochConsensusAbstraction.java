package consensus.algorithms.impl;

import consensus.Paxos;
import consensus.algorithms.abstracts.AbstractAbstraction;
import consensus.module.IConsensus;
import utils.messages.MessagesHelper;
import utils.values.ValueHelper;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Epoch consensus is a primitive similar to consensus, where the processes propose a value
 * and may decide a value. Every epoch is identified by an epoch timestamp and has a designated leader.
 * The goal of epoch consensus is that all processes, regardless whether correct or faulty, decide the
 * same value, and it only represents an attempt to reach consensus; epoch consensus may not terminate
 * and can be aborted when it does not decide or when the next epoch should already be started by the
 * higher-level algorithm.
 * <p>
 * The bellow implementation is a Read/Write epoch consensus. Every process runs at most one epoch
 * consensus at a time and different instances never interfere with each other. The leader tries to
 * impose a decision value on the processes. The other processes witness the actions of the leader,
 * should the leader fail, and they also witness actions of leaders in earlier epochs.
 * <p>
 * The algorithm involves two rounds of message exchanges from the leader to all processes.
 * The goal is for the leader to write its proposal value to all processes, who store the epoch
 * timestamp and the value in their state and acknowledge this to the leader. When the leader
 * receives enough acknowledgments, it will ep-decide this value.
 * <p>
 * When aborted, the epoch consensus implementation simply returns its state, consisting of the
 * timestamp/value pair with the written value, and halts. It is important that the instance performs
 * sno further steps.
 */
public class EpochConsensusAbstraction extends AbstractAbstraction {

    private int ets;
    private int accepted;
    private Paxos.Value tmpVal;
    private Paxos.EpState_ state;
    private boolean canHandleMessages;

    private final Map<Paxos.ProcessId, Paxos.EpState_> states;

    protected EpochConsensusAbstraction(final IConsensus consensus, final int ets, final Paxos.EpState_ epState) {
        super(consensus);

        //could not be initialized into the init because it is received as argument into the object constructor
        this.ets = ets;
        this.state = epState;
        this.states = new ConcurrentHashMap<>();
    }

    @Override
    protected void init() {
        accepted = 0;
        canHandleMessages = true;
        tmpVal = ValueHelper.getUndefinedValue();
    }

    @Override
    public boolean onMessage(final Paxos.Message message) {
        //as long as the abstraction can handle message, than handle the message
        if (!canHandleMessages) {
            return false;
        }

        switch (message.getType()) {
            case EP_PROPOSE:
                return onEpPropose(message.getEpPropose());
            case BEB_DELIVER:
                return onBebDeliver(message.getBebDeliver());
            case PL_DELIVER:
                return onPlDeliver(message.getPlDeliver());
            case EP_ABORT:
                return onEpAbort(message.getEpAbort());
        }

        return false;
    }

    /**
     * This method is used when a EP_Purpose message is encountered
     * The leader sends a Read message to all of the processes
     *
     * @param epPropose: the message
     * @return true
     */
    private boolean onEpPropose(final Paxos.EpPropose epPropose) {
        this.tmpVal = epPropose.getValue();

        //put the message in queue
        final var epReadMessage = MessagesHelper.createEpPurposeReadMessage();
        consensus.trigger(epReadMessage);
        return true;
    }

    //region BEB_DELIVER

    /**
     * Handles the beb deliver message, and based on the message type, it will take several actions
     * Types could be READ, WRITE, DECIDED
     *
     * @param bebDeliver: the beb deliver message
     * @return true
     */
    private boolean onBebDeliver(final Paxos.BebDeliver bebDeliver) {

        final var bebMessage = bebDeliver.getMessage();
        switch (bebMessage.getType()) {
            case EP_READ_:
                return onBebDeliverEpRead(bebDeliver);
            case EP_WRITE_:
                return onBebDeliverEpWrite(bebDeliver);
            case EP_DECIDED_:
                return onBebDeliverDecided(bebDeliver);
        }

        return false;
    }

    /**
     * This method handles the BebDeliverRead message
     * After receiving this message, the process responds with a message that contains the last value stored
     * and value of the timestamp in which the stored value was written
     * (this.state.getValue() and this.state.getValueTimestamp())
     *
     * @param bebDeliverReadMessage: the beb deliver beb read message
     * @return true
     */
    private boolean onBebDeliverEpRead(final Paxos.BebDeliver bebDeliverReadMessage) {
        //get the sender
        final var sender = bebDeliverReadMessage.getSender();

        //create the epStateMessage
        final var epStateMessage = MessagesHelper
                .createEpStateMessage(state.getValueTimestamp(), sender, state.getValue());

        //put the message in queue
        consensus.trigger(epStateMessage);
        return true;
    }

    /**
     * The process receive the chosen value from the leader l and it will store it locally
     *
     * @param bebDeliverWrite: the beb write message
     * @return true
     */
    private boolean onBebDeliverEpWrite(final Paxos.BebDeliver bebDeliverWrite) {
        //get the ep write
        final var epWriteMessage = bebDeliverWrite.getMessage().getEpWrite();

        //change the inner state
        this.state = createState(ets, epWriteMessage.getValue());

        //create an epAccept message
        final var epAcceptMessage = MessagesHelper
                .createEpAcceptMessage(bebDeliverWrite.getSender());

        //put the message into queue
        consensus.trigger(epAcceptMessage);
        return true;
    }

    /**
     * Receive a decided value and decide on that value
     *
     * @param bedDeliverDecided: the received message
     * @return true
     */
    private boolean onBebDeliverDecided(final Paxos.BebDeliver bedDeliverDecided) {
        //get epDecide from the message
        final var epDecide = bedDeliverDecided.getMessage().getEpDecide();
        //creat the epDecided Message
        final var epDecideMessage = MessagesHelper.createEpDecideMessage(ets, epDecide.getValue());
        //put the message into queue
        consensus.trigger(epDecideMessage);
        return true;
    }
    //endregion

    //region PL_DELIVER

    /**
     * Handle the plDeliver message and based on the message type it decides,
     * based on its type, the correct action that needs to be done
     * <p>
     * The message type can be either the EP_STATE or EP_ACCEPT
     *
     * @param plDeliver: the plDeliver message
     * @return true if the message can be handled or false otherwise
     */
    private boolean onPlDeliver(final Paxos.PlDeliver plDeliver) {

        final var plMessage = plDeliver.getMessage();
        switch (plMessage.getType()) {
            case EP_STATE_:
                return onPlDeliverState(plDeliver);
            case EP_ACCEPT_:
                return onPlDeliverAccept(plDeliver);
        }
        return false;
    }

    /**
     * The leader receives a quorum of STATE messages and it chooses the value that comes with the highest
     * timestamp as its proposal value
     * <p>
     * The leader then writes the chosen value to all processes with a WRITE message
     *
     * @param plDeliver the message
     * @return true
     */
    private boolean onPlDeliverState(final Paxos.PlDeliver plDeliver) {

        //get the epState message
        final var epStateMessage = plDeliver.getMessage().getEpState();

        //create a state from the received timestamp and the received value, and store it into the quorum state map
        final var state = createState(epStateMessage.getValueTimestamp(), epStateMessage.getValue());

        //put the state into the quorum state map
        states.put(plDeliver.getSender(), state);

        //check if the majority of processes has decided something, and if not break the execution
        if (states.size() <= consensus.getProcess().size() / 2) {
            return true;
        }

        executeOnMoreThanHalf();
        return true;
    }

    /**
     * The write succeeds when the leader receives an ACCEPT message from a quorum of processes,
     * indicating that they have stored the value locally.
     * The leader now ep-decides the chosen value and announces this in a DECIDED message to all processes;
     * the processes that receive this ep-decide as well.
     */
    private boolean onPlDeliverAccept(final Paxos.PlDeliver ignored) {
        ++accepted;

        //if the value is less than half the processes then do nothing
        if (accepted <= consensus.getProcess().size() / 2) {
            return true;
        }

        accepted = 0;

        //create a epDecidedMessage
        final var createEpDecidedMessage = MessagesHelper.createEpDecidedMessage(ValueHelper.makeCopy(tmpVal));

        //push the message into queue
        consensus.trigger(createEpDecidedMessage);
        return true;
    }


    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private void executeOnMoreThanHalf() {
        //get the highest state (based on the timestamp)
        final var highestState = states.values()
                .stream()
                .max(Comparator.comparingInt(Paxos.EpState_::getValueTimestamp))
                .get();

        //check if the highest state is defined
        if (highestState.getValue().getDefined()) {
            this.tmpVal = highestState.getValue();
        }

        states.clear();

        //create a new EpWrite message
        final var epWriteMessage = MessagesHelper.createEpWriteMessage(ValueHelper.makeCopy(tmpVal));
        consensus.trigger(epWriteMessage);
    }
    //endregion

    /**
     * This message handles the EP_ABORT message
     * When this in encountered, the state is returned and the instance of the class stops from processing messages \
     *
     * @param ignored: this is ignored
     * @return true
     */
    private boolean onEpAbort(final Paxos.EpAbort ignored) {
        //create the epAborted message
        final var epAbortedMessage = MessagesHelper
                .createEpAbortedMessage(ets, state.getValueTimestamp(), state.getValue());

        //the abstraction can no longer receive the message
        canHandleMessages = false;

        //put the message into queue
        consensus.trigger(epAbortedMessage);
        return true;
    }

    /**
     * Create a state with the given timestamp and the given value
     *
     * @param timestamp: the timestamp
     * @param value:     the value
     * @return a instance of EpState_
     */
    private static Paxos.EpState_ createState(final int timestamp, final Paxos.Value value) {
        return Paxos.EpState_.newBuilder()
                .setValueTimestamp(timestamp)
                .setValue(value)
                .build();
    }
}
