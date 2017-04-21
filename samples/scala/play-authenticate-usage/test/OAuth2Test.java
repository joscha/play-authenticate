import com.feth.play.module.pa.providers.oauth2.OAuth2AuthProvider;
import com.feth.play.module.pa.providers.oauth2.google.GoogleAuthProvider;
import com.feth.play.module.pa.user.AuthUser;
import play.Application;
import play.test.FakeApplication;
import play.test.Helpers;
import play.test.TestBrowser;
import play.test.WithBrowser;

import java.util.*;

public abstract class OAuth2Test extends WithBrowser {
    @Override
    protected TestBrowser provideBrowser(int port) {
        return Helpers.testBrowser(Helpers.FIREFOX, port);
    }

    @Override
    protected int providePort() {
        return 9000; // This needs to be 9000, because the registered Applications are expecting the return URL to be on :9000
    }

    @Override
    protected FakeApplication provideFakeApplication() {

        final Map<String, String> additionalConfiguration = new HashMap<String, String>();
        additionalConfiguration.putAll(Helpers.inMemoryDatabase());
        additionalConfiguration.put("smtp.mock", "true");
        additionalConfiguration.put("logger.application", "WARN");

        amendConfiguration(additionalConfiguration);

        final List<String> additionalPlugins = new ArrayList<String>();
        additionalPlugins.add(MyTestUserServicePlugin.class.getName());
        additionalPlugins.add(getProviderUnderTest().getName());
        amendPlugins(additionalPlugins);

        return Helpers.fakeApplication(
                additionalConfiguration,
                additionalPlugins,
                Collections.singletonList(service.MyUserServicePlugin.class.getName())
        );
    }

    protected abstract Class<? extends OAuth2AuthProvider> getProviderUnderTest();

    protected abstract void amendConfiguration(final Map<String, String> additionalConfiguration);
    protected void amendPlugins(final List<String> additionalPlugins) {

    }

    public static class MyTestUserServicePlugin extends service.MyUserServicePlugin {

        private static AuthUser lastAuthUser;

        public MyTestUserServicePlugin(final Application app) {
            super(app);
        }


        @Override
        public Object save(final AuthUser authUser) {
            lastAuthUser = authUser;
            return super.save(authUser);
        }

        public static AuthUser getLastAuthUser() {
            return lastAuthUser;
        }

    }
}
