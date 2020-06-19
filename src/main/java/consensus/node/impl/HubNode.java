package consensus.node.impl;

import consensus.Paxos;
import consensus.node.INode;
import utils.messages.MessagesHelper;
import utils.messages.SendHelper;

import java.io.DataInputStream;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static consensus.Paxos.Message.Type.APP_REGISTRATION;

public class HubNode implements INode {
    private final String nodeOwner;
    private final int nodeOwnerIndex;
    private final int nodePort;
    private final int hubPort;
    private final String hubIp;

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

                //infinitely read
                while (true) {
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

    private void processMessage(final Paxos.Message message) {


    }



}
