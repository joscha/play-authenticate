package providers;

import providers.PaUsernamePasswordAuthProvider.MySignup;

import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser;
import com.feth.play.module.pa.user.NameIdentity;

public class PaUsernamePasswordAuthUser extends UsernamePasswordAuthUser
        implements NameIdentity {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private final String name;

    public PaUsernamePasswordAuthUser(final MySignup signup) {
        super(signup.password, signup.email);
        this.name = signup.name;
    }

    /**
     * Used for password reset only - do not use this to signup a user!
     * @param password
     */
    public PaUsernamePasswordAuthUser(final String password) {
        super(password, null);
        name = null;
    }

    @Override
    public String getName() {
        return name;
    }
}
