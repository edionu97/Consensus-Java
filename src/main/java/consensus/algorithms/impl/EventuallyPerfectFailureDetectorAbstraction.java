package consensus.algorithms.impl;

import consensus.Paxos;
import consensus.algorithms.abstracts.AbstractAbstraction;
import consensus.module.IConsensus;

public class EventuallyPerfectFailureDetectorAbstraction extends AbstractAbstraction {

    public EventuallyPerfectFailureDetectorAbstraction(final IConsensus consensus) {
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
