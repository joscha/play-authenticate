package dao;

// Generated Jul 4, 2015 5:57:00 PM by Hibernate Tools 4.3.1

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import models.SecurityRole;
import models.TokenAction;
import models.User;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Home object for domain model class TokenAction.
 * @see models.TokenAction
 * @author Hibernate Tools
 */
public class TokenActionHome {

	private static final Log log = LogFactory.getLog(TokenActionHome.class);
	
	private final static long VERIFICATION_TIME = 7 * 24 * 3600;

	public void persist(TokenAction transientInstance, EntityManager entityManager) {
		
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

	public void remove(TokenAction persistentInstance, EntityManager entityManager) {
		
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

	public TokenAction merge(TokenAction detachedInstance, EntityManager entityManager) {
		
		EntityTransaction tx = null;
		try {
		    tx = entityManager.getTransaction();
		    tx.begin();
		    
		    TokenAction result = entityManager.merge(detachedInstance);

		    tx.commit();
		    
		    return result;
		}
		catch (RuntimeException e) {
		    if ( tx != null && tx.isActive() ) tx.rollback();
		    throw e; // or display error message
		}
	}

	public TokenAction findById(Integer id, EntityManager entityManager) {
		log.debug("getting TokenAction instance with id: " + id);
		try {
			TokenAction instance = entityManager.find(TokenAction.class, id);
			log.debug("get successful");
			return instance;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}
	
	public TokenAction create(String type, String token, User targetUser, EntityManager entityManager) {
		
		UserHome userDao = new UserHome();
		
		User user = userDao.findById(targetUser.getId(), entityManager);
		
		TokenAction ua = new TokenAction();
		ua.setUser(user);
		ua.setToken(token);
		ua.setType(type);
		Date created = new Date();
		ua.setCreatedOn(created);
		ua.setExpiresOn(new Date(created.getTime() + VERIFICATION_TIME * 1000));
		
		this.persist(ua, entityManager);
		return ua;
	}
	
	public TokenAction findByToken(String token, String type, EntityManager entityManager) {
		try {
			Query query = entityManager.createQuery("SELECT t FROM TokenAction t WHERE lower(t.token) = :token AND lower(t.type) = :type");
			query.setParameter("token", token.toLowerCase());
			query.setParameter("type", type.toLowerCase());
			
			TokenAction instance = (TokenAction) query.getSingleResult();
			log.debug("get successful");
			return instance;
		}catch (NoResultException e){
			return null;
		}
		catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}
	
	public void deleteByUser(User u, String type, EntityManager entityManager) {
		
		try {
			Query query = entityManager.createQuery("SELECT t FROM TokenAction t JOIN t.user u WHERE u = :user AND lower(t.type) = :type");
			query.setParameter("user", u);
			query.setParameter("type", type.toLowerCase());
			
			List<TokenAction> tokens = (List<TokenAction>) query.getResultList();
			
			for(TokenAction token: tokens)
			{
				this.remove(token, entityManager);
			}
			
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}
}
