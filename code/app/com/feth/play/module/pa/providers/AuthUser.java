package com.feth.play.module.pa.providers;

import java.io.Serializable;

public abstract class AuthUser implements AuthUserIdentity, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	public static final long NO_EXPIRATION = -1L;

	public long expires() {
		return NO_EXPIRATION;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
		result = prime * result
				+ ((getProvider() == null) ? 0 : getProvider().hashCode());
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
		final AuthUserIdentity other = (AuthUserIdentity) obj;
		if (getId() == null) {
			if (other.getId() != null)
				return false;
		} else if (!getId().equals(other.getId()))
			return false;
		if (getProvider() == null) {
			if (other.getProvider() != null)
				return false;
		} else if (!getProvider().equals(other.getProvider()))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getId()+"@"+getProvider();
	}
}
