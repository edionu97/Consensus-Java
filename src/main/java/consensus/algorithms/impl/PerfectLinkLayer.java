package consensus.algorithms.impl;

import consensus.Paxos;
import consensus.algorithms.abstracts.AbstractLayer;
import consensus.module.IConsensus;

public class PerfectLinkLayer extends AbstractLayer {

    public PerfectLinkLayer(final IConsensus consensus) {
        super(consensus);
    }

    @Override
    public boolean onMessage(final Paxos.Message message) {
        return false;
    }

}
