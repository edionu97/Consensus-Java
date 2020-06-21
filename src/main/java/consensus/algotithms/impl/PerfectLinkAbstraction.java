package consensus.algotithms.impl;

import consensus.algotithms.abstracts.AbstractAbstractionLayer;
import consensus.module.IConsensusModule;
import utils.messages.SendHelper;

import static consensus.Paxos.Message;
import static consensus.Paxos.Message.Type.PL_SEND;

public class PerfectLinkAbstraction extends AbstractAbstractionLayer {

    public PerfectLinkAbstraction(final IConsensusModule consensus) {
        super(consensus);
    }

    @Override
    protected void init() {
        //does nothing on init
    }

    /**
     * This method handles only the PL_SEND message types
     * @param message: the message that appeared into queue
     * @return true if the message was handled or false otherwise
     */
    @Override
    public boolean onMessage(Message message) {
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
