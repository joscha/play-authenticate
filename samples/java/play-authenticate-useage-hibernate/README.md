# Play Authenticate sample Java application for JPA/Hibernate

Authentication providers that require further configuration parameters
are commented out in `conf/play.plugins`. Please review the configuration
in `conf/play-authenticate/mine.conf` and reenable them if you want to
use them.

Example of play authentication using Hibernate and MySql

Play version: 2.4.2
play authenticate: 0.7.0-SNAPSHOT
MySQL

This implementation does not use a managed entity manager, the developer is responsable
for closing db connections unless using @Transactional in controllers.  There are some compatibility issues using this 
implementation with @Transactional, deadbolt and the hibernate dao objects.  So its best advised to avoid using @Transactional.

So every time you use 

EntityManager em = JPA.em(JPAConstants.DB);

You need to call

em.close(); in the same function before all return statements/exceptions or you could have a connection leak.

Hibernate has been setup to use MySql, you'll need to adjust the application.conf to match your database. Hibernate will create all 
the tables automatically for you when you first run the app, but you need to create the schema/database using phphMyAdmin etc.
There is a MySql workbench project file that has the cascading setup if needed (schema.mwb).