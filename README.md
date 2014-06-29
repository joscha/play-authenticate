# play-authenticate - An extensible authentication plugin for Play! Framework 2 (Java)

This plugin uses concepts from [securesocial2][] and [Play20StartApp][] and provides a sample containing code from [deadbolt2][].

#### Version information
**Play Authenticate currently needs Play! Framework 2.0.2 or later**

Works fine with `2.0` to `2.0.x`, `2.1.0` to `2.1.x` and `2.2.0` to `2.2.2`.

> **Attention 2.0.x developers**  
>_There is a `0.2.3-SNAPSHOT` binary for Play `2.0.x` and an accompanying sample application in the [2.0.x branch](https://github.com/joscha/play-authenticate/tree/2.0.x). This sample differs substantially from the version for `2.1` though, because the deadbolt dependency changed. So if you plan on releasing for `2.0.x` don't take the `2.1` sample from the master stream and vice versa._

> **Attention 2.1.x developers**  
>_There is a [2.1.x branch](https://github.com/joscha/play-authenticate/tree/2.1.x). The first version to support Play 2.1 is play-authenticate version `0.2.4`, the last one `0.3.5`._

### Build status
[![Build Status](https://travis-ci.org/joscha/play-authenticate.png?branch=master)](https://travis-ci.org/joscha/play-authenticate)

## Live demo
You can find a live demo on [heroku](https://play-authenticate.herokuapp.com/ "Play Authenticate sample app") (might not always be the latest version) and there are some screens on our [website](http://joscha.github.io/play-authenticate/).

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
  * Facebook
  * Foursquare
  * Google
  * Untappd
  * VK.com / VKontakte
  * Pocket
  * github
* _OAuth 1_
  * LinkedIn
  * Twitter
  * Xing
* OpenID (experimental)
* Email/Password (with email verification, password security/encryption is fully customizable)
* Your very own authentication provider (LDAP, DB, you-name-it) via an extensible, easy-to-use plugin mechanism based on Play Plugins

The included sample application shows how to use all of those providers.

### Languages
* English
* German (as of 2012-07-09)
* Polish (as of 2012-08-03)
* French (as of 2012-08-28,  commit `967e11e207`)
* Spanish (as of 2013-03-02, commit `59613c5a44`)
* Italian (as of 2013-09-21, core only, commit `b1b9e2a46b`)
* Japanese (as of 2013-11-27, commit `fd4cc56b168d9ed447943c879dd61f271158edf7`)

## Versions
* **TRUNK** [not released in the repository, yet]
  **0.6.1-SNAPSHOT** [2014-06-29]
  * Add Scala 2.10.x binary to repository
  * Test against Scala 2.10.x
  * Fix repository URLs
  * Added simple OAuth2 test
* **0.6.0-SNAPSHOT** [2014-06-10]
  * First release for Play 2.3.x
  * Japanese localization (thanks @naruoga)
  * wanna contribute something? :)
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
