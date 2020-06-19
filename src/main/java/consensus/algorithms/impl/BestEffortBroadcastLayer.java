package consensus.algorithms.impl;

import consensus.Paxos;
import consensus.algorithms.abstracts.AbstractLayer;
import consensus.module.IConsensus;

public class BestEffortBroadcastLayer extends AbstractLayer {

    public BestEffortBroadcastLayer(final IConsensus consensus) {
        super(consensus);
    }

    @Override
    public boolean onMessage(Paxos.Message message) {
        return false;
    }
}
