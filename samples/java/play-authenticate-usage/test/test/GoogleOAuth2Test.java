package test;

import com.feth.play.module.pa.providers.oauth2.google.GoogleAuthUser;
import models.User;
import net.sf.ehcache.CacheManager;
import org.fest.assertions.Assertions;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class GoogleOAuth2Test extends GoogleOAuth2Base {

    @Test
    public void itShouldBePossibleToSignUp() throws Exception {
        signupUser();

        Assertions.assertThat(browser.url()).isEqualTo("/");

        final GoogleAuthUser authUser = (GoogleAuthUser) (OAuth2Test.MyTestUserServiceService.getLastAuthUser());
        assertThat(authUser.getProfileLink()).isEqualTo("https://plus.google.com/109975614317978623876");
        assertThat(authUser.getId()).isEqualTo("109975614317978623876");
        assertThat(authUser.getGender()).isEqualTo("male");

        final User user = User.findByEmail(GoogleOAuth2Base.GOOGLE_USER_EMAIL);
        assertThat(user).isNotNull();
        assertThat(user.firstName).isEqualTo("Joscha");
        assertThat(user.lastName).isEqualTo("Feth");
        assertThat(user.name).isEqualTo("Joscha Feth");
    }

    @Test
    public void itShouldStillWorkIfCacheGetsCleared() throws Exception {
        signupFill();
        CacheManager.getInstance().clearAll();
        signupApprove();
        Assertions.assertThat(browser.url()).isEqualTo("/");
    }
}
