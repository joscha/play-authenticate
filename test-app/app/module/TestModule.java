package module;

import auth.TestResolver;
import com.feth.play.module.pa.Resolver;
import play.api.Configuration;
import play.api.Environment;
import play.api.inject.Binding;
import play.api.inject.Module;
import providers.TestUsernamePasswordAuthProvider;
import scala.collection.Seq;
import service.TestUserService;

/**
 * Test app initial dependency module.
 */
public class TestModule extends Module {
    @Override
    public Seq<Binding<?>> bindings(Environment environment, Configuration configuration) {
        return seq(
                bind(Resolver.class).to(TestResolver.class),
                bind(TestUserService.class).toSelf().eagerly(),
                bind(TestUsernamePasswordAuthProvider.class).toSelf().eagerly()
        );
    }
}
