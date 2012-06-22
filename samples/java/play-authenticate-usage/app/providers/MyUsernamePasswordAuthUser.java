package providers;

import providers.MyUsernamePasswordAuthProvider.MyLogin;
import providers.MyUsernamePasswordAuthProvider.MySignup;

import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser;
import com.feth.play.module.pa.user.NameIdentity;

public class MyUsernamePasswordAuthUser extends UsernamePasswordAuthUser implements NameIdentity {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String name;

	public MyUsernamePasswordAuthUser(final MySignup signup) {
		this((MyLogin) signup);
		this.name = signup.name;
	}
	
	public MyUsernamePasswordAuthUser(final MyLogin signup) {
		super(signup.password, signup.email);
		this.name = "";
	}

	@Override
	public String getName() {
		return name;
	}
}
