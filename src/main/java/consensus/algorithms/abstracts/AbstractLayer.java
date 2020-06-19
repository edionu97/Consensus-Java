package consensus.algorithms.abstracts;

import consensus.algorithms.IAbstractionLayer;
import consensus.module.IConsensus;

public abstract class AbstractLayer implements IAbstractionLayer {

    protected final IConsensus consensus;

    protected AbstractLayer(final IConsensus consensus) {
        this.consensus = consensus;
        init();
    }

    protected abstract void init();
}
