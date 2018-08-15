package com.feth.play.module.pa.providers.cookie;

import com.feth.play.module.pa.PlayAuthenticate;
import play.http.ActionCreator;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.lang.reflect.Method;
import java.util.concurrent.CompletionStage;

public class CookieAuthActionCreator implements ActionCreator {
    @Inject
    PlayAuthenticate auth;

    @Override
    public Action createAction(Http.Request request, Method actionMethod) {
        return new Action.Simple() {
            @Override
            public CompletionStage<Result> call(Http.Context ctx) {
                if(!auth.isLoggedIn(ctx)) {
                    auth.tryAuthenticateWithCookie(ctx);
                }

                return delegate.call(ctx);
            }
        };
    }
}
