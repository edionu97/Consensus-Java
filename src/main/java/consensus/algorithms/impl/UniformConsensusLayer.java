package consensus.algorithms.impl;

import consensus.Paxos;
import consensus.algorithms.abstracts.AbstractLayer;
import consensus.module.IConsensus;
import utils.values.ValueHelper;

import java.util.Comparator;
import java.util.List;

public class UniformConsensusLayer extends AbstractLayer {

    private int ets;
    private int newts;

    private Paxos.Value val;

    private boolean decided;
    private boolean proposed;

    private Paxos.ProcessId l;
    private Paxos.ProcessId newl;

    public UniformConsensusLayer(final IConsensus consensus) {
        super(consensus);
    }

    @Override
    protected void init() {
        this.val = ValueHelper.getUndefinedValue();

        this.proposed = this.decided = false;
        this.ets = this.newts = 0;

        this.l = getMinRankProcess(consensus.getProcess());
        this.newl = null;
    }

    @Override
    public boolean onMessage(final Paxos.Message message) {
        return false;
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private Paxos.ProcessId getMinRankProcess(final List<Paxos.ProcessId> processIdList) {
        //get the min rank process
        return processIdList
                .stream()
                .min(Comparator.comparingInt(Paxos.ProcessId::getRank)).get();
    }
}
