package providers;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.providers.cookie.CookieAuthProvider;
import com.feth.play.module.pa.providers.cookie.CookieAuthUser;
import com.feth.play.module.pa.user.AuthUser;
import models.CookieTokenSeries;
import models.LinkedAccount;
import play.api.Play;
import play.inject.ApplicationLifecycle;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class MyCookieAuthProvider extends CookieAuthProvider {

    @Inject
    PlayAuthenticate auth;

    @Inject
    public MyCookieAuthProvider(PlayAuthenticate auth, ApplicationLifecycle lifecycle) {
        super(auth, lifecycle);
    }

    @Override
    protected void save(CookieAuthUser cookieAuthUser, AuthUser loginUser)  {
        getAuth().getUserService().link(loginUser, cookieAuthUser);

        CookieTokenSeries cookieSeries = CookieTokenSeries.create(loginUser, cookieAuthUser.getSeries(), cookieAuthUser.getToken());

        cookieSeries.save();
    }

    @Override
    protected CheckResult check(CookieAuthUser cookieAuthUser) {
        if(cookieAuthUser.getSeries() == null) {
            return CheckResult.MISSING_SERIES;
        }

        LinkedAccount linkedAccount = LinkedAccount.findByProvider(PROVIDER_KEY, cookieAuthUser.getSeries());

        if(linkedAccount == null) {
            return CheckResult.ERROR;
        }

        CookieTokenSeries cookieSeries = CookieTokenSeries.findBySeries(linkedAccount.user, linkedAccount.providerUserId);

        if(cookieSeries == null) {
            return CheckResult.MISSING_SERIES;
        }

        if(!cookieSeries.token.equals(cookieAuthUser.getToken())) {
            return CheckResult.INVALID_TOKEN;
        }

        long daysSinceCreated = LocalDateTime.from(cookieSeries.timeCreated).until(LocalDateTime.now(), ChronoUnit.DAYS);
        long daysSinceUpdated = LocalDateTime.from(cookieSeries.timeUpdated).until(LocalDateTime.now(), ChronoUnit.DAYS);

        long timeoutDaysSinceCreated = auth.getConfiguration().getLong("cookie.timeoutDays.sinceFirstLogin");

        long timeoutDaysSinceUpdated = auth.getConfiguration().getLong("cookie.timeoutDays.sinceLastLogin");

        if(daysSinceCreated > timeoutDaysSinceCreated) {
            return CheckResult.EXPIRED;
        }

        if(daysSinceUpdated > timeoutDaysSinceUpdated) {
            return CheckResult.EXPIRED;
        }

        return CheckResult.SUCCESS;
    }

    @Override
    protected void renew(CookieAuthUser cookieAuthUser, String newToken) {
        LinkedAccount linkedAccount = LinkedAccount.findByProvider(PROVIDER_KEY, cookieAuthUser.getSeries());

        CookieTokenSeries cookieSeries = CookieTokenSeries.findBySeries(linkedAccount.user, linkedAccount.providerUserId);

        cookieSeries.updateToken(newToken);
        cookieSeries.save();
    }
}
