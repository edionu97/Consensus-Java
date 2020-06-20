package consensus.algorithms.abstracts;

import consensus.algorithms.IAbstractionLayer;
import consensus.module.IConsensus;

public abstract class AbstractAbstraction implements IAbstractionLayer {

    protected String abstractionId;
    protected final IConsensus consensus;

    protected AbstractAbstraction(final IConsensus consensus) {
        this.consensus = consensus;
        init();
    }

    protected abstract void init();
}
