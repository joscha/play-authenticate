# play-authenticate - An extensible authentication plugin for Play 2.0 (Java)

This plugin uses concepts from [securesocial2][] and [Play20StartApp][] and provides a sample containing code from [deadbolt2][].

#### Version information
**Play Authenticate currently needs Play! Framework 2.0.2 or later**

Works fine with 2.0 to 2.0.3 and there is a 2.1 branch which should work with a current 2.1 snapshot (as of 2012-09-17)

## Live demo
You can find a live demo on [heroku](https://play-authenticate.herokuapp.com/ "Play Authenticate sample app") (might not always be the latest version) and there are some screens on our [website](http://joscha.github.com/play-authenticate/).

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

* Google (OAuth2)
* Facebook (OAuth2)
* Foursquare (OAuth2)
* Twitter (OAuth1)
* OpenID (experimental)
* Email/Password (with email verification, password security/encryption is fully customizable)
* Your very own authentication provider (LDAP, DB, you-name-it) via an extensible, easy-to-use plugin mechanism based on Play Plugins

The included sample application shows how to use all of those providers.

### Languages
* English
* German (as of 2012-07-09)
* Polish (as of 2012-08-03)
* French (as of 2012-08-28,  commit 967e11e207)

## Versions
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

Copyright (c) 2012 Joscha Feth

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with the License. You may obtain a copy of the License in the LICENSE file, or at:

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.


[securesocial2]: https://github.com/jaliss/securesocial
[deadbolt2]: https://github.com/schaloner/deadbolt-2
[Play20StartApp]: https://github.com/yesnault/Play20StartApp