# Override default Play's validation messages

# --- Constraints
constraint.required=Wymagane
constraint.min=Minimalna wartość: {0}
constraint.max=Maksymalna wartość: {0}
constraint.minLength=Minimalna długość: {0}
constraint.maxLength=Maksymalna długość: {0}
constraint.email=Email

# --- Formats
format.date=Data (''{0}'')
format.numeric=Numeryczny
format.real=Real

# --- Errors
error.invalid=Niepoprawna wartość
error.required=To pole jest wymagane
error.number=Wymagana wartość numeryczna
error.real=Real number value expected
error.min=Musi być większe lub równe niż {0}
error.max=Musi być mniejsze lub równe niż {0}
error.minLength=Minimalna długość {0}
error.maxLength=Maksymalna długość {0}
error.email=Wymagany poprawny adres e-mail
error.pattern=Must satisfy {0}

### --- play-authenticate START

# play-authenticate: Initial translations

playauthenticate.accounts.link.success=Konto został podłączone
playauthenticate.accounts.merge.success=Konta zostały złączone

playauthenticate.verify_email.error.already_validated=Twój adres został już zweryfikowany.
playauthenticate.verify_email.error.set_email_first=Najpierw musisz podać adres e-mail.
playauthenticate.verify_email.message.instructions_sent=Instrukcje dotyczące weryfikacji adresu zostały wysłane na adres {0}.
playauthenticate.verify_email.success=Adres e-mail  ({0}) został poprawnie zweryfikowany.

playauthenticate.reset_password.message.instructions_sent=Instrukcje dotyczące przywracania hasła zostały wysłane na adres {0}.
playauthenticate.reset_password.message.email_not_verified=Twoje konto nie zostało jeszcze zweryfikowane. Na wskazany adres zostały wysłane instrukcje dotyczące weryfikacji. Dopiero po weryfikacji spróbuj przywrócić hasło w razie potrzeby.
playauthenticate.reset_password.message.no_password_account=Dla tego konta nie ustawiono jeszcze możliwości logowania za pomocą hasła.
playauthenticate.reset_password.message.success.auto_login=Twoje hasło zostało przywrócone.
playauthenticate.reset_password.message.success.manual_login=Twoje hasło zostało przywrócone. Zaloguj się ponownie z użyciem nowego hasła.

playauthenticate.change_password.error.passwords_not_same=Hasła nie są takie same.
playauthenticate.change_password.success=Hasło zostało zmienione.

playauthenticate.password.signup.error.passwords_not_same=Hasła nie są takie same.
playauthenticate.password.login.unknown_user_or_pw=Nieznany użytkownik lub złe hasło.

playauthenticate.password.verify_signup.subject=PlayAuthenticate: Zakończ rejestrację
playauthenticate.password.verify_email.subject=PlayAuthenticate: Potwierdź adres e-mail
playauthenticate.password.reset_email.subject=PlayAuthenticate: Jak ustalić nowe hasło

# play-authenticate: Additional translations

playauthenticate.login.email.placeholder=Twój adres e-mail
playauthenticate.login.password.placeholder=Podaj hasło
playauthenticate.login.password.repeat=Powtórz hasło
playauthenticate.login.title=Logowanie
playauthenticate.login.password.placeholder=Hasło
playauthenticate.login.now=Zaloguj się
playauthenticate.login.forgot.password=Nie pamiętasz hasła?
playauthenticate.login.oauth=lub zaloguj się z innym dostawcą:

playauthenticate.signup.title=Rejestracja
playauthenticate.signup.name=Imię i nazwisko
playauthenticate.signup.now=Zarejestruj się
playauthenticate.signup.oauth=lub zarejestruj się z innym dostawcą:

playauthenticate.verify.account.title=Wymagana weryfikacja adresu e-mail
playauthenticate.verify.account.before=Zanim ustawisz nowe hasło
playauthenticate.verify.account.first=musisz zweryfikować swój adres e-mail.

playauthenticate.change.password.title=Zmień hasło
playauthenticate.change.password.cta=Zmień moje hasło

playauthenticate.merge.accounts.title=Złącz konta
playauthenticate.merge.accounts.question=Czy chcesz połączyć aktualne konto ({0}) z kontem: {1}?
playauthenticate.merge.accounts.true=Tak, połącz oba konta
playauthenticate.merge.accounts.false=Nie, opuść bieżącą sesję i zaloguj się jako nowy użytkownik
playauthenticate.merge.accounts.ok=OK

playauthenticate.link.account.title=Dołącz konto
playauthenticate.link.account.question=Czy chcesz dołączyć konto ({0}) do swojego aktualnego konta użytkownika?
playauthenticate.link.account.true=Tak, dołącz to konto
playauthenticate.link.account.false=Nie, wyloguj mnie i utwórz nowe konto użytkownika
playauthenticate.link.account.ok=OK

# play-authenticate: Signup folder translations

playauthenticate.verify.email.title=Potwierdź adres e-mail
playauthenticate.verify.email.requirement=Musisz potwierdzić swój adres e-mail, aby korzytać z PlayAuthenticate
playauthenticate.verify.email.cta=Na wskazany adres została przesłana informacja. Skorzystaj z dołączonego do niej linku aby aktywować konto.

playauthenticate.password.reset.title=Przywróć hasło
playauthenticate.password.reset.cta=Przywróć moje hasło

playauthenticate.password.forgot.title=Nie pamiętam hasła
playauthenticate.password.forgot.cta=Prześlij instrukcję dot. przywracania hasła

playauthenticate.oauth.access.denied.title=Dostęp OAuth zabroniony
playauthenticate.oauth.access.denied.explanation=Jeśli chcesz używać PlayAuthenticate za pomocą OAuth, musisz zaakceptować połączenie.
playauthenticate.oauth.access.denied.alternative=Jeśli wolisz tego nie robić możesz również
playauthenticate.oauth.access.denied.alternative.cta=zarejestrować się podając nazwę użytkownika i hasło

playauthenticate.token.error.title=Błąd tokena
playauthenticate.token.error.message=Podany token stracił ważność lub nie istnieje.

playauthenticate.user.exists.title=Użytkownik istnieje
playauthenticate.user.exists.message=Ten użytkownik już istnieje.

# play-authenticate: Navigation
playauthenticate.navigation.profile=Profil
playauthenticate.navigation.link_more=Dołącz więcej dostawców
playauthenticate.navigation.logout=Wyloguj się
playauthenticate.navigation.login=Zaloguj się
playauthenticate.navigation.home=Strona główna
playauthenticate.navigation.restricted=Strona zastrzeżona
playauthenticate.navigation.signup=Zarejestruj się

# play-authenticate: Handler
playauthenticate.handler.loginfirst=Musisz się zalogować, aby uzyskać dostęp do strony ''{0}''

# play-authenticate: Profile
playauthenticate.profile.title=Profil użytkownika
playauthenticate.profile.mail=Nazywasz się {0} a twój e-mail to {1}!
playauthenticate.profile.unverified=Niezweryfikowany - kliknij
playauthenticate.profile.verified=zweryfikowany
playauthenticate.profile.providers_many=Dostawcy podłączeni do Twojego konta ({0}):
playauthenticate.profile.providers_one = Jedyny dostawca podłączony do Twojego konta:
playauthenticate.profile.logged=Do obecnego zalogowania użyto:
playauthenticate.profile.session=ID tego konta to {0} a jego sesja wygaśnie {1}
playauthenticate.profile.session_endless=ID tego konta to {0}, jego sesja nie wygasa automatycznie
playauthenticate.profile.password_change=Zmień lub ustaw hasło dla tego konta

# play-authenticate - przykład: Index page
playauthenticate.index.title=Witaj w Play Authenticate
playauthenticate.index.intro=Przykład Play Authenticate
playauthenticate.index.intro_2=Oto szablon prostej aplikacji wykorzystującej Play Authenticate.
playauthenticate.index.intro_3=Skorzystaj z powyższej nawigacji aby przetestować działanie autentykacji.
playauthenticate.index.heading=Nagłówek
playauthenticate.index.details=Szczegóły

# play-authenticate - przykład: Restricted page
playauthenticate.restricted.secrets=Tajemnice, tajemnice, tajemnice... Wszędzie tajemnice!

### --- play-authenticate END