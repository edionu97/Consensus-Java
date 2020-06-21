import consensus.node.impl.HubNode;
import utils.constants.impl.ConstantsManager;

public class Main {
    public static void main(final String[] args) {

        final var constantsManager = new ConstantsManager();

        //get the constants
        final String nodeOwnerName = (String) constantsManager.getConstantValue("ownerName").orElseGet(() -> null);
        final String hubIp = (String) constantsManager.getConstantValue("hubIp").orElseGet(() -> null);
        final int nodePort = (Integer) constantsManager.getConstantValue("nodePort").orElseGet(() -> 0);
        final int hubPort = (Integer) constantsManager.getConstantValue("hubPort").orElseGet(() -> 0);
        final int nodeNr = (Integer) constantsManager.getConstantValue("nodeNr").orElseGet(() -> 0);

        //create the nods and register them
        for (int i = 1; i <= nodeNr; i++) {
            new HubNode(nodeOwnerName, i, nodePort + i, hubIp, hubPort) {{
                start();
                register();
            }};
        }
    }

}
