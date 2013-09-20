package com.feth.play.module.pa.providers.oauth2.pocket;

import play.Application;
import play.libs.WS.Response;

import com.feth.play.module.pa.exceptions.AccessTokenException;
import com.feth.play.module.pa.exceptions.AuthException;
import com.feth.play.module.pa.providers.oauth2.OAuth2AuthProvider;
import com.feth.play.module.pa.user.AuthUserIdentity;

public class PocketAuthProvider extends
		OAuth2AuthProvider<PocketAuthUser, PocketAuthInfo> {

	static final String PROVIDER_KEY = "pocket";
	
	public PocketAuthProvider(Application app) {
		super(app);
	}

	@Override
	public String getKey() {
		return PROVIDER_KEY;
	}

	@Override
  protected PocketAuthInfo buildInfo(Response r) throws AccessTokenException {
	  // TODO Auto-generated method stub
	  return null;
  }

	@Override
  protected AuthUserIdentity transform(PocketAuthInfo info, String state)
      throws AuthException {
	  // TODO Auto-generated method stub
	  return null;
  }

}
