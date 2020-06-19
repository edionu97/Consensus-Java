package consensus.module;

import consensus.Paxos;
import consensus.algorithms.IAbstractionLayer;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

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
     * Configure the consensus system
     * @param actions: defines a set of configuration actions that need to be set on the consensus object
     */
    void configure(final List<Consumer<IConsensus>> actions);

    /**
     * This method adds an another into the layer list
     * @param layer: the layer that will be added
     */
    void pushLayer(final IAbstractionLayer layer);

    /**
     * Modify the inner process list
     * @param processIds: a list with process id
     */
    void alterProcessList(final List<Paxos.ProcessId> processIds);

    /**
     * @return a string representing the id of the system that stated the consensus algorithm the
     */
    String getSystemId();

    /**
     * This method it is used in order to identify the process that sent the message, based on it's port
     * @param message: the network message
     * @return Optional.of(element) if the element is present into the list or optional.empty otherwise
     */
    Optional<Paxos.ProcessId> identifySenderProcessByNetworkMessage(final Paxos.NetworkMessage message);
}
