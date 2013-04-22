package com.feth.play.module.pa.api;

import java.io.Serializable;

public class Json {

	public static class ApiLoginResult implements Serializable {
		private static final long serialVersionUID = 1L;

		public ApiLoginResult(boolean success, String userKey, String provider) {
			this.success = success;
			this.user_key = userKey;
			this.provider = provider;
		}

		public boolean success;
		public String user_key;
		public String provider;
	}

}
