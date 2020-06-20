package consensus.algorithms.impl;

import consensus.Paxos;
import consensus.algorithms.abstracts.AbstractAbstraction;
import consensus.module.IConsensus;
import utils.messages.MessagesHelper;
import utils.messages.SendHelper;

import java.util.Arrays;

public class AppAbstraction extends AbstractAbstraction {

    public AppAbstraction(final IConsensus consensus) {
        super(consensus);
    }

    @Override
    protected void init() {
        //does nothing on init
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

        //configure the consensus module
        consensus.configure(Arrays.asList(
                //set the process list
                (consensus) -> consensus.alterProcessList(processList),
                //push the other layers into the consensus module
                (consensus) -> consensus.pushLayer(new UniformConsensusAbstraction(consensus)),
                (consensus) -> consensus.pushLayer(new EpochChangeAbstraction(consensus)),
                (consensus) -> consensus.pushLayer(new EventuallyPerfectFailureDetectorAbstraction(consensus)),
                (consensus) -> consensus.pushLayer(new EventualLeaderDetectorAbstraction(consensus)),
                (consensus) -> consensus.pushLayer(new BestEffortBroadcastAbstraction(consensus)),
                (consensus) -> consensus.pushLayer(new PerfectLinkAbstraction(consensus))
        ));

        //trigger the appPropose message
        consensus.trigger(MessagesHelper.createUcProposeMessage(appPropose));
        return true;
    }


    private boolean onUcDecide(final Paxos.UcDecide ucDecide) {
        //get the appDecide Message
        var appDecideMessage = MessagesHelper.createAppDecideMessage(consensus.getSystemId(), ucDecide);
        //send the message to the hub
        SendHelper.sendMessage(appDecideMessage, consensus.getHubIp(), consensus.getHubPort(), consensus.getNodePort());
        return true;
    }


}
