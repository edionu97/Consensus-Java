package consensus.algorithms.impl;

import consensus.Paxos;
import consensus.algorithms.abstracts.AbstractLayer;
import consensus.module.IConsensus;

public class EventualLeaderDetectorLayer extends AbstractLayer {

    public EventualLeaderDetectorLayer(final IConsensus consensus) {
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
