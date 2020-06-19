package consensus.algorithms.impl;

import consensus.Paxos;
import consensus.algorithms.abstracts.AbstractLayer;
import consensus.module.IConsensus;
import utils.messages.SendHelper;

import static consensus.Paxos.Message.Type.PL_SEND;

public class PerfectLinkLayer extends AbstractLayer {

    public PerfectLinkLayer(final IConsensus consensus) {
        super(consensus);
    }

    /**
     * This method handles only the PL_SEND message types
     * @param message: the message that appeared into queue
     * @return true if the message was handled or false otherwise
     */
    @Override
    public boolean onMessage(final Paxos.Message message) {

        //if the message type is not PL_SEND than ignore the message
        if (!PL_SEND.equals(message.getType())) {
            return false;
        }

        //get the destination message
        final var destinationProcess = message.getPlSend().getDestination();

        //send the message through tcp network
        SendHelper.sendMessage(message, destinationProcess.getHost(), destinationProcess.getPort(), consensus.getNodePort());
        return true;
    }

}
