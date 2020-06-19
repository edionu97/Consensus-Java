package consensus.module.impl;

import consensus.Paxos;
import consensus.module.IConsensus;

import java.util.Optional;

public class ConsensusSystemModule implements IConsensus {

    private final int nodePort;
    private final int hubPort;
    private final String hubIp;
    private final String systemId;

    public ConsensusSystemModule(final int nodePort,
                                 final int hubPort, final String hubIp, final String systemId) {
        this.nodePort = nodePort;
        this.hubPort = hubPort;
        this.hubIp = hubIp;
        this.systemId = systemId;
    }


    @Override
    public void trigger(final Paxos.Message message) {

        System.out.println(message.getSystemId() + " " + message.getNetworkMessage().getMessage().getType());
    }

    @Override
    public Optional<Paxos.ProcessId> identifySenderProcessByNetworkMessage(final Paxos.NetworkMessage networkMessage) {
        return Optional.empty();
    }
}
