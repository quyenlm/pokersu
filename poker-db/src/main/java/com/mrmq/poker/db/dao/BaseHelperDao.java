package com.mrmq.poker.db.dao;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class BaseHelperDao<E> extends HibernateDaoSupport {
	protected static Logger log = LoggerFactory.getLogger(BaseHelperDao.class);
	
	public E findById(Class<E> clazz, Serializable id) {
		log.debug(new StringBuilder("getting ").append(clazz.getName()).append(" instance with id: ").append(id).toString());
		try {
            if(id == null) return null;
			E instance = getHibernateTemplate().get(clazz, id);
			return instance;
		} catch (RuntimeException e) {
			log.error("get failed", e);
			throw e;
		}
	}

	public void save(E instance) {
		log.debug(new StringBuilder("saving ").append(instance.getClass().getName()).append(" instance").toString());
		try {
			getHibernateTemplate().save(instance);
			log.debug("save successful");
		} catch (RuntimeException e) {
			log.error("save failed", e);
			throw e;
		}
	}
	
	public void delete(E instance) {
		log.debug(new StringBuilder("deleting ").append(instance.getClass().getName()).append(" instance").toString());
		try {
			getHibernateTemplate().delete(instance);
			log.debug("delete successful");
		} catch (RuntimeException e) {
			log.error("delete failed", e);
			throw e;
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<E> findByExample(E instance) {
		log.debug(new StringBuilder("finding ").append(instance.getClass().getName()).append(" instance by example").toString());
		try {
			List<E> esults = (List<E>) getHibernateTemplate().findByExample(instance);
			log.debug("find by example successful, esult size: "
					+ esults.size());
			return esults;
		} catch (RuntimeException e) {
			log.error("find by example failed", e);
			throw e;
		}
	}

	private String getGenericClass() {
		Type c = getClass().getGenericSuperclass();
		String s = c.toString();
		Pattern MY_PATTERN = Pattern.compile("\\<(.*?)\\>");
		Matcher m = MY_PATTERN.matcher(s);
		String generic;
		if (m.find()) {
		    generic = m.group(1);
		} else 
			generic = "null";

		return generic;
	}

	@SuppressWarnings("unchecked")
	public List<E> findByProperty(String propertyName, Object value) {
		log.debug(new StringBuilder("finding ").append(getGenericClass()).append(" instance with property: ")
				.append(propertyName).append(", value: ").append(value).toString());
		try {
			String queryString = new StringBuilder("from ").append(getGenericClass()).append(" as model where model.").append(propertyName).append("= ?").toString();
			return (List<E>) getHibernateTemplate().find(queryString, value);
		} catch (RuntimeException e) {
			log.error("find by property name failed", e);
			throw e;
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<E> findByProperty(String propertyName, String orderPropertyName, Object value) {
		log.debug(new StringBuilder("finding ").append(getGenericClass()).append(" instance with property: ")
				.append(propertyName).append(", value: ").append(value).toString());
		try {
			String queryString = new StringBuilder("from ").append(getGenericClass()).append(" as model where model.activeFlg = 1 and model.").append(propertyName).append("= ?").append(" order by model.").append(orderPropertyName).append(" ASC ").toString();
			return (List<E>) getHibernateTemplate().find(queryString, value);
		} catch (RuntimeException e) {
			log.error("find by property name failed", e);
			throw e;
		}
	}

	public E merge(E detachedInstance) {
		log.debug(new StringBuilder("merging ").append(detachedInstance.getClass().getName()).append(" instance").toString());
		try {
			E esult = getHibernateTemplate().merge(detachedInstance);
			log.debug("merge successful");
			return esult;
		} catch (RuntimeException e) {
			log.error("merge failed", e);
			throw e;
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<E> findAll() {
		log.debug(new StringBuilder("finding all ").append(getGenericClass()).append(" instances").toString());
		try {
			String queryString = new StringBuilder("from ").append(getGenericClass()).append(" as model where model.activeFlg = 1").toString();
			return (List<E>) getHibernateTemplate().find(queryString);
		} catch (RuntimeException e) {
			log.error("find all failed", e);
			throw e;
		}
	}

	public void attachDirty(E instance) {
		log.debug(new StringBuilder("attaching dirty ").append(getClass().getName()).append(" instance").toString());
		try {
			getHibernateTemplate().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException e) {
			log.error("attach failed", e);
			throw e;
		}
	}
	
	public void attachClean(E instance) {
		log.debug(new StringBuilder("attaching clean ").append(getClass().getName()).append(" instance").toString());
		try {
			getHibernateTemplate().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException e) {
			log.error("attach failed", e);
			throw e;
		}
	}
	
	public Integer executeNativeSql(final String sql) throws Exception {
		log.info(sql);
		return getHibernateTemplate().execute(new HibernateCallback<Integer>() {
			public Integer doInHibernate(Session session) throws HibernateException, SQLException {
				try {
					Query query = session.createSQLQuery(sql);
					return query.executeUpdate();
				} catch (Exception ex) {
					log.error(ex.getMessage(), ex);
					if (ex instanceof HibernateException) {
						throw new HibernateException(ex);
					} else {
						throw new SQLException(ex);
						
					}
				}					
			}
		});								
	}
	
	@SuppressWarnings("unchecked")
	public List<E> findAllByProperty(String propertyName, Object value) {
		log.debug(new StringBuilder("finding ").append(getGenericClass()).append(" instance with property: ").append(propertyName).append(", value: ").append(value).toString());
		try {
			String queryString = new StringBuilder("from ").append(getGenericClass()).append(" as model where model.").append(propertyName).append("= ?").toString();
			return (List<E>) getHibernateTemplate().find(queryString, value);
		} catch (RuntimeException e) {
			log.error("find by property name failed", e);
			throw e;
		}
	}

	public void insertBatch(List<E> listValues) {
		Session session = getHibernateTemplate().getSessionFactory().openSession();
		Transaction tx = session.beginTransaction();
		
		int count = 0;
		int batchSize = 20;
		
		for (E e : listValues) {
			count++;
			session.save(e);
		    if (count % batchSize == 0) {
		        //flush a batch of inserts and release memory
		        session.flush();
		        session.clear();
		    }
		}
		   
		tx.commit();
		session.close();
	}
	
	public void updateBatch(List<E> listValues) {
		Session session = getHibernateTemplate().getSessionFactory().openSession();
		Transaction tx = session.beginTransaction();
		
		int count = 0;
		int batchSize = 20;
		for (E e : listValues) {
			count++;
			session.update(e);
			
		    if (count % batchSize == 0) {
		        //flush a batch of inserts and release memory
		        session.flush();
		        session.clear();
		    }
		}
		   
		tx.commit();
		session.close();
	}
	
	public void deleteBatch(List<E> listValues) {
		Session session = getHibernateTemplate().getSessionFactory().openSession();
		Transaction tx = session.beginTransaction();
		
		int count = 0;
		int batchSize = 20;
		
		for (E e : listValues) {
			count++;
			session.delete(e);
		    if (count % batchSize == 0) {
		        //flush a batch of inserts and release memory
		        session.flush();
		        session.clear();
		    }
		}
		   
		tx.commit();
		session.close();
	}
}
