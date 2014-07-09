import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.providers.oauth2.OAuth2AuthProvider;
import com.feth.play.module.pa.providers.oauth2.google.GoogleAuthProvider;
import com.feth.play.module.pa.providers.oauth2.google.GoogleAuthUser;
import models.User;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.fest.assertions.Assertions.assertThat;

public class GoogleOAuth2Test extends OAuth2Test {

    public static final String GOOGLE_USER_EMAIL = "fethjoscha@gmail.com";

    protected void amendConfiguration(final Map<String, String> additionalConfiguration) {
        additionalConfiguration.put(PlayAuthenticate.SETTING_KEY_PLAY_AUTHENTICATE + "." + GoogleAuthProvider.PROVIDER_KEY + "." + OAuth2AuthProvider.SettingKeys.CLIENT_ID, System.getenv("GOOGLE_CLIENT_ID"));
        additionalConfiguration.put(PlayAuthenticate.SETTING_KEY_PLAY_AUTHENTICATE + "." + GoogleAuthProvider.PROVIDER_KEY + "." + OAuth2AuthProvider.SettingKeys.CLIENT_SECRET, System.getenv("GOOGLE_CLIENT_SECRET"));
    }

    protected Class<GoogleAuthProvider> getProviderUnderTest() {
        return GoogleAuthProvider.class;
    }

    @Test
    public void itShouldBePossibleToSignUp() {
        signupUser();

        assertThat(browser.url()).isEqualTo("/");

        final GoogleAuthUser authUser = (GoogleAuthUser) (MyTestUserServicePlugin.getLastAuthUser());
        assertThat(authUser.getProfileLink()).isEqualTo("https://plus.google.com/109975614317978623876");
        assertThat(authUser.getId()).isEqualTo("109975614317978623876");
        assertThat(authUser.getGender()).isEqualTo("male");

        final User user = User.findByEmail(GOOGLE_USER_EMAIL);
        assertThat(user).isNotNull();
        assertThat(user.firstName).isEqualTo("Joscha");
        assertThat(user.lastName).isEqualTo("Feth");
        assertThat(user.name).isEqualTo("Joscha Feth");
    }

    private void signupUser() {
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
