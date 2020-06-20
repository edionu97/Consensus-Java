package consensus.algorithms.impl;

import consensus.Paxos;
import consensus.algorithms.abstracts.AbstractAbstraction;
import consensus.module.IConsensus;
import utils.messages.MessagesHelper;
import utils.messages.ProcessHelper;


/**
 * An epoch change abstraction (leader based) that signals the start of a new epoch when
 * a leader is suspected. Every process maintains a timestamp lastTs (last epoch that it started)
 * and a timestamp ts (last epoch that it attempted to start with itself as a leader).
 * Initially, the process sets ts to its rank. Whenever the leader detector subsequently makes p
 * trust itself, p adds N to ts and sends a NEWEPOCH message with ts.
 */
public class EpochChangeAbstraction extends AbstractAbstraction {

    private Paxos.ProcessId trusted;
    private int lastTs;
    private int ts;

    public EpochChangeAbstraction(final IConsensus consensus) {
        super(consensus);
    }

    @Override
    protected void init() {
        abstractionId = "ec";
        lastTs = 0;
        ts = consensus.getCurrentPID().getRank();
        trusted = ProcessHelper.getMinRankProcess(consensus.getProcess());
    }

    /**
     * Handle only the ELD_TRUST, BEB_DELIVER, PL_DELIVER messages
     *
     * @param message: the message that appeared into queue
     * @return true if the message was handled or false otherwise
     */
    @Override
    public boolean onMessage(final Paxos.Message message) {

        switch (message.getType()) {
            case ELD_TRUST:
                return onEldTrust(message.getEldTrust());
            case BEB_DELIVER:
                return onBebDeliver(message.getBebDeliver());
            case PL_DELIVER:
                return onPlDeliver(message.getPlDeliver());
        }

        return false;
    }

    /**
     * When the leader detector makes p trust itself, p increases the ts with the number of processes
     * Then it sends the new ts into a NEWEPOCH message
     *
     * @param eldTrust: the eldTrust message
     * @return true
     */
    private boolean onEldTrust(final Paxos.EldTrust eldTrust) {
        //set the trusted process
        this.trusted = eldTrust.getProcess();

        //if the processes are not equal than do nothing
        if (eldTrust.getProcess().getPort() != consensus.getCurrentPID().getPort()) {
            return true;
        }

        //increase the ts with the number of processes
        this.ts += consensus.getProcess().size();

        //create a new epoch message
        final var ecNewEpochMessage = MessagesHelper.createEcNewEpoch(abstractionId, this.ts);

        
        //put the message into queue
        consensus.trigger(ecNewEpochMessage);
        return true;
    }

    /**
     * When process p receives a message (NEWEPOCH message) with a parameter that has newts greater than lastTs
     * from a process l and p has set most recently trusted set to l, then the process triggers a message of type
     * StartEpoch  with parameters newts and l.
     * <p>
     * If the condition is not satisfied, the process informs that the new epoch could not be started, through a NACK
     * message
     *
     * @param bebDeliver the message
     * @return true if the message could be handled or false otherwise
     */
    private boolean onBebDeliver(final Paxos.BebDeliver bebDeliver) {
        //we could not handle the message, because it is not NEWEPOCH message
        if (!bebDeliver.getMessage().getType().equals(Paxos.Message.Type.EC_NEW_EPOCH_)) {
            return false;
        }

        //get the timestamp
        final int newTs = bebDeliver.getMessage().getEcNewEpoch().getTimestamp();

        //get the l process
        final var l = bebDeliver.getSender();

        //if the condition is respected
        if (newTs > lastTs && l.getPort() == trusted.getPort()) {
            this.lastTs = newTs;
            //create an ecStartEpochMessage
            final var ecStartEpochMessage = MessagesHelper.
                    createEcStartEpochMessage(abstractionId, newTs, l);
            //put the message into queue
            consensus.trigger(ecStartEpochMessage);
            return true;
        }

        //create he nack message
        final var nackMessage = MessagesHelper.createEcNackMessage(abstractionId, l);
        //put the message into queue
        consensus.trigger(nackMessage);
        return true;
    }

    /**
     * When a process receives a NACK message and if it trusts itself, the ts will be incremented with the number of
     * total processes and it sends a message for starting a new epoch (NEWEPOCH message)
     *
     * @param plDeliver: the plDeliver
     * @return true if the message can be handled or false otherwise
     */
    private boolean onPlDeliver(final Paxos.PlDeliver plDeliver) {
        //check if the message could not be handled
        if (!plDeliver.getMessage().getType().equals(Paxos.Message.Type.EC_NACK_)) {
            return false;
        }

        //check if ports are the not the same
        if (trusted.getPort() != consensus.getCurrentPID().getPort()) {
            return true;
        }

        //increase the ts
        this.ts += consensus.getProcess().size();

        //create the
        final var ecNewEpochMessage = MessagesHelper.createEcNewEpoch(abstractionId, ts);

        //put the message in queue
        consensus.trigger(ecNewEpochMessage);
        return true;
    }


}
