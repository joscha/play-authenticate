import static org.fest.assertions.Assertions.assertThat;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import models.User;

import org.junit.After;
import org.junit.Test;

import play.libs.ws.WS;
import play.libs.ws.WSResponse;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.providers.oauth2.OAuth2AuthProvider;
import com.feth.play.module.pa.providers.oauth2.facebook.FacebookAuthProvider;
import com.feth.play.module.pa.providers.oauth2.facebook.FacebookAuthUser;

public class FacebookOAuth2Test extends OAuth2Test {

    public static final String FACEBOOK_USER_EMAIL = "fethjoscha@gmail.com";

    protected void amendConfiguration(final Map<String, String> additionalConfiguration) {
        additionalConfiguration.put(PlayAuthenticate.SETTING_KEY_PLAY_AUTHENTICATE + "." + FacebookAuthProvider.PROVIDER_KEY + "." + OAuth2AuthProvider.SettingKeys.CLIENT_ID, System.getenv("FACEBOOK_CLIENT_ID"));
        additionalConfiguration.put(PlayAuthenticate.SETTING_KEY_PLAY_AUTHENTICATE + "." + FacebookAuthProvider.PROVIDER_KEY + "." + OAuth2AuthProvider.SettingKeys.CLIENT_SECRET, System.getenv("FACEBOOK_CLIENT_SECRET"));
    }

    protected Class<FacebookAuthProvider> getProviderUnderTest() {
        return FacebookAuthProvider.class;
    }

    @Test
    public void itShouldBePossibleToSignUp() throws InterruptedException {
        signupUser();

        // Make sure the redirect from localhost to fb happened already (and that {@link MyUserServicePlugin#save()} gets called)
        Thread.sleep(3000);

        assertThat(browser.url()).isEqualTo("/#_=_");

        final FacebookAuthUser authUser = (FacebookAuthUser) (MyTestUserServicePlugin.getLastAuthUser());
        assertThat(authUser.getProfileLink()).isEqualTo("https://www.facebook.com/app_scoped_user_id/11111111111111111/");
        assertThat(authUser.getId()).isEqualTo("11111111111111111");
        assertThat(authUser.getGender()).isEqualTo("male");

        final User user = User.findByEmail(FACEBOOK_USER_EMAIL);
        assertThat(user).isNotNull();
        assertThat(user.firstName).isEqualTo("Joscha");
        assertThat(user.lastName).isEqualTo("Feth");
        assertThat(user.name).isEqualTo("Joscha Feth");
    }

    private void signupUser() {
        browser.goTo("/authenticate/" + FacebookAuthProvider.PROVIDER_KEY)
                .fill("#email").with(FACEBOOK_USER_EMAIL)
                .fill("#pass").with(System.getenv("FACEBOOK_USER_PASSWORD"))
                .find("#u_0_1").click();
        browser.await().untilPage().isLoaded();

        // save browser? no!
        browser.find("#u_0_2").click();
        browser.find("#checkpointSubmitButton").click();
        browser.await().untilPage().isLoaded();

        // confirm login
        browser.find("[name='__CONFIRM__']").click();
        browser.await().untilPage().isLoaded();
    }

    /**
     * After the test is finished, revoke the permission to the app so the login dialog appears again on the next login when running the test
     * See https://developers.facebook.com/docs/facebook-login/permissions/v2.1#revokelogin
     */
    @After
    public void shutdown() {
        final FacebookAuthUser authUser = (FacebookAuthUser) (MyTestUserServicePlugin.getLastAuthUser());

        final String url = PlayAuthenticate.getConfiguration().getConfig(FacebookAuthProvider.PROVIDER_KEY).getString("userInfoUrl") + "/permissions";
        final WSResponse r = WS
                .url(url)
                .setQueryParameter(OAuth2AuthProvider.Constants.ACCESS_TOKEN, authUser.getOAuth2AuthInfo().getAccessToken())
                .setQueryParameter("format", "json")
                .setQueryParameter("method", "delete")
                .get().get(10, TimeUnit.SECONDS);
    }
}
