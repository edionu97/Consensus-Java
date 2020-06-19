package consensus.algorithms.impl;

import consensus.Paxos;
import consensus.algorithms.abstracts.AbstractLayer;
import consensus.module.IConsensus;
import utils.values.ValueHelper;

import java.util.Comparator;
import java.util.List;

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
            case UC_PROPOSE: return onUcPropose(message.getUcPropose());
            case EC_START_EPOCH: return onEcStartEpoch(message.getEcStartEpoch());
            case EP_ABORTED: return onEpAborted(message.getEpAborted());
            case EP_DECIDE: return onEpDecide(message.getEpDecide());
        }

        return false;
    }

    private boolean onUcPropose(final Paxos.UcPropose ucPropose) {

        return true;
    }

    private boolean onEcStartEpoch(final Paxos.EcStartEpoch ecStartEpoch) {
        return true;
    }


    private boolean onEpAborted(final Paxos.EpAborted epAborted) {
        return true;
    }

    private boolean onEpDecide(final Paxos.EpDecide epDecide) {
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
}
