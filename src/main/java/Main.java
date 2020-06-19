import bootstrapper.Bootstrapper;
import consensus.node.impl.HubNode;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import utils.constants.IConstantsManager;

public class Main {
    public static void main(final String... args) {

        var bootstrapper = new AnnotationConfigApplicationContext(Bootstrapper.class);
        var constants = bootstrapper.getBean(IConstantsManager.class);

        //get the constants
        final String nodeOwnerName = (String) constants.getConstantValue("ownerName").orElseGet(() -> null);
        final String hubIp = (String) constants.getConstantValue("hubIp").orElseGet(() -> null);
        final int nodePort = (Integer) constants.getConstantValue("nodePort").orElseGet(() -> 0);
        final int hubPort = (Integer) constants.getConstantValue("hubPort").orElseGet(() -> 0);

        //start the nodes and register
        for (int i = 1; i <= 3; i++) {
            new HubNode(nodeOwnerName, i, nodePort + i, hubIp, hubPort) {{
                start();
                register();
            }};
        }

    }
}
