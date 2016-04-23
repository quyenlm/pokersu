package com.mrmq.poker.db.manager.impl;

import java.util.List;

import org.hibernate.Query;

import com.mrmq.poker.db.dao.PkUserDao;
import com.mrmq.poker.db.entity.PkUser;
import com.mrmq.poker.db.entity.PkUser.PkUserStatus;
import com.mrmq.poker.db.manager.DbManager;

public class PkUserManager implements DbManager<PkUser> {
	private PkUserDao pkUserDao;
	
	public PkUserDao getPkUserDao() {
		return pkUserDao;
	}

	public void setPkUserDao(PkUserDao pkUserDao) {
		this.pkUserDao = pkUserDao;
	}
	
	public List<PkUser> loadAlls() {
		List<PkUser> pkUsers;
		
		pkUsers = pkUserDao.findAllByProperty(PkUserDao.STATUS, PkUserStatus.ACTIVE.getNumber());
		
		return pkUsers;
	}
	
	public PkUser loadPkUser(String loginId) {
		List<PkUser> pkUsers;
		
		pkUsers = pkUserDao.findByProperty(PkUserDao.LOGIN, loginId);
		if(pkUsers.size() > 0)
			return pkUsers.get(0);
		
		return null;
	}

	@Override
	public void insert(PkUser instance) throws Exception {
		pkUserDao.save(instance);
	}

	@Override
	public PkUser update(PkUser instance) throws Exception {
		return pkUserDao.merge(instance);
	}

	public PkUser addBalance(Long cash, Integer userId) throws Exception {
		String sql = String.format("UPDATE pk_user SET PREV_BALANCE = BALANCE, BALANCE = BALANCE " + (cash >= 0 ? "+" : "") + "%d WHERE USER_ID = %d;", cash, userId);
		int efected = pkUserDao.executeNativeSql(sql);
		if(efected > 0)
			return pkUserDao.findById(PkUser.class, userId);
		return null;
	}
	
	@Override
	public void delete(PkUser instance) throws Exception {
		pkUserDao.delete(instance);
	}

	@Override
	public void insertBatch(List<PkUser> values) throws Exception {
		pkUserDao.insertBatch(values);
	}

	@Override
	public void updateBatch(List<PkUser> values) throws Exception {
		pkUserDao.updateBatch(values);
	}

	@Override
	public void deleteBatch(List<PkUser> values) throws Exception {
		pkUserDao.deleteBatch(values);
	}
}