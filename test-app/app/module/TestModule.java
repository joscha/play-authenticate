package module;

import auth.TestResolver;
import com.feth.play.module.mail.IMailer;
import com.feth.play.module.mail.Mailer;
import com.feth.play.module.mail.Mailer.MailerFactory;
import com.feth.play.module.pa.Resolver;
import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import play.api.Configuration;
import play.api.Environment;
import play.api.inject.Binding;
import play.api.inject.Module;
import providers.TestUsernamePasswordAuthProvider;
import scala.collection.Seq;
import service.TestUserService;

/**
 * Test app initial dependency module.
 */
public class TestModule extends AbstractModule {
	@Override
	protected void configure() {
		install(new FactoryModuleBuilder().implement(IMailer.class, Mailer.class).build(MailerFactory.class));
		bind(Resolver.class).to(TestResolver.class);
		bind(TestUserService.class).asEagerSingleton();
		bind(TestUsernamePasswordAuthProvider.class).asEagerSingleton();
	}
}
