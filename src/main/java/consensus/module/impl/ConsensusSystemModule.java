package consensus.module.impl;

import consensus.Paxos;
import consensus.algorithms.IAbstractionLayer;
import consensus.module.IConsensus;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConsensusSystemModule implements IConsensus {

    private final int nodePort;
    private final int hubPort;
    private final String hubIp;
    private final String systemId;

    private final List<Paxos.ProcessId> processList = new CopyOnWriteArrayList<>();
    private final List<IAbstractionLayer> abstractionLayers = new CopyOnWriteArrayList<>();
    private final List<Paxos.Message> messageQueue = new CopyOnWriteArrayList<>();

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public ConsensusSystemModule(final int nodePort,
                                 final int hubPort, final String hubIp, final String systemId) {
        this.nodePort = nodePort;
        this.hubPort = hubPort;
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
}
