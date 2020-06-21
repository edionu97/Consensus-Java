package consensus.algorithms.impl;

import consensus.Paxos;
import consensus.algorithms.abstracts.AbstractAbstraction;
import consensus.module.IConsensus;

import java.util.ArrayList;
import java.util.List;

public class EventuallyPerfectFailureDetectorAbstraction extends AbstractAbstraction {

    private static final int DELTA = 100;

    private List<Paxos.ProcessId> alive;
    private List<Paxos.ProcessId> suspected;
    private int delay;

    public EventuallyPerfectFailureDetectorAbstraction(final IConsensus consensus) {
        super(consensus);
    }

    @Override
    protected void init() {
        this.alive = new ArrayList<>(consensus.getProcess());
        this.suspected = new ArrayList<>();
        this.delay = DELTA;
    }

    @Override
    public boolean onMessage(final Paxos.Message message) {
        switch (message.getType()) {
            case PL_DELIVER:
                return onPlDeliver(message.getPlDeliver());
            case EPFD_TIMEOUT:
                return onEpfdTimeout(message.getEpfdTimeout());
        }
        return false;
    }

    /**
     * Handle the plDeliver message
     *
     * @param plDeliver: the message
     * @return true
     */
    private boolean onPlDeliver(final Paxos.PlDeliver plDeliver) {
        //get the inner message
        final var innerMessage = plDeliver.getMessage();

        switch (innerMessage.getType()) {
            case EPFD_HEARTBEAT_REQUEST:
                return onEpfdHeardBeatRequest(plDeliver.getSender());
            case EPFD_HEARTBEAT_REPLY:
                return onEpfdHeardBeatReply(plDeliver.getSender());
        }

        return true;
    }

    private boolean onEpfdHeardBeatRequest(final Paxos.ProcessId sender) {
        return false;
    }

    private boolean onEpfdHeardBeatReply(final Paxos.ProcessId sender) {
        return false;
    }


    private boolean onEpfdTimeout(final Paxos.EpfdTimeout epfdTimeout) {
        return false;
    }


}
