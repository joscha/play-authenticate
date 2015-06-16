package security;

import javax.inject.Singleton;

import play.api.Configuration;
import play.api.Environment;
import play.api.inject.Binding;
import play.api.inject.Module;
import scala.collection.Seq;
import be.objectify.deadbolt.java.cache.HandlerCache;

public class MyCustomDeadboltHook extends Module {
	
	@Override
	public Seq<Binding<?>> bindings(final Environment environment, final Configuration configuration) {
		return seq(bind(HandlerCache.class).to(MyHandlerCache.class).in(Singleton.class));
	}
	
}