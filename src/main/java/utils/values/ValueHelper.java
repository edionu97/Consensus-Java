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

    /**
     * Creates a copy of the value
     * @param value: the value that will be copied
     * @return the copy of the value
     */
    public static Paxos.Value makeCopy(Paxos.Value value) {
        return Paxos.Value.newBuilder()
                .setDefined(value.getDefined())
                .setV(value.getV())
                .build();
    }
}
