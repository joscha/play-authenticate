package models;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.feth.play.module.pa.providers.AuthUser;
import com.feth.play.module.pa.providers.AuthUserIdentity;

import play.db.ebean.Model;

@Entity
public class LinkedAccount extends Model {

	@Id
	public Long id;
	
	public String providerUserId;
	public String providerKey;
	
	
	public static LinkedAccount create(final AuthUser authUser) {
		final LinkedAccount ret = new LinkedAccount();
		ret.providerKey = authUser.getProvider();
		ret.providerUserId = authUser.getId();
		
		return ret;
	}


	public static LinkedAccount create(LinkedAccount acc) {
		final LinkedAccount ret = new LinkedAccount();
		ret.providerKey = acc.providerKey;
		ret.providerUserId = acc.providerUserId;
		
		return ret;
	}
}