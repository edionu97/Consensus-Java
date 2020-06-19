package bootstrapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import utils.constants.IConstantsManager;
import utils.constants.impl.ConstantsManager;

@Configuration
public class Bootstrapper {

    @Bean
    public IConstantsManager constantsManager() {
        return new ConstantsManager();
    }

}
