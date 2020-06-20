package consensus.module.impl;

import consensus.Paxos;
import consensus.algorithms.IAbstractionLayer;
import consensus.module.IConsensus;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class ConsensusSystemModule implements IConsensus {

    private int hubPort;
    private int nodePort;
    private String hubIp;
    private final String systemId;
    private Paxos.ProcessId currentProcessPid;

    private final List<Paxos.ProcessId> processList = new CopyOnWriteArrayList<>();
    private final List<IAbstractionLayer> abstractionLayers = new CopyOnWriteArrayList<>();
    private final List<Paxos.Message> messageQueue = new CopyOnWriteArrayList<>();

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public ConsensusSystemModule(final int hubPort,
                                 final int nodePort, final String hubIp, final String systemId) {
        this.hubPort = hubPort;
        this.nodePort = nodePort;
        this.hubIp = hubIp;
        this.systemId = systemId;
    }

    @Override
    @SuppressWarnings("InfiniteLoopStatement")
    public void init() {
        //run the function on another thread
        executorService.submit(() -> {
            //infinite loop
            while (true) {
                //iterate through message list and check what messages can be processed
                for (int messageIdx = 0; messageIdx < messageQueue.size(); ++messageIdx) {
                    //check to see if the message can be processed by an abstraction layer
                    if (!isHandled(messageQueue.get(messageIdx))) {
                        continue;
                    }
                    //remove the message from the queue because it has been handled
                    messageQueue.remove(messageIdx);
                    break;
                }
                //sleep for some time
                Thread.sleep(15);
            }
        });
    }

    @Override
    public void trigger(final Paxos.Message message) {
        //add the message into queue, and set the systemId accordingly
        messageQueue.add(message
                .toBuilder()
                .setSystemId(systemId)
                .build()
        );
    }

    @Override
    public void configure(final List<Consumer<IConsensus>> configurationActions) {
        if (configurationActions == null) {
            return;
        }
        configurationActions.forEach(action -> action.accept(this));
    }

    @Override
    public void pushLayer(final IAbstractionLayer layer) {
        abstractionLayers.add(layer);
    }

    @Override
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public void alterProcessList(final List<Paxos.ProcessId> processIds) {
        processList.addAll(processIds);
        currentProcessPid = getCurrentProcessPid().get();
    }

    @Override
    public String getSystemId() {
        return systemId;
    }

    @Override
    public String getHubIp() {
        return hubIp;
    }

    @Override
    public int getHubPort() {
        return hubPort;
    }

    @Override
    public int getNodePort() {
        return nodePort;
    }

    @Override
    public Paxos.ProcessId getCurrentPID() {
        return currentProcessPid;
    }

    @Override
    public List<Paxos.ProcessId> getProcess() {
        return processList;
    }

    @Override
    public Optional<Paxos.ProcessId> identifySenderProcessByNetworkMessage(final Paxos.NetworkMessage networkMessage) {
        //get the process that sent the network message
        return processList
                .stream()
                .filter(processId -> processId.getPort() == networkMessage.getSenderListeningPort())
                .findFirst();
    }

    /**
     * This method iterates through all the abstraction list to see if exist at least one abstraction that can process
     * the given message
     *
     * @param message: the message that needs to be processed
     * @return true if there exist at least one abstraction that could handle the message or false otherwise
     */
    private boolean isHandled(final Paxos.Message message) {
        //assume that the message was processed by no abstraction
        var messageWasProcessed = false;
        //check into all the abstraction to see if one of them could handle the message
        for (final var abstraction : abstractionLayers) {
            messageWasProcessed = messageWasProcessed || abstraction.onMessage(message);
        }
        //return the result
        return messageWasProcessed;
    }

    /**
     * @return the process that have the same port with the nodePort aka the process into that the current system runs
     */
    private Optional<Paxos.ProcessId> getCurrentProcessPid() {
        return processList
                .stream()
                .filter(process -> process.getPort() == nodePort)
                .findFirst();
    }
}
