package modules;

import com.feth.play.module.pa.service.UserService;
import play.api.Configuration;
import play.api.Environment;
import play.api.inject.Binding;
import play.api.inject.Module;
import providers.TestUsernamePasswordAuthProvider;
import scala.collection.Seq;
import services.TestSecurityConfig;
import services.TestUserService;

/**
 * Created by rgupta on 04/11/15.
 */
public class SecurityModule extends Module {

    @Override
    public Seq<Binding<?>> bindings(Environment environment, Configuration configuration) {
        return seq(
                // load global state
                bind(TestSecurityConfig.class).toSelf().eagerly(),

                // load service
                bind(UserService.class).to(TestUserService.class).eagerly(),

                // load auth provider
                bind(TestUsernamePasswordAuthProvider.class).toSelf().eagerly()
        );
    }
}
