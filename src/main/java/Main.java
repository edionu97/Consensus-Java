import bootstrapper.Bootstrapper;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import utils.constants.IConstantsManager;

public class Main {
    public static void main(final String ...args) {

        var bootstrapper = new AnnotationConfigApplicationContext(Bootstrapper.class);
        var constants = bootstrapper.getBean(IConstantsManager.class);

    }
}
