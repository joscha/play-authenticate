package test;

import com.feth.play.module.pa.providers.oauth2.OAuth2AuthProvider;
import com.feth.play.module.pa.providers.oauth2.facebook.FacebookAuthProvider;
import com.feth.play.module.pa.providers.oauth2.facebook.FacebookAuthUser;
import models.User;
import org.junit.After;
import org.junit.Test;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriverException;
import play.libs.ws.WSClient;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.feth.play.module.pa.providers.oauth2.OAuth2AuthProvider.SettingKeys.CLIENT_ID;
import static com.feth.play.module.pa.providers.oauth2.OAuth2AuthProvider.SettingKeys.CLIENT_SECRET;
import static org.fest.assertions.Assertions.assertThat;

public class FacebookOAuth2Test extends OAuth2Test {

    public static final String FACEBOOK_USER_EMAIL = "ufbullq_fallerman_1414534488@tfbnw.net";
    public static final String FACEBOOK_USER_ID = "100005169708842";

    protected void amendConfiguration(final Map<String, Object> additionalConfiguration) {
        additionalConfiguration.put(constructSettingKey(CLIENT_ID), System.getenv("FACEBOOK_CLIENT_ID"));
        additionalConfiguration.put(constructSettingKey(CLIENT_SECRET), System.getenv("FACEBOOK_CLIENT_SECRET"));
    }

    protected String getProviderKey() {
        return FacebookAuthProvider.PROVIDER_KEY;
    }

    protected Class<FacebookAuthProvider> getProviderClass() {
        return FacebookAuthProvider.class;
    }

    @Test
    public void itShouldBePossibleToSignUp() throws InterruptedException {
        signupUser();

        // Make sure the redirect from localhost to fb happened already (and that {@link MyUserService#save()} gets called)
        Thread.sleep(3000);

        assertThat(browser.url()).isEqualTo("/#_=_");

        final FacebookAuthUser authUser = (FacebookAuthUser) (MyTestUserServiceService.getLastAuthUser());
        assertThat(authUser.getProfileLink()).contains(FACEBOOK_USER_ID);
        assertThat(authUser.getId()).isEqualTo(FACEBOOK_USER_ID);
        assertThat(authUser.getGender()).isEqualTo("female");

        final User user = User.findByEmail(FACEBOOK_USER_EMAIL);
        assertThat(user).isNotNull();
        assertThat(user.firstName).isEqualTo("Mary");
        assertThat(user.lastName).isEqualTo("Fallerman");
        assertThat(user.name).isEqualTo("Mary Ameafigjhhdb Fallerman");
    }

    private void signupUser() {
        goToLogin();
        browser
                .fill("#email").with(FACEBOOK_USER_EMAIL)
                .fill("#pass").with(System.getenv("FACEBOOK_USER_PASSWORD"))
                .find("#loginbutton").click();
        browser.await().untilPage().isLoaded();

        // save browser? no!
        try {
            // try, because this is not checked for test users, because they are not asked
            final String selector = "#u_0_2";
            browser.await().atMost(10, TimeUnit.SECONDS).until(selector);
            browser.find(selector).click();
            browser.find("#checkpointSubmitButton").click();
            browser.await().untilPage().isLoaded();
        } catch (final NoSuchElementException nsee) {
            // mobile
        } catch(final ElementNotVisibleException enve) {
            // desktop
        } catch(final WebDriverException wde) {
            // something else
        }

        // check login layout
        checkLoginLayout();

        // confirm login
        browser
                .find("[name='__CONFIRM__']")
                .click();
        browser
                .await()
                .untilPage()
                .isLoaded();

    }

    protected String expectedLoginLayout() {
        return "page";
    }

    private void checkLoginLayout() {
        final String selector = "[name='display']";
        browser.await().atMost(10, TimeUnit.SECONDS).until(selector);
        assertThat(browser.find(selector).getValue()).isEqualTo(expectedLoginLayout());
    }

    /**
     * After the test is finished, revoke the permission to the app so the login dialog appears again on the next login when running the test
     * See https://developers.facebook.com/docs/facebook-login/permissions/v2.1#revokelogin
     */
    @After
    public void shutdown() throws Exception {
        final FacebookAuthUser authUser = (FacebookAuthUser) (MyTestUserServiceService.getLastAuthUser());

        if (authUser == null) {
            // in case the test failed, we don't have an authUser
            return;
        }

        final String url = getConfig().getString("userInfoUrl") + "/permissions";

        WSClient wsClient = app.injector().instanceOf(WSClient.class);
        wsClient
                .url(url)
                .setQueryParameter(OAuth2AuthProvider.Constants.ACCESS_TOKEN, authUser.getOAuth2AuthInfo().getAccessToken())
                .setQueryParameter("format", "json")
                .setQueryParameter("method", "delete")
                .get().toCompletableFuture().get(10, TimeUnit.SECONDS);
    }

}
