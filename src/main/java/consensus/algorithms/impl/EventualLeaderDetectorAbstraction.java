package consensus.algorithms.impl;

import consensus.Paxos;
import consensus.algorithms.abstracts.AbstractAbstraction;
import consensus.module.IConsensus;
import utils.messages.MessagesHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An eventual leader-detector abstraction, that encapsulates a leader-election primitive
 * which ensures that eventually the correct processes will elect the same correct process
 * as their leader. Nothing precludes the possibility for leaders to change in an arbitrary
 * manner and for an arbitrary period of time. Moreover, many leaders might be elected during
 * the same period of time without having crashed. Once a unique leader is determined, and does
 * not change again, we say that the leader has stabilized.
 *
 * The Monarchical Eventual Leader Detection is implemented as a leader-detector abstraction.
 * The algorithm maintains the set of processes that are suspected and declares the nonsuspected
 * process with the highest rank to be the leader. Eventually, and provided at least one
 * process is correct, the same correct process will be trusted by all correct processes.
 */
public class EventualLeaderDetectorAbstraction extends AbstractAbstraction {

    private List<Paxos.ProcessId> suspected;
    private Paxos.ProcessId leader;

    public EventualLeaderDetectorAbstraction(final IConsensus consensus) {
        super(consensus);
    }

    @Override
    protected void init() {
        this.suspected = new ArrayList<>();
        this.leader = null;
        updateLeader();
    }

    @Override
    public boolean onMessage(final Paxos.Message message) {

        switch (message.getType()) {
            case EPFD_SUSPECT:
                return onEpfdSuspect(message.getEpfdSuspect());
            case EPFD_RESTORE:
                return onEpfdRestore(message.getEpfdRestore());
        }

        return false;
    }

    /**
     * This method listens for EPFD_SUSPECT message
     * If a process is suspected to be dead, it is added into the list
     * The leader is updated
     * @param epfdSuspect: the received message
     * @return true
     */
    private boolean onEpfdSuspect(final Paxos.EpfdSuspect epfdSuspect) {

        //check if in the list is a process that have the same port as the r
        final var isPresent = suspected.stream().anyMatch(x -> x.getPort() == epfdSuspect.getProcess().getPort());
        if(isPresent) {
            return true;
        }

        //add the process into the suspected list if it is not already into the list
        suspected.add(epfdSuspect.getProcess());

        //trigger the leader update check
        updateLeader();
        return true;
    }

    /**
     * If the process was restored, we no longer need to suspect it, so we remove it from the suspected list
     * @param epfdRestore: the message
     * @return true
     */
    private boolean onEpfdRestore(final Paxos.EpfdRestore epfdRestore) {
        //remove the process from suspected list
        suspected.removeIf(processId -> epfdRestore.getProcess().getPort() == processId.getPort());
        //check update leader
        updateLeader();
        return true;
    }

    /**
     * Updates the trusted leader if the current leader is not the the process with the maximum
     * (by rank) from the processes that are not suspected to be dead
     */
    private void updateLeader() {
        //get the process that are not suspected to be dead
        final var difference = consensus.getProcess()
                .stream()
                .filter(nodeProcess -> suspected
                        .stream()
                        .noneMatch(suspectedProcess -> suspectedProcess.getPort() == nodeProcess.getPort())
                )
                .collect(Collectors.toList());

        //get the new leader
        final var maxRankAliveProcess = difference
                .stream()
                .max(Comparator.comparingInt(Paxos.ProcessId::getRank)).get();

        //check if event condition is triggered
        if(!(this.leader == null || this.leader.getRank() != maxRankAliveProcess.getRank())) {
            return;
        }

        //update the leader
        this.leader = maxRankAliveProcess;

        //create the message
        final var eldTrustMessage = MessagesHelper.createEldTrustMessage(this.leader);

        //put the message into queue
        consensus.trigger(eldTrustMessage);
    }
}
