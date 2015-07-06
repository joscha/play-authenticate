package dao;

// Generated Jul 4, 2015 5:57:00 PM by Hibernate Tools 4.3.1

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import models.SecurityRole;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import play.db.jpa.JPA;

/**
 * Home object for domain model class SecurityRole.
 * @see models.SecurityRole
 * @author Hibernate Tools
 */
public class SecurityRoleHome {

	private static final Log log = LogFactory.getLog(SecurityRoleHome.class);

	public void persist(SecurityRole transientInstance, EntityManager entityManager) {
		
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

	public void remove(SecurityRole persistentInstance, EntityManager entityManager) {
		
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

	public SecurityRole merge(SecurityRole detachedInstance, EntityManager entityManager) {
		
		EntityTransaction tx = null;
		try {
		    tx = entityManager.getTransaction();
		    tx.begin();
		    
		    SecurityRole result = entityManager.merge(detachedInstance);

		    tx.commit();
		    
		    return result;
		}
		catch (RuntimeException e) {
		    if ( tx != null && tx.isActive() ) tx.rollback();
		    throw e; // or display error message
		}
	}

	public SecurityRole findById(Integer id, EntityManager entityManager) {
		log.debug("getting SecurityRole instance with id: " + id);
		try {
			SecurityRole instance = entityManager.find(SecurityRole.class, id);
			log.debug("get successful");
			return instance;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}
	
	public SecurityRole findByRoleName(String name, EntityManager entityManager) {
		try {
			Query query = entityManager.createQuery("SELECT r FROM SecurityRole r WHERE r.roleName = :name");
			query.setParameter("name", name);
			
			SecurityRole instance = (SecurityRole) query.getSingleResult();
			log.debug("get successful");
			return instance;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}
	
	public Boolean hasInitialData(EntityManager entityManager) {
		//log.debug("getting SecurityRole instance with id: " + id);
		try {
			Query query = entityManager.createQuery("SELECT COUNT(*) FROM SecurityRole");
			
			Long count = (Long) query.getSingleResult();
			
			if(count.intValue() == 0)
			{
				return false;
			}
			else
			{
				return true;
			}
			
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}
}
