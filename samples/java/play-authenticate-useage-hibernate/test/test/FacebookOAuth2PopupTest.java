package test;

import com.feth.play.module.pa.providers.oauth2.facebook.FacebookAuthProvider;

import java.util.Map;

public class FacebookOAuth2PopupTest extends FacebookOAuth2Test {

    protected void amendConfiguration(final Map<String, String> additionalConfiguration) {
        super.amendConfiguration(additionalConfiguration);
        additionalConfiguration.put(constructSettingKey(FacebookAuthProvider.SettingKeys.DISPLAY), "popup");
    }

    @Override
    protected String expectedLoginLayout() {
        return "popup";
    }
}
