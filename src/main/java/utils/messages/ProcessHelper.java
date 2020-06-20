package utils.messages;

import consensus.Paxos;

import java.util.Comparator;
import java.util.List;

public class ProcessHelper {

    /**
     * Get the rank to the min process from the process list
     * @param processIdList: the process list
     * @return the id of the min process
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public static Paxos.ProcessId getMinRankProcess(final List<Paxos.ProcessId> processIdList) {
        //get the min rank process
        return processIdList
                .stream()
                .min(Comparator.comparingInt(Paxos.ProcessId::getRank)).get();
    }
}
