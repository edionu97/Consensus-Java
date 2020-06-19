package utils.messages;

import consensus.Paxos;

import static consensus.Paxos.Message.Type.APP_REGISTRATION;

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
}
