package consensus.module.impl;

import consensus.Paxos;
import consensus.algotithms.IAbstractionLayer;
import consensus.module.IConsensusModule;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConsensusSystemModule implements IConsensusModule {

    private final int nodePort;
    private final int hubPort;
    private final String hubIp;
    private final String systemId;
    private Paxos.ProcessId currentProcessId;

    private final List<Paxos.ProcessId> processList = new CopyOnWriteArrayList<>();
    private final List<IAbstractionLayer> abstractionList = new CopyOnWriteArrayList<>();
    private final List<Paxos.Message> messageQueue = new CopyOnWriteArrayList<>();

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public ConsensusSystemModule(final int hubPort,
                                 final int nodePort, final String hubIp, final String systemId) {

        this.nodePort = nodePort;
        this.hubIp = hubIp;
        this.hubPort = hubPort;
        this.systemId = systemId;
    }

    @SuppressWarnings("InfiniteLoopStatement")
    public void init() {
        //run the function on another thread
        executorService.execute(() -> {
            //infinite loop
            while (true) {
                //iterate through message list and check what messages can be processed
                for (int messageIndex = 0; !messageQueue.isEmpty() && messageIndex < messageQueue.size(); ++messageIndex) {
                    //process the messages
                    var wasProcessed = false;
                    for (final var abstraction : abstractionList) {
                        //check to see if the message can be processed by an abstraction layer
                        if (abstraction.onMessage(messageQueue.get(messageIndex))) {
                            wasProcessed = true;
                        }
                    }
                    //if the message was handled that remove it from the queue
                    if (wasProcessed) {
                        messageQueue.remove(messageIndex);
                        break;
                    }
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
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
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public void alterProcessList(final List<Paxos.ProcessId> processesList) {
        processList.addAll(processesList);
        currentProcessId = getCurrentProcessPid().get();
    }


    @Override
    public void pushLayer(final IAbstractionLayer abstractionLayer) {
        abstractionList.add(abstractionLayer);
    }

    @Override
    public int getNodePort() {
        return nodePort;
    }

    @Override
    public int getHubPort() {
        return hubPort;
    }

    @Override
    public Paxos.ProcessId getCurrentPID() {
        return currentProcessId;
    }

    @Override
    public List<Paxos.ProcessId> getProcessList() {
        return processList;
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
    public Optional<Paxos.ProcessId> identifySenderProcessByNetworkMessage(final Paxos.NetworkMessage networkMessage) {
        //get the process that sent the network message
        return processList
                .stream()
                .filter(processId -> processId.getPort() == networkMessage.getSenderListeningPort())
                .findFirst();
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
