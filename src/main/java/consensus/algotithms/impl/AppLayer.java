package consensus.algotithms.impl;

import consensus.Paxos;
import consensus.algotithms.abstracts.AbstractAbstractionLayer;
import consensus.module.IConsensusModule;
import utils.messages.MessagesHelper;
import utils.messages.SendHelper;

import static consensus.Paxos.AppPropose;
import static consensus.Paxos.Message;

public class AppLayer extends AbstractAbstractionLayer {

    public AppLayer(final IConsensusModule consensus) {
        super(consensus);
    }

    @Override
    protected void init() {
        //do nothing on init
    }

    @Override
    public boolean onMessage(final Message message) {
        switch (message.getType()) {
            case APP_PROPOSE:
                return onAppPurposeMessage(message.getAppPropose());
            case UC_DECIDE:
                return onUcDecide(message.getUcDecide());
        }
        return false;
    }

    /**
     * Handle appPurpose message
     *
     * @param appPropose: the app purpose
     * @return true
     */
    private boolean onAppPurposeMessage(final AppPropose appPropose) {
        //alter the process list
        consensus.alterProcessList(appPropose.getProcessesList());
        //add layers
        consensus.pushLayer(new EventuallyPerfectFailureDetectorAbstraction(consensus));
        consensus.pushLayer(new BestEffortBroadcastAbstraction(consensus));
        consensus.pushLayer(new PerfectLinkAbstraction(consensus));
        consensus.pushLayer(new EventualLeaderDetectorAbstraction(consensus));
        consensus.pushLayer(new UniformConsensusAbstraction(consensus));
        consensus.pushLayer(new EpochChangeAbstraction(consensus));
        //push the message into queue
        consensus.trigger(MessagesHelper.createUcProposeMessage(appPropose));
        return true;
    }

    /**
     * Handle the uniform consensus decided value and send it to the hub.
     *
     * @param ucDecide the message
     */
    private boolean onUcDecide(final Paxos.UcDecide ucDecide) {
        //get the appDecide Message
        var appDecideMessage = MessagesHelper.createAppDecideMessage(consensus.getSystemId(), ucDecide);
        //send the message to the hub
        SendHelper.sendMessage(appDecideMessage, consensus.getHubIp(), consensus.getHubPort(), consensus.getNodePort());
        return true;
    }
}
