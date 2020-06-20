package consensus.algorithms.impl;

import consensus.Paxos;
import consensus.algorithms.abstracts.AbstractLayer;
import consensus.module.IConsensus;
import utils.messages.MessagesHelper;
import utils.values.ValueHelper;

import java.util.Comparator;
import java.util.List;

import static consensus.Paxos.Message.Type.EP_ABORT;
import static consensus.Paxos.Message.Type.EP_PROPOSE;

/**
 * A uniform consensus algorithm based on a fail-noisy model (Leader driven consensus) that runs
 * through a sequence of epochs. The value that is decided by the consensus algorithm is the value
 * that is ep-decided by one of the underlying epoch consensus instances. To switch from one epoch
 * to the next, the algorithm aborts the running epoch consensus, obtains its state, and initializes
 * the next epoch consensus with the state. Hence, the algorithm invokes a well-formed sequence of
 * epoch consensus instances.
 * <p>
 * As soon as a process has obtained the proposal value for consensus from the
 * application and the process is also the leader of the current epoch, it ep-proposes this
 * value for epoch consensus. When the current epoch ep-decides a value, the process
 * also decides that value in consensus, but continues to participate in the consensus
 * algorithm, to help other processes decide.
 */
public class UniformConsensusLayer extends AbstractLayer {

    private int ets;
    private int newts;

    private Paxos.Value val;

    private boolean decided;
    private boolean proposed;

    private Paxos.ProcessId l;
    private Paxos.ProcessId newl;

    public UniformConsensusLayer(final IConsensus consensus) {
        super(consensus);
    }

    @Override
    protected void init() {
        this.val = ValueHelper.getUndefinedValue();

        this.proposed = this.decided = false;
        this.ets = this.newts = 0;

        this.l = getMinRankProcess(consensus.getProcess());
        this.newl = null;

        startNewEpoch(ets, newts, val);
    }

    @Override
    public boolean onMessage(final Paxos.Message message) {

        switch (message.getType()) {
            case UC_PROPOSE:
                return onUcPropose(message.getUcPropose());
            case EC_START_EPOCH:
                return onEcStartEpoch(message.getEcStartEpoch());
            case EP_ABORTED:
                return onEpAborted(message.getEpAborted());
            case EP_DECIDE:
                return onEpDecide(message.getEpDecide());
        }

        return false;
    }

    /**
     * Handle the ucPropose message, by updating the val
     * Also taking in consideration that val is part of condition that could trigger EP_PROPOSE, we need to check
     * if all the conditions are met
     * @param ucPropose: the ucPropose message
     * @return true
     */
    private boolean onUcPropose(final Paxos.UcPropose ucPropose) {
        this.val = ucPropose.getValue();
        checkEventTriggerCondition();
        return true;
    }

    /**
     * Handle the EcStartEpoch event
     * @param ecStartEpoch: the ec start epoch message
     * @return true
     */
    private boolean onEcStartEpoch(final Paxos.EcStartEpoch ecStartEpoch) {
        //modify the leader and the timestamp
        newl = ecStartEpoch.getNewLeader();
        newts = ecStartEpoch.getNewTimestamp();

        //create the message
        final var epAbortMessage = MessagesHelper.createEpAbortMessage();

        //put the message into queue
        consensus.trigger(epAbortMessage);
        return true;
    }


    /**
     * Handle the epAborted message
     * @param epAborted: the epAborted message
     * @return true if the ets == getEts or false otherwise
     */
    private boolean onEpAborted(final Paxos.EpAborted epAborted) {
        //check if the event condition is met
        if(ets != epAborted.getEts()) {
            return false;
        }

        //modify the values
        ets = newts;
        l = newl;
        proposed = false;
        startNewEpoch(ets, epAborted.getValueTimestamp(), epAborted.getValue());

        //check also for the trigger condition
        checkEventTriggerCondition();
        return true;
    }

    /**
     * Handle the EpDecide event
     * @param epDecide: the EP_Decide message
     * @return true if the event condition is met or false otherwise
     */
    private boolean onEpDecide(final Paxos.EpDecide epDecide) {
        //check the event condition
        if(ets != epDecide.getEts()) {
            return false;
        }

        //if not decided, decide the ep value
        if(!decided) {
            decided = true;
            consensus.trigger(MessagesHelper
                    .createUcDecideMessage(ValueHelper.makeCopy(epDecide.getValue())));
        }
        return true;
    }

    private void startNewEpoch(final int ets, final int timeStamp, final Paxos.Value val) {
        //create the EPState with timestamp 0 and value = val
        var state_ = Paxos.EpState_.newBuilder()
                .setValueTimestamp(timeStamp)
                .setValue(ValueHelper.makeCopy(val))
                .build();

        //add new layer into the consensus (epoch consensus layer)
        consensus.pushLayer(new EpochConsensusLayer(consensus, ets, state_));
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private static Paxos.ProcessId getMinRankProcess(final List<Paxos.ProcessId> processIdList) {
        //get the min rank process
        return processIdList
                .stream()
                .min(Comparator.comparingInt(Paxos.ProcessId::getRank)).get();
    }

    /**
     * This is the function that checks if the EP_Propose message will be send
     */
    private void checkEventTriggerCondition() {

        //if the condition is not met, do nothing
        if (!(l.getPort() == consensus.getCurrentPID().getPort() && val.getDefined() && !proposed)) {
            return;
        }

        //modify the values accordingly to the algorithm
        proposed = true;
        final var epProposeMessage = MessagesHelper.createEpProposeMessage(ValueHelper.makeCopy(val));

        //push the message into queue
        consensus.trigger(epProposeMessage);
    }
}
