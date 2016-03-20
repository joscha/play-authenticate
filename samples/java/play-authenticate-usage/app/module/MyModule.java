package module;

import com.feth.play.module.pa.Resolver;
import com.feth.play.module.pa.providers.openid.OpenIdAuthProvider;
import play.api.Configuration;
import play.api.Environment;
import play.api.inject.Binding;
import play.api.inject.Module;
import providers.MyStupidBasicAuthProvider;
import providers.MyUsernamePasswordAuthProvider;
import scala.collection.Seq;
import service.DataInitializer;
import service.MyResolver;
import service.MyUserService;

/**
 * Initial DI module.
 */
public class MyModule extends Module {
    @Override
    public Seq<Binding<?>> bindings(Environment environment, Configuration configuration) {
        return seq(
                bind(Resolver.class).to(MyResolver.class),

                bind(DataInitializer.class).toSelf().eagerly(),

                bind(MyUserService.class).toSelf().eagerly(),
                //bind(GoogleAuthProvider.class).toSelf().eagerly(),
                //bind(FacebookAuthProvider.class).toSelf().eagerly(),
                //bind(FoursquareAuthProvider.class).toSelf().eagerly(),
                bind(MyUsernamePasswordAuthProvider.class).toSelf().eagerly(),
                bind(OpenIdAuthProvider.class).toSelf().eagerly(),
                //bind(TwitterAuthProvider.class).toSelf().eagerly(),
                //bind(LinkedinAuthProvider.class).toSelf().eagerly(),
                //bind(VkAuthProvider.class).toSelf().eagerly(),
                //bind(XingAuthProvider.class).toSelf().eagerly(),
                //bind(UntappdAuthProvider.class).toSelf().eagerly(),
                //bind(PocketAuthProvider.class).toSelf().eagerly(),
                //bind(GithubAuthProvider.class).toSelf().eagerly(),
                bind(MyStupidBasicAuthProvider.class).toSelf().eagerly()
                //bind(SpnegoAuthProvider.class).toSelf().eagerly(),
                //bind(EventBriteAuthProvider.class).toSelf().eagerly(),

        );
    }
}
