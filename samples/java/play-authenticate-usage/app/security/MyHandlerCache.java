package security;

import javax.inject.Singleton;

import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.cache.HandlerCache;

@Singleton
public class MyHandlerCache implements HandlerCache {
	
	private final DeadboltHandler defaultHandler = new MyDeadboltHandler();

	@Override
	public DeadboltHandler apply(final String key) {
		return this.defaultHandler;
	}

	@Override
	public DeadboltHandler get() {
		return this.defaultHandler;
	}
}