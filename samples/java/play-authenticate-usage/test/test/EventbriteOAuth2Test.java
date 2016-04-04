package test;

import com.feth.play.module.pa.providers.oauth2.eventbrite.EventBriteAuthProvider;
import com.feth.play.module.pa.providers.oauth2.eventbrite.EventBriteAuthUser;
import models.User;
import org.fluentlenium.core.domain.FluentWebElement;
import org.junit.Test;
import org.openqa.selenium.NoSuchElementException;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.feth.play.module.pa.providers.oauth2.OAuth2AuthProvider.SettingKeys.*;
import static org.fest.assertions.Assertions.assertThat;
import static org.fluentlenium.core.filter.FilterConstructor.*;

public class EventbriteOAuth2Test extends OAuth2Test {

    public static final String EVENTBRITE_USER_EMAIL = "fethjoscha@gmail.com";

    protected void amendConfiguration(final Map<String, Object> additionalConfiguration) {
        additionalConfiguration.put(constructSettingKey(CLIENT_ID), System.getenv("EVENTBRITE_CLIENT_ID"));
        additionalConfiguration.put(constructSettingKey(CLIENT_SECRET), System.getenv("EVENTBRITE_CLIENT_SECRET"));
    }

    protected String getProviderKey() {
        return EventBriteAuthProvider.PROVIDER_KEY;
    }

    protected Class<EventBriteAuthProvider> getProviderClass() {
        return EventBriteAuthProvider.class;
    }

    @Test
    public void itShouldBePossibleToSignUp() {
        signupUser();

        assertThat(browser.url()).isEqualTo("/");

        final EventBriteAuthUser authUser = (EventBriteAuthUser) (MyTestUserServiceService.getLastAuthUser());
        assertThat(authUser.getId()).isEqualTo("107949557141");

        final User user = User.findByEmail(EVENTBRITE_USER_EMAIL);
        assertThat(user).isNotNull();
        assertThat(user.firstName).isEqualTo("Joscha");
        assertThat(user.lastName).isEqualTo("Feth");
        assertThat(user.name).isEqualTo("Joscha Feth");
    }

    private void signupUser() {
        goToLogin();
        try {
            final String migrationLightboxSelector = "#migration_lightbox";
            final FluentWebElement migrationLightbox = browser.findFirst(migrationLightboxSelector);
            migrationLightbox.find(".mfp-close").click();
            browser.await().atMost(5L, TimeUnit.SECONDS).until(migrationLightboxSelector).areNotDisplayed();
        } catch(final NoSuchElementException nsee) {
            // migration lightbox was not shown, so we do not need to close it
        }

        browser.fill("input", withName("email")).with(EVENTBRITE_USER_EMAIL);
        browser.fill("input", withName("password")).with(System.getenv("EVENTBRITE_USER_PASSWORD"));
        browser.find("input", with("value").equalTo("Log in")).click();
        browser.await().untilPage().isLoaded();

        browser.await().atMost(5, TimeUnit.SECONDS).until("#access_choices_allow").areEnabled();
        browser.find("#access_choices_allow").click();
        browser.await().untilPage().isLoaded();
    }
}
