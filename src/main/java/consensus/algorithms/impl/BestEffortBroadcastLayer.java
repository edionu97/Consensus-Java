package consensus.algorithms.impl;

import consensus.Paxos;
import consensus.algorithms.abstracts.AbstractLayer;
import consensus.module.IConsensus;
import utils.messages.MessagesHelper;

/**
 * A broadcast abstraction that enables a process to send a message, in a one-shot operation,
 * to all processes in a system, including itself.
 * Messages are unique, that is, no process ever broadcasts the same message
 * twice and furthermore, no two processes ever broadcast the same message.
 */
public class BestEffortBroadcastLayer extends AbstractLayer {

    public BestEffortBroadcastLayer(final IConsensus consensus) {
        super(consensus);
    }

    @Override
    protected void init() {
        abstractionId = "beb";
    }

    /**
     * This algorithm listens only for beb broadcast and plDeliver messages
     * @param message: the message that appeared into queue
     * @return true if the message was handled
     */
    @Override
    public boolean onMessage(final Paxos.Message message) {
        switch (message.getType()) {
            case BEB_BROADCAST:
                return onBebBroadcast(message.getBebBroadcast());
            case PL_DELIVER:
                return onPlDeliver(message.getPlDeliver());
        }
        return false;
    }

    /**
     * On the broadcast message we need to broadcast the message to every single existing process
     * (including current process)
     * @param bebBroadcast: the broadcast
     * @return true
     */
    private boolean onBebBroadcast(final Paxos.BebBroadcast bebBroadcast) {
        //iterate through all the processes
        consensus.getProcess().forEach(processId -> {
            //create the beb message
            final var bebMessage = MessagesHelper
                    .createPlSendMessage(abstractionId, processId, bebBroadcast.getMessage());
            //put the message into queue
            consensus.trigger(bebMessage);
        });

        //mark the request as handled
        return true;
    }

    /**
     * Handle the PL_Deliver message
     * @param plDeliver: the message
     * @return true
     */
    private boolean onPlDeliver(final Paxos.PlDeliver plDeliver) {
        //create the bebDeliver message
        final var bebDeliverMessage = MessagesHelper.createBebDeliver(abstractionId,
                plDeliver.getSender(), plDeliver.getMessage());

        //put the message into queue
        consensus.trigger(bebDeliverMessage);
        return true;
    }
}
