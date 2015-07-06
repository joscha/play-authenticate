package test;

import com.feth.play.module.pa.providers.oauth2.google.GoogleAuthProvider;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.feth.play.module.pa.providers.oauth2.OAuth2AuthProvider.SettingKeys.*;

public abstract class GoogleOAuth2Base extends OAuth2Test {

    public static final String GOOGLE_USER_EMAIL = "fethjoscha@gmail.com";

    protected void amendConfiguration(final Map<String, String> additionalConfiguration) {
        additionalConfiguration.put(constructSettingKey(CLIENT_ID), System.getenv("GOOGLE_CLIENT_ID"));
        additionalConfiguration.put(constructSettingKey(CLIENT_SECRET), System.getenv("GOOGLE_CLIENT_SECRET"));
    }

    protected String getProviderKey() {
        return GoogleAuthProvider.PROVIDER_KEY;
    }

    protected Class<GoogleAuthProvider> getProviderUnderTest() {
        return GoogleAuthProvider.class;
    }

    protected void signupUser() {
        signupFill();
        signupApprove();
    }

    protected void signupFill() {
        goToLogin();
        browser
                .fill("#Email").with(GOOGLE_USER_EMAIL)
                .find("#next").click();
        browser.await().untilPage().isLoaded();
        browser
                .fill("#Passwd").with(System.getenv("GOOGLE_USER_PASSWORD"))
                .find("#signIn").click();
        browser.await().untilPage().isLoaded();
    }

    protected void signupApprove() {
        browser.await().atMost(5, TimeUnit.SECONDS).until("#submit_approve_access").areEnabled();
        browser.find("#submit_approve_access").click();
        browser.await().untilPage().isLoaded();
    }
}
