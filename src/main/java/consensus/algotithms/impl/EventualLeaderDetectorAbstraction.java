package consensus.algotithms.impl;

import consensus.Paxos;
import consensus.algotithms.abstracts.AbstractAbstractionLayer;
import consensus.module.IConsensusModule;
import utils.messages.MessagesHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static consensus.Paxos.Message;
import static consensus.Paxos.ProcessId;

/**
 * An eventual leader-detector abstraction, that encapsulates a leader-election primitive
 * which ensures that eventually the correct processes will elect the same correct process
 * as their leader. Nothing precludes the possibility for leaders to change in an arbitrary
 * manner and for an arbitrary period of time. Moreover, many leaders might be elected during
 * the same period of time without having crashed. Once a unique leader is determined, and does
 * not change again, we say that the leader has stabilized.
 * <p>
 * The Monarchical Eventual Leader Detection is implemented as a leader-detector abstraction.
 * The algorithm maintains the set of processes that are suspected and declares the nonsuspected
 * process with the highest rank to be the leader. Eventually, and provided at least one
 * process is correct, the same correct process will be trusted by all correct processes.
 */
public class EventualLeaderDetectorAbstraction extends AbstractAbstractionLayer {

    private List<ProcessId> suspected;
    private ProcessId leader;

    public EventualLeaderDetectorAbstraction(final IConsensusModule consensus) {
        super(consensus);
    }

    @Override
    protected void init() {
        this.suspected = new ArrayList<>();
        this.leader = null;
        updateLeader();
    }

    @Override
    public boolean onMessage(final Message message) {
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
     *
     * @param epfdSuspect: the received message
     * @return true
     */
    private boolean onEpfdSuspect(final Paxos.EpfdSuspect epfdSuspect) {
        //get the suspected process
        final var suspectedProcess = epfdSuspect.getProcess();
        //if the suspected process is not already into the list
        if (suspected.stream().noneMatch(processId -> suspectedProcess.getPort() != processId.getPort())) {
            suspected.add(epfdSuspect.getProcess());
            updateLeader();
        }
        return true;
    }


    /**
     * If the process was restored, we no longer need to suspect it, so we remove it from the suspected list
     *
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
        //get all processes that are not suspected
        final var difference = new ArrayList<>(consensus.getProcessList())
                .stream()
                .filter(node -> suspected
                        .stream()
                        .noneMatch(suspected -> suspected.getPort() == node.getPort()))
                .collect(Collectors.toList());

        //if all the list is empty, then do nothing
        if (difference.isEmpty()) {
            return;
        }

        //get the new leader
        final var maxRankAliveProcess = difference
                .stream()
                .max(Comparator.comparingInt(Paxos.ProcessId::getRank)).get();

        //check if event condition is triggered
        if (!(this.leader == null || this.leader.getRank() != maxRankAliveProcess.getRank())) {
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
