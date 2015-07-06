package service;

import java.util.Date;

import javax.persistence.EntityManager;

import models.User;
import play.Application;
import play.db.jpa.JPA;

import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.AuthUserIdentity;
import com.feth.play.module.pa.service.UserServicePlugin;
import com.google.inject.Inject;

import constants.JpaConstants;
import dao.UserHome;

public class HibernateUserServicePlugin extends UserServicePlugin {

	@Inject
	public HibernateUserServicePlugin(Application app) {
		super(app);
	}

	@Override
	public Object save(AuthUser authUser) {
		
		EntityManager em = JPA.em(JpaConstants.DB);
		
		UserHome userDao = new UserHome();
		
		boolean isLinked = userDao.existsByAuthUserIdentity(authUser, em);
		if (!isLinked) {
			Integer userId = userDao.create(authUser, em).getId();
			
			em.close();
			return userId;
		} else {
			// we have this user already, so return null
			em.close();
			return null;
		}
	}

	@Override
	public Object getLocalIdentity(AuthUserIdentity identity) {
		EntityManager em = JPA.em(JpaConstants.DB);
		
		// For production: Caching might be a good idea here...
		// ...and dont forget to sync the cache when users get deactivated/deleted
		UserHome userDao = new UserHome();
		
		User u = userDao.findByAuthUserIdentity(identity, em);
		em.close();
		if(u != null) {
			return u.getId();
		} else {
			return null;
		}
	}

	@Override
	public AuthUser merge(AuthUser newUser, AuthUser oldUser) {
		
		EntityManager em = JPA.em(JpaConstants.DB);
		
		UserHome userDao = new UserHome();
		if (!oldUser.equals(newUser)) {
			userDao.merge(oldUser, newUser, em);
		}
		
		em.close();
		return oldUser;
	}

	@Override
	public AuthUser link(AuthUser oldUser, AuthUser newUser) {
		
		EntityManager em = JPA.em(JpaConstants.DB);
		
		UserHome userDao = new UserHome();
		
		userDao.addLinkedAccount(oldUser, newUser, em);
		
		em.close();
		return newUser;
	}
	
	@Override
	public AuthUser update(AuthUser knownUser) {
		EntityManager em = JPA.em(JpaConstants.DB);
		
		// User logged in again, bump last login date
		UserHome userDao = new UserHome();
		
		User u = userDao.findByAuthUserIdentity(knownUser, em);
		u.setLastLogin(new Date());
		userDao.merge(u, em);

		em.close();
		return knownUser;
	}

}
