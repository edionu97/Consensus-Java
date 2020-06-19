package consensus.algorithms.impl;

import consensus.Paxos;
import consensus.algorithms.IAbstractionLayer;

public class AbstractionLayer implements IAbstractionLayer {

    @Override
    public boolean onMessage(final Paxos.Message message) {
        return false;
    }
}
