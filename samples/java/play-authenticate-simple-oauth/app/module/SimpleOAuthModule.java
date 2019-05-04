package module;

import com.feth.play.module.pa.Resolver;
import play.api.Configuration;
import play.api.Environment;
import play.api.inject.Binding;
import play.api.inject.Module;
import scala.collection.Seq;
import service.MyResolver;
import service.MyUserService;

/**
 * Initial DI module.
 */
public class SimpleOAuthModule extends Module {
    @Override
    public Seq<Binding<?>> bindings(Environment environment, Configuration configuration) {
        return seq(
                bind(Resolver.class).to(MyResolver.class),
                bind(MyUserService.class).toSelf().eagerly()
        );
    }
}
