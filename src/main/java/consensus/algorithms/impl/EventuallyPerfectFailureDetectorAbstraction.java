package consensus.algorithms.impl;

import consensus.Paxos;
import consensus.algorithms.abstracts.AbstractAbstraction;
import consensus.module.IConsensus;

import java.util.List;

public class EventuallyPerfectFailureDetectorAbstraction extends AbstractAbstraction {

    private List<Paxos.ProcessId> alive;

    public EventuallyPerfectFailureDetectorAbstraction(final IConsensus consensus) {
        super(consensus);
    }

    @Override
    protected void init() {
        this.alive = consensus.getProcess();
    }

    @Override
    public boolean onMessage(final Paxos.Message message) {
        return false;
    }
}
