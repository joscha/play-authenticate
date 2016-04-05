package test;

import com.feth.play.module.pa.providers.oauth2.google.GoogleAuthProvider;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.feth.play.module.pa.providers.oauth2.OAuth2AuthProvider.SettingKeys.CLIENT_ID;
import static com.feth.play.module.pa.providers.oauth2.OAuth2AuthProvider.SettingKeys.CLIENT_SECRET;

public abstract class GoogleOAuth2Base extends OAuth2Test {

    public static final String GOOGLE_USER_EMAIL = "fethjoscha@gmail.com";

    protected void amendConfiguration(final Map<String, Object> additionalConfiguration) {
        additionalConfiguration.put(constructSettingKey(CLIENT_ID), System.getenv("GOOGLE_CLIENT_ID"));
        additionalConfiguration.put(constructSettingKey(CLIENT_SECRET), System.getenv("GOOGLE_CLIENT_SECRET"));
    }

    protected String getProviderKey() {
        return GoogleAuthProvider.PROVIDER_KEY;
    }

    protected Class<GoogleAuthProvider> getProviderClass() {
        return GoogleAuthProvider.class;
    }

    protected void signupUser() throws InterruptedException {
        signupFill();
        signupApprove();
    }

    protected void signupFill() throws InterruptedException {
        goToLogin();
        browser
                .fill("#Email").with(GOOGLE_USER_EMAIL)
                .find("#next").click();
        browser.await().untilPage().isLoaded();
        TimeUnit.SECONDS.sleep(2); // couldn't figure any other way to make it work
        // apparently page is actually not ready at this point
        // #Passwd element is not being found without this sleep
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
