package consensus.algorithms.impl;

import consensus.Paxos;
import consensus.algorithms.abstracts.AbstractAbstraction;
import consensus.module.IConsensus;
import utils.messages.MessagesHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static consensus.Paxos.Message.Type.EPFD_TIMEOUT;

public class EventuallyPerfectFailureDetectorAbstraction extends AbstractAbstraction {

    private static final int DELTA = 100;

    private int delay;
    private List<Paxos.ProcessId> alive;
    private List<Paxos.ProcessId> suspected;
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    public EventuallyPerfectFailureDetectorAbstraction(final IConsensus consensus) {
        super(consensus);
    }

    @Override
    protected void init() {
        super.abstractionId = "epfd";
        this.alive = new ArrayList<>(consensus.getProcess());
        this.suspected = new ArrayList<>();
        this.delay = DELTA;
        setTimeout();
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

        //handle the message
        switch (innerMessage.getType()) {
            case EPFD_HEARTBEAT_REQUEST:
                return onEpfdHeardBeatRequest(plDeliver.getSender());
            case EPFD_HEARTBEAT_REPLY:
                return onEpfdHeardBeatReply(plDeliver.getSender());
        }

        return false;
    }

    /**
     * If the process is alive, send heartbeat reply to the process that created the request
     *
     * @param sender: the process that started the request
     * @return true
     */
    private boolean onEpfdHeardBeatRequest(final Paxos.ProcessId sender) {
        //create epfdReplyMessage
        final var epfdReplyMessage = MessagesHelper.createEpfdReply(abstractionId, sender);
        //put the message into queue
        consensus.trigger(epfdReplyMessage);
        return true;
    }

    /**
     * If the process responded, then consider them alive
     *
     * @param sender: the sender of the reply message
     * @return true
     */
    private boolean onEpfdHeardBeatReply(final Paxos.ProcessId sender) {

        //if the process is not already into the list
        if (alive.stream().noneMatch(processId -> sender.getPort() == processId.getPort())) {
            alive.add(sender);
        }

        return true;
    }

    /**
     * Set the timeout
     */
    private void setTimeout() {
        executorService.schedule(
                () -> consensus.trigger(MessagesHelper.createEpfdTimeout()),
                delay,
                TimeUnit.MILLISECONDS);
    }

    /**
     * Check if there is any process that was suspected to be not alive and now is alive.
     * If the delay is too small and not all processes managed to respond with a heartbeat reply, then increase it
     * to permit to all the processes that are alive to sent a heartbeat response.
     * Afterwards request a heartbeat reply from every processId.
     */
    private boolean onEpfdTimeout(final Paxos.EpfdTimeout ignored) {
        //check the intersection
        final var intersectionNotEmpty = alive
                .stream()
                .anyMatch(aliveProcess -> suspected
                        .stream()
                        .anyMatch(suspectedProcess -> suspectedProcess.getPort() == aliveProcess.getPort())
                );

        //if the intersection is not empty
        if(intersectionNotEmpty) {
            this.delay += DELTA;
        }

        for(final var process : consensus.getProcess()) {
            //check if the process is alive
            final var isAlive = alive.stream().anyMatch(processId -> process.getPort() == processId.getPort());
            //check if the process is suspected
            final var isSuspected = suspected.stream().anyMatch(processId -> processId.getPort() == process.getPort());

            //if the process is not alive and the process is not suspected
            if(!isAlive && !isSuspected) {
                this.suspected.add(process);
                consensus.trigger(MessagesHelper.createEpfdSuspectMessage(process));
            }
            //if the process is alive and suspected
            if(isAlive && isSuspected) {
                this.suspected.removeIf(suspected -> suspected.getPort() == process.getPort());
                consensus.trigger(MessagesHelper.createEpfdRestoreMessage(process));
            }

            consensus.trigger(MessagesHelper.createEpfdHeartBeatRequestMessage(abstractionId, process));
        }

        alive.clear();
        setTimeout();
        return true;
    }


}
