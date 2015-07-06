package dao;

// Generated Jul 4, 2015 5:57:00 PM by Hibernate Tools 4.3.1

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import models.LinkedAccount;
import models.SecurityRole;
import models.User;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser;
import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.AuthUserIdentity;
import com.feth.play.module.pa.user.EmailIdentity;
import com.feth.play.module.pa.user.FirstLastNameIdentity;
import com.feth.play.module.pa.user.NameIdentity;

/**
 * Home object for domain model class User.
 * @see models.User
 * @author Hibernate Tools
 */
public class UserHome {

	private static final Log log = LogFactory.getLog(UserHome.class);
	
	public void persist(User transientInstance, EntityManager entityManager) {
		
		EntityTransaction tx = null;
		try {
		    tx = entityManager.getTransaction();
		    tx.begin();
		    
		    entityManager.persist(transientInstance);

		    tx.commit();
		}
		catch (RuntimeException e) {
		    if ( tx != null && tx.isActive() ) tx.rollback();
		    throw e; // or display error message
		}
	}

	public void remove(User persistentInstance, EntityManager entityManager) {
		
		EntityTransaction tx = null;
		try {
		    tx = entityManager.getTransaction();
		    tx.begin();
		    
		    entityManager.remove(persistentInstance);

		    tx.commit();
		}
		catch (RuntimeException e) {
		    if ( tx != null && tx.isActive() ) tx.rollback();
		    throw e; // or display error message
		}
	}

	public User merge(User detachedInstance, EntityManager entityManager) {
		
		EntityTransaction tx = null;
		try {
		    tx = entityManager.getTransaction();
		    tx.begin();
		    
		    User result = entityManager.merge(detachedInstance);

		    tx.commit();
		    
		    return result;
		}
		catch (RuntimeException e) {
		    if ( tx != null && tx.isActive() ) tx.rollback();
		    throw e; // or display error message
		}
	}

	public User findById(Integer id, EntityManager entityManager) {
		log.debug("getting User instance with id: " + id);
		try {
			User instance = entityManager.find(User.class, id);
			log.debug("get successful");
			return instance;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}
	
	public LinkedAccount getAccountByProvider(User user, String providerKey, EntityManager entityManager) {
		LinkedAccountHome dao = new LinkedAccountHome();
		
		return dao.findByProviderKey(user, providerKey, entityManager);
	}
	
	public void changePassword(User user, UsernamePasswordAuthUser authUser, boolean create, EntityManager entityManager) {
		LinkedAccount a = this.getAccountByProvider(user, authUser.getProvider(), entityManager);
		
		LinkedAccountHome dao = new LinkedAccountHome();
		
		if (a == null) {
			if (create) {
				a = dao.create(user, authUser.getProvider(), authUser.getId(), entityManager);
				a.setUser(user);
			} else {
				throw new RuntimeException("Account not enabled for password usage");
			}
		}
		
		a.setProviderUserId(authUser.getHashedPassword());
		
		dao.merge(a, entityManager);
	}
	
	public void resetPassword(User user, UsernamePasswordAuthUser authUser, boolean create, EntityManager entityManager) {
		// You might want to wrap this into a transaction
		this.changePassword(user, authUser, create, entityManager);
		
		TokenActionHome tokenDao = new TokenActionHome();
		
		tokenDao.deleteByUser(user, "PASSWORD_RESET", entityManager);
	}
	
	public boolean existsByAuthUserIdentity( AuthUserIdentity identity, EntityManager entityManager) {
		User exp = null;
		if (identity instanceof UsernamePasswordAuthUser) {
			exp = findByUsernamePasswordIdentity((UsernamePasswordAuthUser) identity, entityManager);
		} else {
			exp = getAuthUserFind(identity, entityManager);
		}
		
		if(exp == null)
		{
			return false;
		}
		else
		{
			return true;
		}
		//return exp.findRowCount() > 0;
	}
	
	public User findByUsernamePasswordIdentity(UsernamePasswordAuthUser identity, EntityManager entityManager) {
		
		try {
			Query query = entityManager.createQuery("SELECT DISTINCT u FROM LinkedAccount l JOIN l.user u WHERE l.providerKey = :pKey AND u.email = :email AND u.active = true");
			query.setParameter("pKey", identity.getProvider());
			query.setParameter("email", identity.getEmail());
			
			User user = (User) query.getSingleResult();
			
			return user;
		} catch (NoResultException nr) {
			return null;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		} 
	}
	
    public User findByEmail(String email, EntityManager entityManager) {
		
		try {
			Query query = entityManager.createQuery("SELECT DISTINCT u FROM User u WHERE u.email = :email AND u.active = true");
			query.setParameter("email", email);
			
			User user = (User) query.getSingleResult();
			
			return user;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}
	
	private User getAuthUserFind(AuthUserIdentity identity, EntityManager entityManager) {
		
		try {
			Query query = entityManager.createQuery("SELECT DISTINCT u FROM LinkedAccount l JOIN l.user u WHERE l.providerKey = :pKey AND l.providerUserId = :userId AND u.active = true");
			query.setParameter("pKey", identity.getProvider());
			query.setParameter("userId", identity.getId());
			
			User user = (User) query.getSingleResult();
			
			return user;
		}catch (NoResultException e) {
			return null;
		}
		catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

	public User findByAuthUserIdentity(AuthUserIdentity identity, EntityManager entityManager) {
		if (identity == null) {
			return null;
		}
		if (identity instanceof UsernamePasswordAuthUser) {
			return findByUsernamePasswordIdentity((UsernamePasswordAuthUser) identity, entityManager);
		} else {
			return getAuthUserFind(identity, entityManager);
		}
	}
	
	public User create(AuthUser authUser, EntityManager entityManager) {
		User user = new User();
		
		SecurityRoleHome roleDao = new SecurityRoleHome();
		
		SecurityRole role = roleDao.findByRoleName(controllers.Application.USER_ROLE, entityManager);
		
		List<SecurityRole> roles = user.getSecurityRoles();
		roles.add(role);
		
		user.setSecurityRoles(roles);
		
		user.setActive(true);
		user.setLastLogin(new Date());

		if (authUser instanceof EmailIdentity) {
			EmailIdentity identity = (EmailIdentity) authUser;
			// Remember, even when getting them from FB & Co., emails should be
			// verified within the application as a security breach there might
			// break your security as well!
			user.setEmail(identity.getEmail());
			user.setEmailValidated(false);
		}

		if (authUser instanceof NameIdentity) {
			NameIdentity identity = (NameIdentity) authUser;
			String name = identity.getName();
			if (name != null) {
				user.setName(name);
			}
		}
		
		if (authUser instanceof FirstLastNameIdentity) {
		  FirstLastNameIdentity identity = (FirstLastNameIdentity) authUser;
		  String firstName = identity.getFirstName();
		  String lastName = identity.getLastName();
		  if (firstName != null) {
		    user.setFirstName(firstName);
		  }
		  if (lastName != null) {
		    user.setLastName(lastName);
		  }
		}
		
		user = this.merge(user, entityManager);
		
        LinkedAccountHome accountsDao = new LinkedAccountHome();
		
		accountsDao.create(user, authUser.getProvider(), authUser.getId(), entityManager);

		return user;
	}
	
	public void verify(User unverified, EntityManager entityManager) {
		// You might want to wrap this into a transaction
		unverified.setEmailValidated(true);
		this.merge(unverified, entityManager);
		
		TokenActionHome tokenDao = new TokenActionHome();
		
		tokenDao.deleteByUser(unverified, "EMAIL_VERIFICATION", entityManager);
	}
	
	public void merge(User currentUser, User otherUser, EntityManager entityManager) {
		
		Set<LinkedAccount> currentUserAccounts = currentUser.getLinkedAccounts();
		
		LinkedAccountHome linkedAccountDao = new LinkedAccountHome();
		
		for (LinkedAccount acc : otherUser.getLinkedAccounts()) {
			currentUserAccounts.add(linkedAccountDao.create(currentUser, acc.getProviderKey(), acc.getProviderUserId(), entityManager));
		}
		
		currentUser.setLinkedAccounts(currentUserAccounts);
		
		otherUser.setActive(false);
		
		this.merge(otherUser, entityManager);
		this.merge(currentUser, entityManager);
		
		// do all other merging stuff here - like resources, etc.

		// deactivate the merged user that got added to this one
		//otherUser.active = false;
		//Ebean.save(Arrays.asList(new User[] { otherUser, this }));
	}
	
	public void merge(AuthUser oldUser, AuthUser newUser, EntityManager entityManager) {
		
		User oldUserDb = this.findByAuthUserIdentity(oldUser, entityManager);
		User newUserDb = this.findByAuthUserIdentity(newUser, entityManager);
		
		this.merge(oldUserDb, newUserDb, entityManager);
	}
	
	public void addLinkedAccount(AuthUser oldUser, AuthUser newUser, EntityManager entityManager) {
		User u = this.findByAuthUserIdentity(oldUser, entityManager);
		
		LinkedAccountHome linkedAccountsDao = new LinkedAccountHome();
		
		Set<LinkedAccount> linkedAccounts = u.getLinkedAccounts();
		
		linkedAccounts.add(linkedAccountsDao.create(u, newUser.getProvider(), newUser.getId(), entityManager));
		
		u.setLinkedAccounts(linkedAccounts);
		
		this.merge(u, entityManager);
	}
}
