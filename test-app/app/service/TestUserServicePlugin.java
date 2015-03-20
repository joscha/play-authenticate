package service;

import java.util.HashMap;
import java.util.Map;

import play.Application;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.service.UserServicePlugin;
import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.AuthUserIdentity;
import com.google.inject.Inject;

public class TestUserServicePlugin extends UserServicePlugin {

	private final Map<AuthUserIdentity, AuthUser> users = new HashMap<AuthUserIdentity, AuthUser>();

    @Inject
	public TestUserServicePlugin(Application app) {
		super(app);
	}

	@Override
	public void onStart() {
		PlayAuthenticate.setUserService(this);
	}

	@Override
	public Object save(final AuthUser authUser) {
		users.put(new ImmutableAuthUserIdentity(authUser), authUser);
		return authUser;
	}

	@Override
	public Object getLocalIdentity(final AuthUserIdentity identity) {
		return users.get(new ImmutableAuthUserIdentity(identity));
	}

	@Override
	public AuthUser merge(AuthUser newUser, AuthUser oldUser) {
		// Not Implemented
		return newUser;
	}

	@Override
	public AuthUser link(AuthUser oldUser, AuthUser newUser) {
		// Not Implemented
		return newUser;
	}

	private class ImmutableAuthUserIdentity implements AuthUserIdentity {

		private final String id;
		private final String provider;

		public ImmutableAuthUserIdentity(final String id, final String provider) {
			this.id = id;
			this.provider = provider;
		}

		public ImmutableAuthUserIdentity(final AuthUserIdentity aui) {
			this(aui.getId(), aui.getProvider());
		}

		@Override
		public String getId() {
			return id;
		}

		@Override
		public String getProvider() {
			return provider;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((id == null) ? 0 : id.hashCode());
			result = prime * result
					+ ((provider == null) ? 0 : provider.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ImmutableAuthUserIdentity other = (ImmutableAuthUserIdentity) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (id == null) {
				if (other.id != null)
					return false;
			} else if (!id.equals(other.id))
				return false;
			if (provider == null) {
				if (other.provider != null)
					return false;
			} else if (!provider.equals(other.provider))
				return false;
			return true;
		}

		private TestUserServicePlugin getOuterType() {
			return TestUserServicePlugin.this;
		}

	}

}
