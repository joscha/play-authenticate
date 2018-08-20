package com.feth.play.module.pa.providers.cookie;

import com.feth.play.module.pa.PlayAuthenticate;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class SudoForbidCookieAuthAction extends play.mvc.Action.Simple {
    @Inject
    private PlayAuthenticate auth;

    public CompletionStage<Result> call(Http.Context ctx) {
        if(auth.isAuthorizedWithCookie(ctx)) {
            ctx.flash().put("error", "Please type password again to access requested page");
            return CompletableFuture.completedFuture(redirect(this.auth.getResolver().relogin()));
        }
        return delegate.call(ctx);
    }
}