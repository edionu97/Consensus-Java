package consensus.algotithms;

import consensus.Paxos;

public interface IAbstractionLayer {
    boolean onMessage(Paxos.Message message);
}
