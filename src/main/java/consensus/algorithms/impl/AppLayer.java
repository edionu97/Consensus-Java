package consensus.algorithms.impl;

import consensus.Paxos;
import consensus.algorithms.abstracts.AbstractLayer;
import consensus.module.IConsensus;

public class AppLayer extends AbstractLayer {

    public AppLayer(final IConsensus consensus) {
        super(consensus);
    }

    /**
     * This abstraction only listens for APP_PURPOSE and UC_DECIDE
     * @param message: the message that appeared into queue
     * @return true if the message was processed by the abstraction or false otherwise
     */
    @Override
    public boolean onMessage(final Paxos.Message message) {
        switch (message.getType()) {
            case APP_PROPOSE: return onAppPropose(message.getAppPropose());
            case UC_DECIDE: return onUcDecide(message.getUcDecide());
        }
        return false;
    }

    /**
     * Handle appPurpose message
     * @param appPropose: the app purpose
     * @return true
     */
    private boolean onAppPropose(final Paxos.AppPropose appPropose) {
        //get the process list
        final var processList = appPropose.getProcessesList();

        return true;
    }


    private boolean onUcDecide(final Paxos.UcDecide ucDecide) {
        return true;
    }


}
