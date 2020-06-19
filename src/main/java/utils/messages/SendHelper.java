package utils.messages;

import consensus.Paxos;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;

import static consensus.Paxos.Message.Type.NETWORK_MESSAGE;
import static consensus.Paxos.Message.Type.PL_SEND;

public class SendHelper {

    /**
     * sends a message to the given destination, converting it into byte array
     * @param message: the message that will be send over the network
     * @param destinationAddress: the message destination address
     * @param destinationPort: the message destination port
     * @param nodePort: the node port (the sender listening port)
     */
    public static void sendMessage(final Paxos.Message message,
                                   final String destinationAddress,
                                   final int destinationPort, final int nodePort) {
        try(var socket = new Socket(destinationAddress, destinationPort)) {
            var outputStream = socket.getOutputStream();
            outputStream.write(sentMessageToBytes(message, nodePort));
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This is a helper method, that converts the message from object into byte array, and sends it over the network
     * Firstly in the array will be the length of the message (an integer) and after that the message itself
     * @param message: the message that needs to be send over the network
     * @param nodePort: the port on witch the sender listens for messages
     * @return a byte array
     */
    private static byte[] sentMessageToBytes(final Paxos.Message message, final int nodePort) {
        final var sentMessage = Paxos.Message.newBuilder()
                .setType(NETWORK_MESSAGE)
                .setNetworkMessage(Paxos.NetworkMessage.newBuilder()
                        .setMessage(PL_SEND.equals(message.getType()) ? message.getPlSend().getMessage() : message)
                        .setSenderHost(PL_SEND.equals(message.getType()) ? message.getPlSend().getDestination().getHost() : "")
                        .setSenderListeningPort(nodePort)
                        .build())
                .setAbstractionId(message.getAbstractionId())
                .setSystemId(message.getSystemId())
                .build();
        final var messageBytes = sentMessage.toByteArray();
        return ByteBuffer.allocate(Integer.BYTES + messageBytes.length).putInt(messageBytes.length).put(messageBytes).array();
    }



}
