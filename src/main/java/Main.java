import starter.impl.ProgramStarter;
import utils.constants.impl.ConstantsManager;

public class Main {
    public static void main(final String[] args) {

        final var constantsManager = new ConstantsManager();
        new ProgramStarter(constantsManager){{
            start();
        }};
    }

}
