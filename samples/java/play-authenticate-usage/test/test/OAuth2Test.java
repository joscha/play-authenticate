package test;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.providers.oauth2.OAuth2AuthProvider;
import com.feth.play.module.pa.user.AuthUser;
import com.google.inject.Inject;
import org.junit.Before;
import play.Application;
import play.Configuration;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.Helpers;
import play.test.TestBrowser;
import play.test.WithBrowser;
import service.MyUserService;

import java.util.HashMap;
import java.util.Map;

import static play.inject.Bindings.bind;


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
    protected Application provideApplication() {

        final Map<String, Object> additionalConfiguration = new HashMap<>();
        additionalConfiguration.putAll(Helpers.inMemoryDatabase());

        additionalConfiguration.put("smtp.mock", "true");

        amendConfiguration(additionalConfiguration);

        return new GuiceApplicationBuilder()
                .bindings(
                        bind(getProviderClass()).toSelf().eagerly()
                )
                .configure(additionalConfiguration)
                .overrides(bind(MyUserService.class).to(MyTestUserServiceService.class).eagerly())
                .build();
    }

    protected abstract Class<? extends OAuth2AuthProvider> getProviderClass();
    protected abstract String getProviderKey();

    protected abstract void amendConfiguration(final Map<String, Object> additionalConfiguration);

    protected void goToLogin() {
        browser.goTo("/authenticate/" + getProviderKey());
        browser.await().untilPage().isLoaded();
    }

    protected String constructSettingKey(final String setting) {
        return PlayAuthenticate.SETTING_KEY_PLAY_AUTHENTICATE + "." + getProviderKey() + "." + setting;
    }

    protected Configuration getConfig() {
        return app.injector().instanceOf(PlayAuthenticate.class).getConfiguration().getConfig(getProviderKey());
    }

    public static class MyTestUserServiceService extends MyUserService {

        private static AuthUser lastAuthUser;

        @Inject
        public MyTestUserServiceService(final PlayAuthenticate auth) {
            super(auth);
        }

        @Override
        public void onStart() {
            this.auth.setUserService(this);
        }

        @Override
        public Object save(final AuthUser authUser) {
            lastAuthUser = authUser;
            return super.save(authUser);
        }

        public static AuthUser getLastAuthUser() {
            return lastAuthUser;
        }

        public static void resetLasAuthUser() {
            lastAuthUser = null;
        }

    }

    @Before
    public void resetLastAuthUser() {
        MyTestUserServiceService.resetLasAuthUser();
    }

}
