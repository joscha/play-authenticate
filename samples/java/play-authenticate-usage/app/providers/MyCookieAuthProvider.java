package providers;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.providers.cookie.CookieAuthProvider;
import com.feth.play.module.pa.providers.cookie.CookieAuthUser;
import models.LinkedAccount;
import play.inject.ApplicationLifecycle;

import javax.inject.Inject;

public class MyCookieAuthProvider extends CookieAuthProvider {
    @Inject
    public MyCookieAuthProvider(PlayAuthenticate auth, ApplicationLifecycle lifecycle) {
        super(auth, lifecycle);
    }

    @Override
    protected CheckResult check(CookieAuthUser cookieAuthUser) {
        LinkedAccount.find
        return CheckResult.SUCCESS;
    }

    @Override
    protected void renew(CookieAuthUser cookieAuthUser, String newToken) {
        // TODO
    }
}
