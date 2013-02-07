package security;

import com.feth.play.module.pa.PlayAuthenticate;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import play.Configuration;
import play.Play;
import play.cache.Cache;

import java.util.ArrayList;
import java.util.List;
import static play.mvc.Http.Context;
import static play.mvc.Http.Request;
import static play.mvc.Http.Session;

public class PaSession {


    private static final Configuration PA_CONF = PlayAuthenticate.getConfiguration();
    public static final String SES_KEY = "pa.ses.id";
    public static final String EXPIRES_KEY = "pa.u.exp";
    public static final String USER_KEY = "pa.u.id";
    private static final String CACHE_PREFIX = "PA_SES_";


    public String sesId;
    public String ipLock;
    private String userId;
    private String hash;
    private Context context;


    /**
     * Creates session with new id
     *
     * @param session Session
     * @param request Request
     */
    public PaSession(Session session, Request request) {
        this(session, request, RandomStringUtils.randomAlphanumeric(64));
    }

    /**
     * Creates session from context (for comparison)
     *
     * @param ctx Context (contains session and request)
     */
    public PaSession(Context ctx) {
        this(ctx.session(), ctx.request(), ctx.session().get(SES_KEY));
        this.context=ctx;
    }

    public PaSession(Session session, Request request, String sesId) {

        this.sesId = sesId;
        this.userId = session.get(USER_KEY);
        this.ipLock = createIpLock(request);

        calculateHash();
    }

    private void calculateHash() {
        this.hash = DigestUtils.sha256Hex("ses_hash_"+this.sesId+"_" + this.sesId + this.userId + this.ipLock);
    }


    private String createIpLock(Request request) {
        int ipLockSize = PA_CONF.getInt("session.ipLockSize");
        if (ipLockSize == 0) return "";


        String ipAddress = (PA_CONF.getBoolean("session.useForwardIp")
                && request.getHeader(PA_CONF.getString("session.forwardIpHeader")) != null)
                ? request.getHeader(PA_CONF.getString("session.forwardIpHeader"))
                : request.remoteAddress();

        List<String> parts = new ArrayList<String>();

        String ipLockParts[] = ipAddress.split("\\.");
        int i = 0;
        for (String part : ipLockParts) {
            if (i < ipLockSize) parts.add(part);
            i++;
        }

        return StringUtils.join(parts, ".");
    }


    public void saveToCache(Session session) {
        Cache.set(CACHE_PREFIX + this.sesId, this, PA_CONF.getInt("session.timeout"));
        session.put(EXPIRES_KEY, getExpDate());
        session.put(SES_KEY, this.sesId);
    }

    public static void delete(Session session) {
        String cacheKey = CACHE_PREFIX + session.get(SES_KEY);
        Cache.set(cacheKey, null, 0);
        session.remove(SES_KEY);
    }

    public boolean exists() {

        if (Play.isDev()
                && PA_CONF.getBoolean("session.disableCheckInDev") != null
                && PA_CONF.getBoolean("session.disableCheckInDev")
                ) return true;

        String cacheKey = CACHE_PREFIX + this.context.session().get(SES_KEY);
        PaSession cachedSession = (PaSession) Cache.get(cacheKey);
        boolean exists;
        if (cachedSession != null && this.hash.equals(cachedSession.hash)) {

            this.saveToCache(this.context.session());
            exists = true;

        } else {
            exists = false;
            delete(this.context.session());
            com.feth.play.module.pa.controllers.Authenticate.logout();
        }
        return exists;
    }

    public String getExpDate() {
        int timeout = PA_CONF.getInt("session.timeout");
        String out = "-1";
        if (timeout > 0) {
            Long ts = (System.currentTimeMillis()) + (timeout*1000);
            out = ts.toString();
        }
        return out;
    }
}
