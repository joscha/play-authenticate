import com.feth.play.module.pa.providers.oauth2.google.GoogleAuthProvider;
import com.feth.play.module.pa.providers.oauth2.google.GoogleAuthUser;
import com.feth.play.module.pa.user.AuthUser;
import models.User;
import org.junit.Test;
import play.Application;
import play.test.FakeApplication;
import play.test.Helpers;
import play.test.TestBrowser;
import play.test.WithBrowser;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.fest.assertions.Assertions.assertThat;

public class GoogleOAuth2Test extends WithBrowser {


    public static final String GOOGLE_USER_EMAIL = "fethjoscha@gmail.com";

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

        additionalConfiguration.put("play-authenticate.google.clientId", System.getenv("GOOGLE_CLIENT_ID"));
        additionalConfiguration.put("play-authenticate.google.clientSecret", System.getenv("GOOGLE_CLIENT_SECRET"));

        return Helpers.fakeApplication(
                additionalConfiguration,
                Arrays.asList(
                        MyTestUserServicePlugin.class.getName(),
                        com.feth.play.module.pa.providers.oauth2.google.GoogleAuthProvider.class.getName()
                ),
                Collections.singletonList(service.MyUserServicePlugin.class.getName())
        );
    }

    @Test
    public void itShouldBePossibleToSignUpWithGoogle() {
        signupGoogleUser();

        assertThat(browser.url()).isEqualTo("/");

        final GoogleAuthUser authUser = (GoogleAuthUser) (MyTestUserServicePlugin.getLastAuthUser());
        assertThat(authUser.getProfileLink()).isEqualTo("https://plus.google.com/109975614317978623876");
        assertThat(authUser.getId()).isEqualTo("109975614317978623876");
        assertThat(authUser.getGender()).isEqualTo("male");

        final User googleUser = User.findByEmail(GOOGLE_USER_EMAIL);
        assertThat(googleUser).isNotNull();
        assertThat(googleUser.firstName).isEqualTo("Joscha");
        assertThat(googleUser.lastName).isEqualTo("Feth");
        assertThat(googleUser.name).isEqualTo("Joscha Feth");
    }

    private void signupGoogleUser() {
        browser.goTo("/authenticate/" + GoogleAuthProvider.PROVIDER_KEY)
                .fill("#Email").with(GOOGLE_USER_EMAIL)
                .fill("#Passwd").with(System.getenv("GOOGLE_USER_PASSWORD"))
                .find("#signIn").click();
        browser.await().untilPage().isLoaded();
        browser.await().atMost(5, TimeUnit.SECONDS).until("#submit_approve_access").areEnabled();
        browser.find("#submit_approve_access").click();
        browser.await().untilPage().isLoaded();
    }
}
