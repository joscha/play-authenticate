package models;

import be.objectify.deadbolt.models.Permission;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Initial version based on work by Steve Chaloner (steve@objectify.be) for
 * Deadbolt2
 */
@Entity
public class AuthenticateUserPermission extends Model implements Permission {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Id
    public Long id;

    public String value;

    public static final Finder<Long, AuthenticateUserPermission> find = new Finder<Long, AuthenticateUserPermission>(
            Long.class, AuthenticateUserPermission.class);

    public String getValue() {
        return value;
    }

    public static AuthenticateUserPermission findByValue(String value) {
        return find.where().eq("value", value).findUnique();
    }
}
