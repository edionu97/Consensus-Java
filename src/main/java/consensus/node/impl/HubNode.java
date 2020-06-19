package consensus.node.impl;

import consensus.Paxos;
import consensus.module.IConsensus;
import consensus.module.impl.ConsensusSystemModule;
import consensus.node.INode;
import utils.messages.MessagesHelper;
import utils.messages.SendHelper;

import java.io.DataInputStream;
import java.net.ServerSocket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HubNode implements INode {

    private final String nodeOwner;
    private final int nodeOwnerIndex;
    private final int nodePort;
    private final int hubPort;
    private final String hubIp;

    private final Map<String, IConsensus> sysIdToConsensus = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    /**
     * Create a hub_node
     *
     * @param owner:      owner of the node
     * @param ownerIndex: the index of the owner
     * @param nodePort:   node's port
     * @param hubIp:      hub's ip
     * @param hubPort:    hub's port
     */
    public HubNode(final String owner,
                   final int ownerIndex,
                   final int nodePort, final String hubIp, final int hubPort) {

        this.nodeOwner = owner;
        this.nodeOwnerIndex = ownerIndex;
        this.nodePort = nodePort;
        this.hubIp = hubIp;
        this.hubPort = hubPort;
    }

    @Override
    @SuppressWarnings("InfiniteLoopStatement")
    public void start() {
        try {
            //create a listening connection on the nodePort
            var socket = new ServerSocket(nodePort);

            //execute the infinitely read on another thread
            executorService.submit(() -> {
                //infinitely read loop
                while (true) {
                    try {
                        //wait until a message is pushed on the network, and get the message stream
                        final var messageByteStream = new DataInputStream(socket.accept().getInputStream());

                        //get the message length
                        final int messageLength = messageByteStream.readInt();
                        if (messageLength <= 0) {
                            continue;
                        }

                        //create a byte array for storing all the message bytes, and store all the message bytes into an array
                        var byteArray = new byte[messageLength];
                        messageByteStream.readFully(byteArray, 0, messageLength);

                        //transform the bytes into a Paxos.Message and process the message
                        processMessage(Paxos.Message.parseFrom(byteArray));
                    } catch (final Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void register() {
        //create the app registration message
        final var registrationMessage = MessagesHelper.createAppRegistrationMessage(nodeOwner, nodeOwnerIndex);
        //send the message to the hub
        SendHelper.sendMessage(registrationMessage, hubIp, hubPort, nodePort);
    }

    /**
     * Process the received receivedMessage
     * If the receivedMessage is app purpose then start a new instance of consensus module, otherwise, if other than the
     * AppPurpose receivedMessage is encountered, the receivedMessage is pushed into the correct queue (it's system queue), based on
     * it's system id
     * @param receivedMessage: the received receivedMessage
     */
    private void processMessage(final Paxos.Message receivedMessage) {

        //get the network receivedMessage
        final var networkMessage =  receivedMessage.getNetworkMessage();
        //get the receivedMessage from the network
        final var innerMessage = networkMessage.getMessage();
        //get the systemId
        final var systemId = receivedMessage.getSystemId();

        //if the receivedMessage is AppPurpose than start a new consensus module
        if (MessagesHelper.isAppPurpose(innerMessage)) {
            onAppPurpose(innerMessage, systemId);
            return;
        }

        //handle other messages, by sending them back into the proper queue
        onMessage(receivedMessage, systemId);
    }

    /**
     * This method is used for handling the AppPurpose receivedMessage
     * @param receivedMessage: the receivedMessage itself
     */
    private void onAppPurpose(final Paxos.Message receivedMessage, final String systemId) {
        //crete a new instance of a consensus system
        var consensusSystemModule = new ConsensusSystemModule(nodePort, hubPort, hubIp, systemId);
        //add it to the map
        sysIdToConsensus.put(systemId, consensusSystemModule);
        //put the receivedMessage into the queue (trigger the action)
        consensusSystemModule.trigger(receivedMessage);
    }


    /**
     * This is a callback for handling all the messages types received by the node, excepting the AppPurpose receivedMessage
     * The messages, should be pushed back into the proper system queue (to the proper consensus system)
     * @param receivedMessage: the receivedMessage
     * @param systemId: the id of the system
     */
    private void onMessage(final Paxos.Message receivedMessage, final String systemId) {

        //get the network receivedMessage
        final var networkMessage =  receivedMessage.getNetworkMessage();
        //get the receivedMessage from the network
        final var innerMessage = networkMessage.getMessage();

        //get the system
        var consSystem = sysIdToConsensus.get(systemId);
        if(consSystem == null) {
            return;
        }

        //get the sender process, and if the process is not found, do nothing
        final var senderProcessOptional = consSystem.identifySenderProcessByNetworkMessage(networkMessage);
        if(senderProcessOptional.isEmpty()){
            return;
        }

        //get the sender process
        final var senderProcess = senderProcessOptional.get();

        //create PL_DELIVER receivedMessage
        final var plDeliverMessage = MessagesHelper
                .createPLDeliverMessage(senderProcess, innerMessage, receivedMessage.getAbstractionId());

        //trigger the plDeliver message so that all the abstractions that listen for PL_DELIVER message type to be informed
        consSystem.trigger(plDeliverMessage);
    }
}
