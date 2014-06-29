# A simple SPNEGO auth provider

This authentication provider uses the standard Kerberos support
provided by java to authenticate against a Windows Active Directory
domain. It implements the minimal functionality you need if you just
want to ensure that the request comes from a user that is
authenticated in the domain. It does not implement full AD
integration, but rather treats AD as just another simple identity
provider. It also does not get any other information from the AD
except the fact that the user is authenticated. This could be done by
performing an LDAP query in an overridden `makeAuthUser` method.

It also does not offer a fallback to the (deprecated, insecure)
NTLMSSP authentication that is part of full HTTP "Negotiate"
authentication.

Login configuration parameters must be provided as java properties,
which can be passed on the command line:

```
play -Djavax.security.auth.useSubjectCredsOnly=false -Djava.security.auth.login.config=conf/login.conf
```

To use it, you must create a service user account on the domain
controller and create a keytab for it, and then configure the
principal in `conf/login.conf`, like this:

```
com.sun.security.jgss.initiate {
  com.sun.security.auth.module.Krb5LoginModule required;
};

com.sun.security.jgss.accept {
  com.sun.security.auth.module.Krb5LoginModule
    required
    principal="HTTP/server.example.com@MYDOMAIN"
    useKeyTab=true
    keyTab="conf/server.keytab"
    storeKey=true;
};
```

The windows domain and the kdc will be taken from the `krb5.conf` file
specified on the command line, or can be configured in `mine.conf`:

```
play-authenticate {
	spnego {
		realm=EXAMPLE.COM
		kdc="192.168.1.1"
	}
}
```
