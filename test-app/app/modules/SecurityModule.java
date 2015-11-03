package modules;

import com.feth.play.module.pa.service.UserService;
import play.api.Configuration;
import play.api.Environment;
import play.api.inject.Binding;
import play.api.inject.Module;
import scala.collection.Seq;
import service.SecurityConfig;
import service.TestSecurityConfig;
import service.TestUserService;

/**
 * Created by rgupta on 03/11/15.
 */
public class SecurityModule extends Module {

    @Override
    public Seq<Binding<?>> bindings(Environment environment, Configuration configuration) {
        return seq(
                // load up resolver
                bind(SecurityConfig.class).to(TestSecurityConfig.class).eagerly(),

                // load up user serivce
                bind(UserService.class).to(TestUserService.class).eagerly()
        );
    }
}
