# [![Build Status](https://travis-ci.org/joscha/play-authenticate.svg?branch=master)](https://travis-ci.org/joscha/play-authenticate) PlayAuthenticate - An extensible authentication plugin for Play! Framework 2 (Java)

This plugin uses concepts from [securesocial2][] and [Play20StartApp][] and provides a sample containing code from [deadbolt2][].

#### Version information
**Play Authenticate currently needs Play! Framework 2.0.2 or later**

Play Authenticate is cross-tested in Java 1.6 and Java 1.7

Works fine with

* `2.0.2` to `2.0.x` (last: `0.2.3-SNAPSHOT` - [2.0.x branch](https://github.com/joscha/play-authenticate/tree/2.0.x))
* `2.1.0` to `2.1.x` (last: `0.3.5-SNAPSHOT` - [2.1.x branch](https://github.com/joscha/play-authenticate/tree/2.1.x))
* `2.2.0` to `2.2.x` (last: `0.5.2-SNAPSHOT` - [2.2.x branch](https://github.com/joscha/play-authenticate/tree/2.2.x))
* `2.3.0` to `2.3.x` (last: `0.6.x-SNAPSHOT` - [master branch](https://github.com/joscha/play-authenticate/tree/master))

## Live demo
You can find a live demo on [heroku](https://play-authenticate.herokuapp.com/ "Play Authenticate sample app") (usually outdated) and there are some screens on our [website](http://joscha.github.io/play-authenticate/).

## Quickstart
Clone the project, go to `samples/java/play-authenticate-usage` and run `sbt run` to see a sample application. There are only very few of the providers enabled, because for most of them, you need to add OAuth credentials which you can get at the various different providers.

### Include the Dependencies

Play-Authenticate is available in [Maven Central](http://search.maven.org/#artifactdetails%7Ccom.feth%7Cplay-authenticate_2.11%7C0.6.8%7Cjar).

```
<dependency>
    <groupId>com.feth</groupId>
    <artifactId>play-authenticate_2.11</artifactId>
    <version>0.6.8</version>
</dependency>
```
or

```
val appDependencies = Seq(
  "com.feth" %% "play-authenticate" % "0.6.8"
  )
```

## Features

* Fully customizable and localizable controllers and views (e.g. Play Authenticate allows you to define your own controllers and views for every visual step of the signup and/or log in process)
* Completely dynamic URL generation for all views (uses the route file - means you can adapt the look and feel as much as you like).
	* The sample shows how to do this with Twitter bootstrap
* Linking of accounts (e.g. one local user with multiple authentication providers)
	* Linking can be done automatically or after asking the user (default)
* Merge detection (e.g. a user created two unconnected local accounts)
	* Account merge can be done automatically or after asking the user (default)
* Tight [deadbolt2][] authorization integration (Sample included).
* HTTPS support (for OAuth2 redirect links and email verification).
* Verification email used by Email/Password provider is fully customizable and localizable and can be sent in either text or HTML or both.

### Providers
As of now, the following authentication providers are supported out of the box:

* _OAuth 2_
  * ![Facebook](samples/java/play-authenticate-usage/public/icons/facebook-24x24.png)
  * ![Foursquare](samples/java/play-authenticate-usage/public/icons/foursquare-24x24.png)
  * ![Google](samples/java/play-authenticate-usage/public/icons/google-24x24.png)
  * Untappd
  * VK.com / VKontakte
  * Pocket
  * ![github](samples/java/play-authenticate-usage/public/icons/github-24x24.png)
  * ![Eventbrite](samples/java/play-authenticate-usage/public/icons/eventbrite-24x24.png)
* _OAuth 1_
  * ![LinkedIn](samples/java/play-authenticate-usage/public/icons/linkedin-24x24.png)
  * ![Twitter](samples/java/play-authenticate-usage/public/icons/twitter-24x24.png)
  * ![Xing](samples/java/play-authenticate-usage/public/icons/xing-24x24.png)
* ![OpenID](samples/java/play-authenticate-usage/public/icons/openid-24x24.png) OpenID
* ![E-Mail](samples/java/play-authenticate-usage/public/icons/password-24x24.png) Email/Password (with email verification, password security/encryption is fully customizable)
* [Basic Authentication](http://en.wikipedia.org/wiki/Basic_access_authentication)
* [SPNEGO](http://en.wikipedia.org/wiki/SPNEGO)
* Your very own authentication provider (LDAP, DB, you-name-it) via an extensible, easy-to-use plugin mechanism based on Play Plugins

The included sample application shows how to use all of those providers.
There is also a [sample application using Play!Authenticate with MongoDB](https://github.com/ntenisOT/play-authenticate-mongo).

### Languages
* English
* German (as of 2012-07-09)
* Polish (as of 2012-08-03)
* French (as of 2012-08-28,  commit `967e11e207`)
* Spanish (as of 2013-03-02, commit `59613c5a44`)
* Italian (as of 2013-09-21, core only, commit `b1b9e2a46b`)
* Japanese (as of 2013-11-27, commit `fd4cc56b168d9ed447943c879dd61f271158edf7`)
* Portuguese (as of 2014-07-29)

## Versions
* **TRUNK** [not released in the repository, yet]
  * Fancy contributing something? :-)
* **0.6.8** [2014-11-07]
  * Throw an exception if `Resolver` is not defined
  * Upgrade sample app to bootstrap 3.2 (thanks @joslash, @enalmada)
  * Remove obsolete repository resolvers
  * Fix NPE on cache clear during ongoing OAuth flow
* **0.6.7** [2014-11-02]
  * Maintenance release
* **0.6.6** [2014-10-29]
  * First release in Maven Central
* **0.6.6-SNAPSHOT** [2014-10-29]
  * Portuguese translation (thanks @hugotavares)
  * Updated mailer with attachment support (thanks @mkurz)
  * Added display param support for Facebook (thanks @mkurz)
  * Fixed serialization problem with memcached (thanks @dborisenko)
* **0.6.5-SNAPSHOT** [2014-07-29]
  * Compile binaries with Java 6 for better compatibility (thanks @rui-ferreira)
  * Extend Travis build matrix to include Java 6 and Java 7
* **0.6.4-SNAPSHOT** [2014-07-09]
  * Eventbrite Auth provider (thanks @thandaanda)
* **0.6.3-SNAPSHOT** [2014-07-05]
  * Fix for cache being emptied (see issue #189)
  * Update to play-easymail 0.6.3
* **0.6.2-SNAPSHOT** [2014-06-29]
  * Add Basic Auth provider (thanks @fhars)
  * Add SPNEGO Auth provider (thanks @fhars)
* **0.6.1-SNAPSHOT** [2014-06-29]
  * Add Scala 2.10.x binary to repository
  * Travis testing against Scala 2.10.x
  * Fix repository URLs (thanks @dobau)
  * Added simple OAuth2 test
  * ATTENTION: Binaries are not published in ivy style any more - please update your resolver URLs (see issue [#189](https://github.com/joscha/play-authenticate/issues/189))
* **0.6.0-SNAPSHOT** [2014-06-10]
  * First release for Play 2.3.x (thanks @jakubvalenta)
  * Japanese localization (thanks @naruoga)
* **0.5.3-SNAPSHOT** [2014-07-05]
  * Maintenance release: backports for play-easymail
  * Update to play-easymail 0.5.2
* **0.5.2-SNAPSHOT** [2013-11-11]
  * Automatically generate and use `state` parameter in OAuth2.
  * New github provider
  * German core translation
  * Fix broken OpenID provider
  * Add a default timeout for external identity providers
  * Lots of small fixes and enhancements
* **0.5.0-SNAPSHOT** [2013-10-28]
  * Prepare for Play 2.2 (thanks @smola, @tjdett, @dribba, @goryuchkin)
  * Updated to deadbolt 2.2 RC2
  * Updated play-easymail to version `0.5` (Headers can now contain hyphens, thanks @jtammen)
* **0.3.5-SNAPSHOT** [2013-10-23] _(last version for Play 2.1.x)_
  * Fixed Error handling for OAuth1 providers (thanks @vlopato)
* **0.3.4-SNAPSHOT** [2013-09-22]
  * Update to HttpClient 4.3
  * Italian translation (thanks @ironmaiden189)
  * [Pocket](https://getpocket.com) provider (thanks @ironmaiden189)
  * Xing and LinkedIn providers got more information (profile URL, ec.) (thanks @jtammen)
* **0.3.3-SNAPSHOT** [2013-09-01]
  * New authentication provider for [Untappd](https://untappd.com) (thanks @sweigardc)
  * New authentication provider for [Xing](http://www.xing.com) (thanks @jtammen)
  * Allow custom emails (thanks @jtammen)
  * Move to play-easymail 0.3 (allows for custom headers)
* **0.3.0-SNAPSHOT** [2013-07-22]
  * Bumped Apache HttpClient to version `4.2.5`
  * Bumped deadbolt2 to version `2.1-RC2`
  * New authentication provider for [VK.com](http://ww.vk.com) (thanks @dborisenko)
  * Add `refresh_token`/`expires_in` to OAuth2 and Google/Facebook providers (thanks @jayceecam)
  * Fix for Twitter API 1.1 (thanks @xjodoin)
  * Fix for facebook users that have no user name set (thanks @smola)
  * Fix for facebook error messages (issue #26, thanks @smola)
  * Add setting for Facebook user fields to retrieve. (thanks @smola)
  * Spanish translation, thanks to @petru-ghita
* **0.2.5-SNAPSHOT** [2013-02-14]
  * Fix for users signing up that shall be logged in straight away. **PLEASE NOTE: Breaking change!!! Upgrading to this version requires you to add one additional method to your custom UsernamePasswordAuthProvider. Have a look at the sample to see a default implementation that should work for almost everyone.**
* **0.2.4-SNAPSHOT** [2013-02-06]
  * First version for Play! Framework 2.1.0.
* **0.2.3-SNAPSHOT** [2012-12-17] _(last version for Play 2.0.x)_
  * Use reflection for email templates in samples to allow easier addition of new languages (thanks @biesoir for the initial version)
  * Linkedin added (thanks @tonygwu)
* **0.2.2-SNAPSHOT** [2012-11-07]
  * Fixed caching behaviour for CDNs (thanks @enalmada)
  * Added initial Getting Started guide (thanks Peter Zeller)
  * Upgraded HttpClient dependency from 4.2 to 4.2.1
  * Switch to Play 2.0.4
* **0.2.1-SNAPSHOT** [2012-10-22]
  * Added Polish translation (thanks to @biesoir)
  * Added French translation (thanks to @Mortimerp9)
  * Added simple OAuth example
  * Fixed an NPE occuring when signing up with email previously connected to a Google Account
  * Fixed serialization problem with memcache
  * Switch to Play 2.0.3
* **0.2.0-SNAPSHOT** [2012-08-01]
  * Add twitter provider (many thanks to Fred Cecilia (@naiky) for the initial version)
* **0.1.9-SNAPSHOT** [2012-07-16]
  * Fix for invalid locale formats (e.g. en-GB) coming from   Google and/or facebook OAuth.
* **0.1.8-SNAPSHOT** [2012-07-12]
  * Change: Password auth provider now gets Context passed in `buildLoginAuthUser` and `buildSignupAuthUser` so you can get the locale of the user signing up for example. This change affects prior written auth providers based on `UsernamePasswordAuthProvider` - see the sample app or just add an additional `Context` parameter to fix it.
  * More localization (emails, parts of navigation)
  * Fix of `LocaleIdentity` implementation for facebook and google.
* **0.1.7-SNAPSHOT** [2012-07-09]
  * i18n message bundle update, email formatting, use play-easymail module, fix when user has not set an email and tries to log in via password
* **0.1.6-SNAPSHOT** [2012-07-07]
  * fix bugs, added setting for overriding the redirect_uri callback for OAuth providers to enable usage through CDNs, etc.
* **0.1.5-SNAPSHOT** [2012-07-05]
  * password reset, password change, email verification
* **0.1.4-SNAPSHOT** [2012-06-29]
  * bug fixes, nice message page if OAuth access gets denied and most important support for play 2.0.2
* **0.1.3-SNAPSHOT** [2012-06-27]
  * bug fixes, state parameter for OAuth2 providers
* **0.1.2-SNAPSHOT** [2012-06-25]
  * bug fixes
* **0.1.1-SNAPSHOT** [2012-06-24]
  * lots of options, refined interface for sample app, etc.
* **0.1.0** [2012-06-19]
  * Initial release

## License

Copyright (c) 2012-2014 Joscha Feth

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with the License. You may obtain a copy of the License in the LICENSE file, or at:

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.


[securesocial2]: https://github.com/jaliss/securesocial
[deadbolt2]: https://github.com/schaloner/deadbolt-2
[Play20StartApp]: https://github.com/yesnault/Play20StartApp
