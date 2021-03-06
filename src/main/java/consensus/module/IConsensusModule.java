package consensus.module;

import consensus.Paxos;
import consensus.algotithms.IAbstractionLayer;

import java.util.List;
import java.util.Optional;

public interface IConsensusModule {
    /**
     * Initiates the algorithm
     */
    void init();

    /**
     * This method trigger a new event, by sending a message
     *
     * @param message: the message information
     */
    void trigger(final Paxos.Message message);


    /**
     * This method adds an another into the layer list
     *
     * @param layer: the layer that will be added
     */
    void pushLayer(final IAbstractionLayer layer);

    /**
     * Modify the inner process list
     *
     * @param processIds: a list with process id
     */
    void alterProcessList(final List<Paxos.ProcessId> processIds);

    /**
     * @return a string representing the id of the system that stated the consensus algorithm the
     */
    String getSystemId();

    /**
     * @return a string representing the hub IP
     */
    String getHubIp();

    /**
     * @return an integer representing the hub port
     */
    int getHubPort();

    /**
     * @return an integer representing the node listener port
     */
    int getNodePort();

    /**
     * @return an instance of a ProcessId representing the PID of the running process
     */
    Paxos.ProcessId getCurrentPID();


    /**
     * @return a list of process
     */
    List<Paxos.ProcessId> getProcessList();

    /**
     * This method it is used in order to identify the process that sent the message, based on it's port
     *
     * @param message: the network message
     * @return Optional.of(element) if the element is present into the list or optional.empty otherwise
     */
    Optional<Paxos.ProcessId> identifySenderProcessByNetworkMessage(final Paxos.NetworkMessage message);


}
