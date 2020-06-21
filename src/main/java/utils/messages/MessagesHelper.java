package utils.messages;

import consensus.Paxos;

import static consensus.Paxos.Message.Type.*;

public class MessagesHelper {

    /**
     * Create an appRegistration message
     *
     * @param nodeOwner:      the owner of the node
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
     *
     * @param sender:        the sender process id
     * @param message:       the message itself
     * @param abstractionId: the abstractionId
     * @return a configured PL_DELIVER Paxos.Message
     */
    public static Paxos.Message createPLDeliverMessage(final Paxos.ProcessId sender,
                                                       final Paxos.Message message, final String abstractionId) {
        return Paxos.Message.newBuilder()
                .setType(PL_DELIVER)
                .setAbstractionId(abstractionId)
                .setPlDeliver(Paxos.PlDeliver.newBuilder()
                        .setMessage(message)
                        .setSender(sender)
                        .build())
                .build();
    }

    /**
     * Create the EpStateMessage
     *
     * @param valueTimestamp:  the timestamp of the value
     * @param messageReceiver: the receiver of the message
     * @param value:           the value of the value
     * @return a fully configured instance of the EpStateMessage
     */
    public static Paxos.Message createEpStateMessage(final int valueTimestamp,
                                                     final Paxos.ProcessId messageReceiver, final Paxos.Value value) {

        //create the epState message
        final var epStateMessage = Paxos.Message.newBuilder()
                .setType(EP_STATE_)
                .setEpState(Paxos.EpState_.newBuilder()
                        .setValueTimestamp(valueTimestamp)
                        .setValue(value)
                        .build())
                .build();

        //pLSend message
        return Paxos.Message.newBuilder()
                .setType(PL_SEND)
                .setPlSend(Paxos.PlSend.newBuilder()
                        .setDestination(messageReceiver)
                        .setMessage(epStateMessage)
                        .build())
                .build();
    }

    /**
     * Create the EpAcceptMessage
     *
     * @param destination: the destination of the message
     * @return a fully configured EP_Accept message
     */
    public static Paxos.Message createEpAcceptMessage(final Paxos.ProcessId destination) {
        return Paxos.Message.newBuilder()
                .setType(PL_SEND)
                .setPlSend(Paxos.PlSend.newBuilder()
                        .setDestination(destination)
                        .setMessage(Paxos.Message.newBuilder()
                                .setType(EP_ACCEPT_)
                                .setEpAccept(Paxos.EpAccept_.newBuilder()
                                        .build())
                                .build())
                        .build())
                .build();
    }

    /**
     * This method is used for creating EpPurposeRead message
     *
     * @return a fully configured EpReadMessage
     */
    public static Paxos.Message createEpPurposeReadMessage() {
        return Paxos.Message.newBuilder()
                .setType(BEB_BROADCAST)
                .setBebBroadcast(Paxos.BebBroadcast.newBuilder()
                        .setMessage(Paxos.Message.newBuilder()
                                .setType(EP_READ_)
                                .setEpRead(Paxos.EpRead_.newBuilder()
                                        .build())
                                .build())
                        .build())
                .build();
    }

    /**
     * Create ep decide message
     *
     * @param timestamp:    the timestamp of the message
     * @param decidedValue: the agreed value
     * @return a fully configured Ep_Decide message
     */
    public static Paxos.Message createEpDecideMessage(final int timestamp, final Paxos.Value decidedValue) {
        return Paxos.Message.newBuilder()
                .setType(EP_DECIDE)
                .setEpDecide(Paxos.EpDecide.newBuilder()
                        .setEts(timestamp)
                        .setValue(decidedValue)
                        .build())
                .build();
    }

    /**
     * Create the EP_WRITE message
     *
     * @param value: the value that needs to be written
     * @return a fully configured message
     */
    public static Paxos.Message createEpWriteMessage(final Paxos.Value value) {
        return Paxos.Message.newBuilder()
                .setType(BEB_BROADCAST)
                .setBebBroadcast(Paxos.BebBroadcast.newBuilder()
                        .setMessage(Paxos.Message.newBuilder()
                                .setType(EP_WRITE_)
                                .setEpWrite(Paxos.EpWrite_.newBuilder()
                                        .setValue(value)
                                        .build())
                                .build())
                        .build())
                .build();
    }

    /**
     * Create an EpDecidedMessage
     *
     * @param decidedValue: the value on which the process is decided
     * @return a fully configured value
     */
    public static Paxos.Message createEpDecidedMessage(final Paxos.Value decidedValue) {
        return Paxos.Message.newBuilder()
                .setType(BEB_BROADCAST)
                .setBebBroadcast(Paxos.BebBroadcast.newBuilder()
                        .setMessage(Paxos.Message.newBuilder()
                                .setType(EP_DECIDED_)
                                .setEpDecided(Paxos.EpDecided_.newBuilder()
                                        .setValue(decidedValue)
                                        .build())
                                .build())
                        .build())
                .build();
    }

    /**
     * Create the EpAbortedMessage
     *
     * @param ets:       the timestamp (epoch stating timestamp)
     * @param timestamp: the state value timestamp
     * @param value:     the state value
     * @return a fully configured EpAbortedMessage
     */
    public static Paxos.Message createEpAbortedMessage(final int ets, final int timestamp, final Paxos.Value value) {
        return Paxos.Message.newBuilder()
                .setType(EP_ABORTED)
                .setEpAborted(Paxos.EpAborted.newBuilder()
                        .setEts(ets)
                        .setValueTimestamp(timestamp)
                        .setValue(value)
                        .build())
                .build();
    }

    /**
     * Crates the UcPurposeMessage
     *
     * @param appPropose: the app purpose message
     * @return a configured UcPurpose message
     */
    public static Paxos.Message createUcProposeMessage(final Paxos.AppPropose appPropose) {
        return Paxos.Message.newBuilder()
                .setType(UC_PROPOSE)
                .setUcPropose(Paxos.UcPropose.newBuilder()
                        .setValue(appPropose.getValue())
                        .build())
                .build();
    }

    /**
     * Create and configure an app decide message
     *
     * @param systemId: the id of the system
     * @param ucDecide: the uc decide
     * @return a properly configured AppDecide message
     */
    public static Paxos.Message createAppDecideMessage(final String systemId, final Paxos.UcDecide ucDecide) {
        return Paxos.Message.newBuilder()
                .setType(APP_DECIDE)
                .setSystemId(systemId)
                .setAppDecide(Paxos.AppDecide.newBuilder()
                        .setValue(ucDecide.getValue())
                        .build())
                .build();
    }

    /**
     * Create an instance of EP_PROPOSE message
     *
     * @param value: the value that will be send into the message as proposed value
     * @return a fully configured EP_PROPOSE message
     */
    public static Paxos.Message createEpProposeMessage(final Paxos.Value value) {
        return Paxos.Message.newBuilder()
                .setType(EP_PROPOSE)
                .setEpPropose(Paxos.EpPropose.newBuilder()
                        .setValue(value)
                        .build())
                .build();
    }

    /**
     * Crete an instance of EP_ABORT message
     *
     * @return a fully configured instance of epAbortMessage
     */
    public static Paxos.Message createEpAbortMessage() {
        return Paxos.Message.newBuilder()
                .setType(EP_ABORT)
                .setEpAbort(Paxos.EpAbort.newBuilder()
                        .build())
                .build();
    }

    /**
     * Create the UC_DECIDE message
     *
     * @param value: the decided value
     * @return a fully configured UC_DECIDE message
     */
    public static Paxos.Message createUcDecideMessage(final Paxos.Value value) {
        return Paxos.Message.newBuilder()
                .setType(UC_DECIDE)
                .setUcDecide(Paxos.UcDecide
                        .newBuilder()
                        .setValue(value).build())
                .build();
    }

    /**
     * Create a beb broadcast message
     *
     * @param abstractionId:  the abstraction's identifier
     * @param destinationPid: the destination process (the process that will receive the information)
     * @param msg:            the message that will be sent
     * @return a fully configured beb message
     */
    public static Paxos.Message createPlSendMessage(final String abstractionId,
                                                    final Paxos.ProcessId destinationPid, final Paxos.Message msg) {
        return Paxos.Message.newBuilder()
                .setType(PL_SEND)
                .setAbstractionId(abstractionId)
                .setPlSend(Paxos.PlSend.newBuilder()
                        .setDestination(destinationPid)
                        .setMessage(msg)
                        .build())
                .build();
    }

    /**
     * Create beb deliver message
     *
     * @param bebIdentifier: the beb deliver abstraction's ID
     * @param sender:        the process that send the message
     * @param message:       the message itself
     * @return a fully configured bebDeliverMessage
     */
    public static Paxos.Message createBebDeliver(final String bebIdentifier,
                                                 final Paxos.ProcessId sender, final Paxos.Message message) {
        return Paxos.Message.newBuilder()
                .setType(BEB_DELIVER)
                .setAbstractionId(bebIdentifier)
                .setBebDeliver(Paxos.BebDeliver.newBuilder()
                        .setSender(sender)
                        .setMessage(message)
                        .build())
                .build();
    }

    /**
     * Create an EcNewEpoch message
     *
     * @param abstractionId: the id of the abstraction
     * @param timestamp:     the value of the timestamp
     * @return a fully configured EC_New_Message
     */
    public static Paxos.Message createEcNewEpoch(final String abstractionId, final int timestamp) {
        return Paxos.Message.newBuilder()
                .setType(BEB_BROADCAST)
                .setBebBroadcast(Paxos.BebBroadcast.newBuilder()
                        .setMessage(Paxos.Message.newBuilder()
                                .setAbstractionId(abstractionId)
                                .setType(EC_NEW_EPOCH_)
                                .setEcNewEpoch(Paxos.EcNewEpoch_.newBuilder()
                                        .setTimestamp(timestamp)
                                        .build())
                                .build())
                        .build())
                .build();
    }

    /**
     * Creates a new exStartEpochMessage
     *
     * @param abstractionId: the abstraction's identifier
     * @param timestamp:     the new value of the timestamp
     * @param leader:        the new leader
     * @return a fully configured EcStartEpochMessage
     */
    public static Paxos.Message createEcStartEpochMessage(final String abstractionId,
                                                          final int timestamp, final Paxos.ProcessId leader) {
        return Paxos.Message.newBuilder()
                .setAbstractionId(abstractionId)
                .setType(EC_START_EPOCH)
                .setEcStartEpoch(Paxos.EcStartEpoch.newBuilder()
                        .setNewTimestamp(timestamp)
                        .setNewLeader(leader)
                        .build())
                .build();
    }

    /**
     * Create a nack message
     *
     * @param abstractionId: the abstraction's id
     * @param destination:   the destination process
     * @return a fully configured EcNackMessage
     */
    public static Paxos.Message createEcNackMessage(final String abstractionId, final Paxos.ProcessId destination) {
        return Paxos.Message.newBuilder()
                .setType(PL_SEND)
                .setPlSend(Paxos.PlSend.newBuilder()
                        .setDestination(destination)
                        .setMessage(Paxos.Message.newBuilder()
                                .setAbstractionId(abstractionId)
                                .setType(EC_NACK_)
                                .setEcNack(Paxos.EcNack_.newBuilder()
                                        .build())
                                .build())
                        .build())
                .build();
    }

    /**
     * Create a ec new epoch message
     * @param abstractionId: the abstraction's id
     * @param timestamp: the timestamp
     * @return a fully configured EC_NEW_EPOCH message
     */
    public static Paxos.Message createEcNewEpochMessage(final String abstractionId, final int timestamp){
        return Paxos.Message.newBuilder()
                .setType(BEB_BROADCAST)
                .setBebBroadcast(Paxos.BebBroadcast.newBuilder()
                        .setMessage(Paxos.Message.newBuilder()
                                .setAbstractionId(abstractionId)
                                .setType(EC_NEW_EPOCH_)
                                .setEcNewEpoch(Paxos.EcNewEpoch_.newBuilder()
                                        .setTimestamp(timestamp)
                                        .build())
                                .build())
                        .build())
                .build();
    }

    /**
     * Create an ELD_Trust message
     * @param process: the process
     * @return a fully configured instance of the message
     */
    public static Paxos.Message createEldTrustMessage(final Paxos.ProcessId process) {
        return Paxos.Message.newBuilder()
                .setType(ELD_TRUST)
                .setEldTrust(Paxos.EldTrust.newBuilder()
                        .setProcess(process)
                        .build())
                .build();
    }

    /**
     * Check if the message is app purpose
     *
     * @param message: Paxos.Message
     * @return true if type of the message is APP_PURPOSE of false otherwise
     */
    public static boolean isAppPurpose(final Paxos.Message message) {
        return message.getType().equals(APP_PROPOSE);
    }
}
