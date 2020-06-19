package consensus.algorithms;

import consensus.Paxos;

public interface IAbstractionLayer {

    /**
     * This method is implemented by all the existing abstractions, and it handles the messages
     * @param message: the message that appeared into queue
     * @return true if the message was handled or false otherwise
     */
    boolean onMessage(final Paxos.Message message);
}
