# Override default Play's validation messages

# --- Constraints
constraint.required=Обязательно для заполнения
constraint.min=Минимальное значение: {0}
constraint.max=Максимальное значение: {0}
constraint.minLength=Минимальная длина: {0}
constraint.maxLength=Максимальная длина: {0}
constraint.email=Email

# --- Formats
format.date=Дата (''{0}'')
format.numeric=Число
format.real=Вещественное число

# --- Errors
error.invalid=Неверное значение
error.required=Это поле необходимо для заполнения
error.number=Необходимо числовое значение
error.real=Необходимо вещественное числовое значение
error.min=Должно быть больше или равно {0}
error.max=Должно быть меньше или равно {0}
error.minLength=Минимальная длина значения - {0}
error.maxLength=Максимальная длина значения - {0}
error.email=Необходим действительный Email адрес
error.pattern=Значение должно удовлетворять {0}

### --- play-authenticate START

# play-authenticate: Initial translations

playauthenticate.accounts.link.success=Аккаунт успешно присоединён
playauthenticate.accounts.merge.success=Аккаунты успешно объединены

playauthenticate.verify_email.error.already_validated=Ваш Email уже был подтверждён.
playauthenticate.verify_email.error.set_email_first=Вы должны сначала указать свой Email.
playauthenticate.verify_email.message.instructions_sent=Инструкции по подтверждению Вашего Email были отправлены на адрес {0}.
playauthenticate.verify_email.success=Email адрес ({0}) успешно подтверждён.

playauthenticate.reset_password.message.instructions_sent=Инструкции по смене Вашего пароля были отправлены на адрес {0}.
playauthenticate.reset_password.message.email_not_verified=Ваш Email ещё не подтверждён.  Письмо с инструкциями по подтверждению уже было было отправлено. Попробуйте сбросить Ваш пароль позже.
playauthenticate.reset_password.message.no_password_account=На Вашем аккаунте ещё не установлен пароль.
playauthenticate.reset_password.message.success.auto_login=Ваш пароль был сброшен.
playauthenticate.reset_password.message.success.manual_login=Ваш пароль был сброшен. Пожалуйста, авторизуйтесь, используя новый пароль.

playauthenticate.change_password.error.passwords_not_same=Пароли не совпадают.
playauthenticate.change_password.success=Пароль был успешно изменён.

playauthenticate.password.signup.error.passwords_not_same=Пароли не совпадают.
playauthenticate.password.login.unknown_user_or_pw=Неверное имя пользователя или пароль.

playauthenticate.password.verify_signup.subject=PlayAuthenticate: Завершение регистрации
playauthenticate.password.verify_email.subject=PlayAuthenticate: Подтверждение Email адреса
playauthenticate.password.reset_email.subject=PlayAuthenticate: Сброс пароля

# play-authenticate: Additional translations

playauthenticate.login.email.placeholder=Ваш Email
playauthenticate.login.password.placeholder=Пароль
playauthenticate.login.password.repeat=Повторите пароль
playauthenticate.login.title=Войти
playauthenticate.login.now=Войти сейчас
playauthenticate.login.forgot.password=Забыли пароль?
playauthenticate.login.oauth=или войдите используя следующие способы входа:
playauthenticate.login.basic=или попробуйте авторизацию HTTP Basic

playauthenticate.signup.title=Регистрация
playauthenticate.signup.name=Ваше имя
playauthenticate.signup.now=Зарегистрироваться сейчас
playauthenticate.signup.oauth=или зарегистрируйтесь, используя следующие способы входа:

playauthenticate.verify.account.title=Требуется подтверждение Email
playauthenticate.verify.account.before=Перед установкой пароля Вы должны
playauthenticate.verify.account.first=сначала подтвердить свой Email

playauthenticate.change.password.title=Смените здесь свой пароль
playauthenticate.change.password.cta=Сменить пароль

playauthenticate.merge.accounts.title=Объединение аккаунтов
playauthenticate.merge.accounts.question=Вы хотите объединить Ваш текущий аккаунт ({0}) с этим аккаунтом: {1}?
playauthenticate.merge.accounts.true=Да, объединить эти два аккаунта
playauthenticate.merge.accounts.false=Нет, выйти из текущего аккаунта и войти как новый пользователь
playauthenticate.merge.accounts.ok=OK

playauthenticate.link.account.title=Присоединение аккаунта
playauthenticate.link.account.question=Присоединить аккаунт ({0}) к Вашему пользователю?
playauthenticate.link.account.true=Да, присоединить этот аккаунт
playauthenticate.link.account.false=Нет, выйти и создать нового пользователя с этим аккаунтом
playauthenticate.link.account.ok=OK

# play-authenticate: Signup folder translations

playauthenticate.verify.email.title=Подтверждение Вашего Email
playauthenticate.verify.email.requirement=Перед использованием PlayAuthenticate Вам сначала необходимо подтвердить Ваш Email адрес.
playauthenticate.verify.email.cta=На зарегистрированный адрес было отправлено письмо. Пожалуйста, перейдите по ссылке в письме, чтобы активировать Ваш аккаунт.

playauthenticate.password.reset.title=Сброс пароля
playauthenticate.password.reset.cta=Сбросить пароль

playauthenticate.password.forgot.title=Забыли пароль?
playauthenticate.password.forgot.cta=Отправить инструкции по восстановлению

playauthenticate.oauth.access.denied.title=Доступ через OAuth запрещён
playauthenticate.oauth.access.denied.explanation=Если Вы хотите использовать PlayAuthenticate с OAuth, Вы должны подтвердить соединение.
playauthenticate.oauth.access.denied.alternative=Если вы предпочитаете не делать этого, вы можете также
playauthenticate.oauth.access.denied.alternative.cta=войти с именем пользователя и паролем 

playauthenticate.token.error.title=Ошибка токена
playauthenticate.token.error.message=Данный токен или истёк, или не существует.

playauthenticate.user.exists.title=Пользователь существует
playauthenticate.user.exists.message=Пользователь уже существует.

# play-authenticate: Navigation
playauthenticate.navigation.profile=Профиль
playauthenticate.navigation.link_more=Присоединить другие провайдеры
playauthenticate.navigation.logout=Выйти
playauthenticate.navigation.login=Войти
playauthenticate.navigation.home=Главная
playauthenticate.navigation.restricted=Страница с ограниченным доступом
playauthenticate.navigation.signup=Регистрация

# play-authenticate: Handler
playauthenticate.handler.loginfirst=Чтобы просматривать ''{0}'', вы должны сначала войти

# play-authenticate: Profile
playauthenticate.profile.title=Профиль пользователя
playauthenticate.profile.mail=Ваше имя - {0}, и ваш Email адрес {1}!
playauthenticate.profile.unverified=не подтвержден - нажмите для подтверждения
playauthenticate.profile.verified=подтвержден
playauthenticate.profile.providers_many={0} провайдеров присоединено к Вашему аккаунту:
playauthenticate.profile.providers_one = Один провайдер присоединен к Вашему аккаунту:
playauthenticate.profile.logged=Сейчас вы вошли через:
playauthenticate.profile.session=Ваш ID пользователя {0}, и сессия истекает {1}
playauthenticate.profile.session_endless=Ваш ID пользователя {0}, и сессия не истечет, так как она бесконечная
playauthenticate.profile.password_change=Сменить / установить пароль для Вашего аккаунта

# play-authenticate - sample: Index page
playauthenticate.index.title=Добро пожаловать в Play Authenticate
playauthenticate.index.intro=Демонстрационное приложение Play Authenticate
playauthenticate.index.intro_2=Это шаблон простого приложения с аутентификацией.
playauthenticate.index.intro_3=Изучите меню сверху, чтобы посмотреть простые примеры страниц с поддерживаемыми способами аутентификации.
playauthenticate.index.heading=Заголовок
playauthenticate.index.details=Посмотреть детали

# play-authenticate - sample: Restricted page
playauthenticate.restricted.secrets=Секреты, везде!

### --- play-authenticate END
