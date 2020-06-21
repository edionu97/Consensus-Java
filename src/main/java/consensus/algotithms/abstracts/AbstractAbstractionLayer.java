package consensus.algotithms.abstracts;

import consensus.algotithms.IAbstractionLayer;
import consensus.module.IConsensusModule;

public abstract class AbstractAbstractionLayer implements IAbstractionLayer {

    protected String abstractionId;
    protected final IConsensusModule consensus;

    protected AbstractAbstractionLayer(final IConsensusModule consensus) {
        this.consensus = consensus;
        init();
    }

    protected abstract void init();
}
