package consensus.algorithms.impl;

import consensus.Paxos;
import consensus.algorithms.abstracts.AbstractLayer;
import consensus.module.IConsensus;

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
public class EpochConsensusLayer extends AbstractLayer {

    protected EpochConsensusLayer(final IConsensus consensus, final int ets, Paxos.EpState_ epState) {
        super(consensus);
    }

    @Override
    protected void init() {

    }

    @Override
    public boolean onMessage(final Paxos.Message message) {
        return false;
    }
}
