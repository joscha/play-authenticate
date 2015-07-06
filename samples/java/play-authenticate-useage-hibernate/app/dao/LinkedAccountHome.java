package dao;

// Generated Jul 4, 2015 5:57:00 PM by Hibernate Tools 4.3.1

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import models.LinkedAccount;
import models.SecurityRole;
import models.User;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.feth.play.module.pa.user.AuthUser;

/**
 * Home object for domain model class LinkedAccount.
 * @see models.LinkedAccount
 * @author Hibernate Tools
 */
public class LinkedAccountHome {

	private static final Log log = LogFactory.getLog(LinkedAccountHome.class);

	public void persist(LinkedAccount transientInstance, EntityManager entityManager) {
		
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

	public void remove(LinkedAccount persistentInstance, EntityManager entityManager) {
		
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

	public LinkedAccount merge(LinkedAccount detachedInstance, EntityManager entityManager) {
		
		EntityTransaction tx = null;
		try {
		    tx = entityManager.getTransaction();
		    tx.begin();
		    
		    LinkedAccount result = entityManager.merge(detachedInstance);

		    tx.commit();
		    
		    return result;
		}
		catch (RuntimeException e) {
		    if ( tx != null && tx.isActive() ) tx.rollback();
		    throw e; // or display error message
		}
	}

	public LinkedAccount findById(Integer id, EntityManager entityManager) {
		log.debug("getting LinkedAccount instance with id: " + id);
		try {
			LinkedAccount instance = entityManager.find(LinkedAccount.class, id);
			log.debug("get successful");
			return instance;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}
	
	public LinkedAccount findByProviderKey(User user, String key, EntityManager entityManager) {
		
		try {
			Query query = entityManager.createQuery("SELECT l FROM LinkedAccount l WHERE l.providerKey = :pKey AND l.user = :user");
			query.setParameter("pKey", key);
			query.setParameter("user", user);
			
			LinkedAccount linkedAccount = (LinkedAccount) query.getSingleResult();
			
			return linkedAccount;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}
	
	/*public LinkedAccount create(AuthUser authUser, EntityManager entityManager) {
		LinkedAccount ret = new LinkedAccount();
		this.update(ret, authUser, entityManager);
		return ret;
	}*/
	
	public LinkedAccount create(User userAccount, String providerKey, String providerUserId, EntityManager entityManager) {
		LinkedAccount ret = new LinkedAccount();
		ret.setUser(userAccount);
		ret.setProviderKey(providerKey);
		ret.setProviderUserId(providerUserId);
		
		//this.update(ret, authUser, entityManager);
		return this.merge(ret, entityManager);
	}
	
	public void update(LinkedAccount linkedAccount, AuthUser authUser, EntityManager entityManager) {
		
		linkedAccount.setProviderKey(authUser.getProvider());
		linkedAccount.setProviderUserId(authUser.getId());
		
		this.merge(linkedAccount, entityManager);
	}

	/*public LinkedAccount create(LinkedAccount acc, EntityManager entityManager) {
		LinkedAccount ret = new LinkedAccount();
		
		ret.setProviderKey(acc.getProviderKey());
		ret.setProviderUserId(acc.getProviderUserId());
		this.persist(ret, entityManager);

		return ret;
	}*/
}
