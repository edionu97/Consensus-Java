package utils.values;

import consensus.Paxos;

public class ValueHelper {

    /**
     * @return an undefined value equivalent with the _|_ sign
     */
    public static Paxos.Value getUndefinedValue() {
        return Paxos.Value.newBuilder()
                .setDefined(false)
                .build();
    }
}
