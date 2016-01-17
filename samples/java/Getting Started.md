#Getting Started


##Importing the Plugin
---

Add Play-Authenticate to your app dependencies. This is done by modifying the `project/Build.scala` file.
Add `"com.feth"      %%  "play-authenticate" % "0.2.1-SNAPSHOT"` (`0.2.1` might actually change - have a look at the version history to select the latest version) as a dependency and add the resolvers as shown below:

	import sbt._
	import Keys._
	import PlayProject._
	
	object ApplicationBuild extends Build {
	
	    val appName         = "authtut"
	    val appVersion      = "1.0-SNAPSHOT"
	
	    val appDependencies = Seq(
	      // Add your project dependencies here,
	      "com.feth"      %%  "play-authenticate" % "0.2.0-SNAPSHOT"
	    )
	
	    val main = PlayProject(appName, appVersion, appDependencies, mainLang = JAVA).settings(
	      // Add your own project settings here   
	      resolvers += Resolver.url("play-easymail (release)", url("http://joscha.github.io/play-easymail/repo/releases/"))(Resolver.ivyStylePatterns),
	      resolvers += Resolver.url("play-easymail (snapshot)", url("http://joscha.github.io/play-easymail/repo/snapshots/"))(Resolver.ivyStylePatterns),
	  
	        
	      resolvers += Resolver.url("play-authenticate (release)", url("http://joscha.github.io/play-authenticate/repo/releases/"))(Resolver.ivyStylePatterns),
	      resolvers += Resolver.url("play-authenticate (snapshot)", url("http://joscha.github.io/play-authenticate/repo/snapshots/"))(Resolver.ivyStylePatterns)
	    )
	
	}
	
 
##Configuration File
---

Create a new file `play-authenticate/mine.conf` in your conf folder. Include this file in your `application.conf` by adding the following line to it:

	include "play-authenticate/mine.conf"

In your mine.conf file you can configure play-authenticate. The following example configuration will disable account merging (e.g. when they log in with an existing account whilst being logged in already with another account) and link users automatically when they authenticate with a new provider while being logged in.

	play-authenticate {
		accountMergeEnabled=false
		accountAutoLink=true
	}

The configuration file is also used to configure the different authentication providers.
	
A list of all configuration options can be found here TODO.

### Configuring Mail

You also have to configure smtp and play-easymail. For testing purposes you can add the following line to your application.conf file:

	smtp.mock=true
	
For a real application you can use the following template ([source](https://github.com/joscha/play-easymail/tree/master/samples/play-easymail-usage/conf)):

	# SMTP mailer settings
	smtp {
		# SMTP server
		# (mandatory)
		# defaults to gmail
		host=smtp.gmail.com

		# SMTP port
		# defaults to 25
		port=587

		# Use SSL
		# for GMail, this should be set to true
		ssl=true

		# authentication user
		# Optional, comment this line if no auth
		# defaults to no auth
		user="you@gmail.com"

		# authentication password
		# Optional, comment this line to leave password blank
		# defaults to no password
		password=password
	}	
	
	play-easymail {
		from {
			# Mailing from address
			email="you@gmail.com"

			# Mailing name
			name="Your Name"

			# Seconds between sending mail through Akka (defaults to 1)
			# delay=1
		}
	}


##Creating the necessary views
---

You have to integrate Play-Authenticate into your views by yourself. Play-Authenticate provides some template helpers to do this.
You have to add one import to include theses templates:

	@import com.feth.play.module.pa.views.html._

### Login

The @forProviders helper lets you iterate over all registered providers. The following example creates a login-link for each provider:

	@forProviders() { p =>
		<a href="@p.getUrl()">@p.getKey()</a>
	}

### Logout

Creating a logout link is straight forward:

	<a href="@com.feth.play.module.pa.controllers.routes.Authenticate.logout">Logout</a>

### @currentAuth Helper

The @currentAuth helper lets you do something with the current auth provider. 
The following example checks if the user is logged in and displays an logout link if she is.

	@currentAuth() { auth =>
		@if(auth != null) {
			<a href="@com.feth.play.module.pa.controllers.routes.Authenticate.logout">Logout</a>
		}
	}

This second example displays some account information: 

	@currentAuth() { auth =>
        Logged in with provider '@auth.getProvider()' and the user ID '@auth.getId()'<br/>
        Your session expires
        @if(auth.expires() == -1){
            never
        } else {
            at @{new java.util.Date(auth.expires())}
        }
    }

##Routes
---
Add the following routes to your `conf/routes` file:

	GET     /logout                     com.feth.play.module.pa.controllers.Authenticate.logout
	GET     /authenticate/:provider     com.feth.play.module.pa.controllers.Authenticate.authenticate(provider: String)
	GET     /authenticate/:provider/denied     controllers.Application.oAuthDenied(provider: String)

The controllers for the first two routes are provided by the framework, but you have to decide how you handle
denied authentications. In the example above the controller for this is implemented by the method `oAuthDenied` in `controllers.Application`.

Below you can see an example implementation of this method:

	public static Result oAuthDenied(final String providerKey) {
		flash(FLASH_ERROR_KEY,
				"You need to accept the OAuth connection in order to use this website!");
		return redirect(routes.Application.index());
	}
	
	
##Configure the Resolver
---

Play-Authenticate needs some pages provided by your application. You configure these pages by setting a resolver
in the `onStart` method of the [Global object](http://www.playframework.org/documentation/2.0.4/JavaGlobal).

TODO explain Resolver interface and its methods
	
	import play.Application;
	import play.GlobalSettings;
	import play.mvc.Call;

	import com.feth.play.module.pa.PlayAuthenticate;
	import com.feth.play.module.pa.PlayAuthenticate.Resolver;
	import com.feth.play.module.pa.exceptions.AccessDeniedException;
	import com.feth.play.module.pa.exceptions.AuthException;

	import controllers.routes;

	public class Global extends GlobalSettings {

		public void onStart(final Application app) {
			PlayAuthenticate.setResolver(new Resolver() {

				@Override
				public Call login() {
					// Your login page
					return routes.Application.index();
				}

				@Override
				public Call afterAuth() {
					// The user will be redirected to this page after authentication
					// if no original URL was saved
					return routes.Application.index();
				}

				@Override
				public Call afterLogout() {
					return routes.Application.index();
				}

				@Override
				public Call auth(final String provider) {
					// You can provide your own authentication implementation,
					// however the default should be sufficient for most cases
					return com.feth.play.module.pa.controllers.routes.Authenticate
							.authenticate(provider);
				}

				@Override
				public Call onException(final AuthException e) {
					if (e instanceof AccessDeniedException) {
						return routes.Application
								.oAuthDenied(((AccessDeniedException) e)
										.getProviderKey());
					}

					// more custom problem handling here...

					return super.onException(e);
				}

				@Override
				public Call askLink() {
					// We don't support moderated account linking in this sample.
					// See the play-authenticate-usage project for an example
					return null;
				}

				@Override
				public Call askMerge() {
					// We don't support moderated account merging in this sample.
					// See the play-authenticate-usage project for an example
					return null;
				}
			});
		}

	}
	
	
Of course you have to create the pages to which the resolver refers by yourself.	

##User Service Plugin
---

We yet have to tell Play-Authenticate how to store users in a database. This is done by creating a sub class 
of `com.feth.play.module.pa.service.UserServicePlugin` and implementing the abstract methods of this class.
This subclass has to be registered as a plugin. To do this, create a new text file `play.plugins` in your `conf` folder 
(if it does not exist) and add the following line:

	10005:service.MyUserServicePlugin
	
In this line `10005` determines when this plugin is loaded. Plugins with a higher number are loaded first. 
`service.MyUserServicePlugin` is the fully qualified class name of your UserService class.

The `UserService` interface works with `AuthUser` objects. The combination of `getId` and `getProvider` from `AuthUser` can be used to identify an user.


- The `getLocalIdentity` function gets called on any login to check whether the session user still has a valid corresponding local user. Returns the local identifying object if the auth provider/id combination has been linked to a local user account already or null if not.
- The `save` method of the UserServicePlugin is called, when the user logs in for the first time (i.e. `getLocalIdentity` returned null for this AuthUser). This method should store the user to the database and return an object identifying the user. 
- The `update` method is called when a user logs in. You might make profile updates here with data coming from the login provider or bump a last-logged-in date.
- The `merge` function should merge two different local user accounts to one account. Returns the user to generate the session information from.
- The `link` function links a new account to an existing local user. Returns the auth user to log in with.


Here is an example implementation of the UserServicePlugin:

	package service;

	import models.User;
	import play.Application;

	import com.feth.play.module.pa.user.AuthUser;
	import com.feth.play.module.pa.user.AuthUserIdentity;
	import com.feth.play.module.pa.service.UserServicePlugin;

	public class MyUserServicePlugin extends UserServicePlugin {

		@Inject
		public MyUserServicePlugin(final Application app) {
			super(app);
		}

		@Override
		public Object save(final AuthUser authUser) {
			final boolean isLinked = User.existsByAuthUserIdentity(authUser);
			if (!isLinked) {
				return User.create(authUser).id;
			} else {
				// we have this user already, so return null
				return null;
			}
		}

		@Override
		public Object getLocalIdentity(final AuthUserIdentity identity) {
			// For production: Caching might be a good idea here...
			// ...and dont forget to sync the cache when users get deactivated/deleted
			final User u = User.findByAuthUserIdentity(identity);
			if(u != null) {
				return u.id;
			} else {
				return null;
			}
		}

		@Override
		public AuthUser merge(final AuthUser newUser, final AuthUser oldUser) {
			if (!oldUser.equals(newUser)) {
				User.merge(oldUser, newUser);
			}
			return oldUser;
		}

		@Override
		public AuthUser link(final AuthUser oldUser, final AuthUser newUser) {
			User.addLinkedAccount(oldUser, newUser);
			return null;
		}

	}

	
##Adding Authentication Providers
---

### Google Authentication Provider

To use google as an authentication provider you first have 
to get API keys for your application. You can create your key 
using the [Google API console](https://code.google.com/apis/console/). 
Go to *API Access* and *Create another client ID*. In the settings you
should add `http://localhost:9000/authenticate/google` as an 
*Authorized Redirect URI*. Of course you should adjust this URI to your 
application. 

You then have to add the GoogleAuthProvider to the list of plugins
in `conf/play.plugins`. Add the following line:

	10010:com.feth.play.module.pa.providers.oauth2.google.GoogleAuthProvider


Furthermore you need to configure the Google Auth Provider by adding 
the following to your `conf/play-authenticate/mine.conf` file (inside 
the play-authenticate block):

    google {
        redirectUri {
            # Whether the redirect URI scheme should be HTTP or HTTPS (HTTP by default)
            secure=false

            # You can use this setting to override the automatic detection
            # of the host used for the redirect URI (helpful if your service is running behind a CDN for example)
            # host=yourdomain.com
        }

        # Google credentials
        # These are mandatory for using OAuth and need to be provided by you,
        # if you want to use Google as an authentication provider.
        # Get them here: https://code.google.com/apis/console
        clientId="180332425123-lbz1g8510ohfu8ecabs10s15a21nf0k.apps.googleusercontent.com"
        clientSecret="1ao-clfNeLaXltUNbuUmBzTf"
    }

You have to replace the values for clientId and clientSecret with your 
own keys. The keys above are not valid.


TODO short description for other providers.

##Adding Access Control
---

TODO

### Using play.mvc.Security
---

TODO

### Using Deadbolt
---

TODO

