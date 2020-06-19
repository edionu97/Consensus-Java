package consensus.module;

import consensus.Paxos;

import java.util.Optional;

public interface IConsensus {

    /**
     * Initiates the algorithm
     */
    void init();

    /**
     * This method trigger a new event, by sending a message
     * @param message: the message information
     */
    void trigger(final Paxos.Message message);

    /**
     * This method it is used in order to identify the process that sent the message, based on it's port
     * @param message: the network message
     * @return Optional.of(element) if the element is present into the list or optional.empty otherwise
     */
    Optional<Paxos.ProcessId> identifySenderProcessByNetworkMessage(final Paxos.NetworkMessage message);
}
