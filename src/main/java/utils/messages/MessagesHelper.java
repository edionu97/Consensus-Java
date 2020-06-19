package utils.messages;

import consensus.Paxos;

import static consensus.Paxos.Message.Type.*;

public class MessagesHelper {

    /**
     * Create an appRegistration message
     * @param nodeOwner: the owner of the node
     * @param nodeOwnerIndex: the index of the owner node
     * @return a new instance of Paxos.Message
     */
    public static Paxos.Message createAppRegistrationMessage(final String nodeOwner, final int nodeOwnerIndex) {
        return Paxos.Message.newBuilder()
                .setType(APP_REGISTRATION)
                .setAppRegistration(Paxos.AppRegistration.newBuilder()
                        .setOwner(nodeOwner)
                        .setIndex(nodeOwnerIndex)
                        .build())
                .build();
    }

    /**
     * This method creates a PL_DELIVER message
     * @param sender: the sender process id
     * @param message: the message itself
     * @param abstractionId: the abstractionId
     * @return a configured PL_DELIVER Paxos.Message
     */
    public static Paxos.Message createPLDeliverMessage(final Paxos.ProcessId sender,
                                                       final Paxos.Message message, final String abstractionId) {
        return  Paxos.Message.newBuilder()
                .setType(PL_DELIVER)
                .setAbstractionId(abstractionId)
                .setPlDeliver(Paxos.PlDeliver.newBuilder()
                        .setMessage(message)
                        .setSender(sender)
                        .build())
                .build();
    }

    /**
     * Check if the message is app purpose
     * @param message: Paxos.Message
     * @return true if type of the message is APP_PURPOSE of false otherwise
     */
    public static boolean isAppPurpose(final Paxos.Message message) {
        return message.getType().equals(APP_PROPOSE);
    }
}
