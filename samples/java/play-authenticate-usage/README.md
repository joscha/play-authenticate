# Play Authenticate sample Java - refactored


----------

###WARNING

In this version of sample model names was changed! If you are upgrading from older version of sample and using automatic Ebean's updates you **HAVE TO** backup your tables manually, as they are subject to destroy!


----------

###What's new

----------

 - Sample's structure was completely refactored to avoid conflicts and names' clashes while integrating `Play Authenticate` in existing applications (see below).
 - Added support for manual changing the language.
 - Added support for fetching default labels in case if label for selected language doesn't exist.
 - Hardcoded labels moved to the language files.
 - Added possibility to override and translate Play's validation's and constraints' messages 
 - Added Polish translation
 - This version requires Play 2.0.2

----------

###How to use it in own app?

----------

There's a checklist of things you need to copy from the sample into your app. Of course it will be much easier, if you'll do it at the beginning - on the quite fresh app. Later you will need to resolve namespace conflicts manually.


 1. Prepare a backup ;) **Especially make sure that you saved your DB data**, as Ebean can create evolution, which will drop existing data!
 2. Copy all controllers into your application. All our controllers contains `Authenticate*` in its names. The only exception is `Language` controller which is a bonus one and can be also used without Play Authenticate. ***Note:*** in this sample `Language` controller is required.
 3. Copy all models. ***Note:*** The same as in case of controllers, models' names begins with  `Authenticate*`. Therefore Ebean will create tables like `authenticate_user` instead of `user` - that's the reason why you need to backup your data before refactoring existing app.
 4. Copy `views.authenticate` package and all its content. We don't suspect any conflict in this area.
 5. Copy methods `onStart()` and `initialData()` from the `Global` object (`/app/Global.java`) if you haven't that object yet, just copy whole file, otherwise make sure you don't overwrite your previous code.
 6. Copy translated labels into your app's `messages.xy` files. All our labels are prefixed with `playauthenticate.*`
 7. Copy ***Play Authenticate*** and ***Language class*** routes, both marked with START / END comments.
 8. Copy whole `conf/play-authenticate` folder
 9. In your `conf/application.conf` file include the files from above `conf/play-authenticate`
 10. In copied `conf/play-authenticate/smtp.conf` set your SMTP account
 11. in copied `conf/play-authenticate/mine.conf` set proper credentials for required providers
 12. Copy `conf/play.plugins` into your app, remove the providers which you do not want to use in your app.
 13. Also in your  `conf/application.conf` add param `application.languageNames="First language, Second language, etc"`
 14. Copy required CSS, images and javascript from `public` directory, most probably you'll need to copy at least `public/icons` folder.
 15. Copy blocks marked with START / END from `project/Build.scala`
 16. Check if everything is working on http://localhost:9000/authenticate


----------

###Additional languages


----------

Adding new language requires few steps, there is a checklist for adding `French` language (code `fr`) (don't forget to share your translation :) ):

 1. Create new messages file with two-letter code of the language ie. `messages.fr`
 2. Copy content from `messages.en` and translate it (try to keep placeholders like `{0}` or other special chars)
 3. in `conf/application.conf` add the code to the setting ie:

        application.langs="en,de,pl,fr"
    
 4. Also in `conf/application.conf` add the native language name in the same order (it will be used for language menu):

        application.languageNames="English,Deutsch,polski,Français"

 5. In `app/views/authenticate` and its subfolders find all email templates with language suffixes in the name and create their translated copies ie:

        _verify_email_en.scala.txt    ->    _verify_email_fr.scala.txt

 6. For each set of translated templates there is a special file which selects the proper language (in this case it's `verify_email.scala.txt`) you have to add new case for Français (it should be placed **before** `case _ => {...}`:

        @defining(lang().code) { langcode =>
            @langcode match {
                case "de" => {@_verify_email_de(verificationUrl,token,name,email)}
                case "pl" => {@_verify_email_pl(verificationUrl,token,name,email)}
                case "fr" => {@_verify_email_fr(verificationUrl,token,name,email)}
                case _ => {@_verify_email_en(verificationUrl,token,name,email)}
        }}
    
 7. For testing localized templates you don't need to spam your own e-mail: for test in `conf/application.conf` comment including the `smtp.conf` and enable `smtp.mock`, it will display a mail's content in the terminal :

        # SMTP
        #include "play-authenticate/smtp.conf"
        smtp.mock=true