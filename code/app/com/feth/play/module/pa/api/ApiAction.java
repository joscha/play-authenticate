package com.feth.play.module.pa.api;

import com.feth.play.module.pa.PlayAuthenticate;

import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;


public class ApiAction extends Action<ApiRoute> {

	  public Result call(Http.Context ctx) throws Throwable {
	    if(configuration.isApiAction) {
	    	ctx.flash().put(PlayAuthenticate.API_ACTION_KEY, "true");
	    }
	    return delegate.call(ctx);
	  }
	}