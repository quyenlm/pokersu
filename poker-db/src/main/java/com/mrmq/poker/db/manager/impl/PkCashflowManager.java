package com.mrmq.poker.db.manager.impl;

import java.util.List;

import com.mrmq.poker.db.dao.PkCashflowDao;
import com.mrmq.poker.db.entity.PkCashflow;
import com.mrmq.poker.db.manager.DbManager;

public class PkCashflowManager implements DbManager<PkCashflow> {
	private PkCashflowDao pkCashflowDao;
	
	public PkCashflowDao getPkCashflowDao() {
		return pkCashflowDao;
	}

	public void setPkCashflowDao(PkCashflowDao pkUserDao) {
		this.pkCashflowDao = pkUserDao;
	}

	@Override
	public List<PkCashflow> loadAlls() throws Exception {
		throw new Exception("Not support this method");
	}
	
	@Override
	public void insert(PkCashflow instance) throws Exception {
		pkCashflowDao.save(instance);
	}

	@Override
	public PkCashflow update(PkCashflow instance) throws Exception {
		return pkCashflowDao.merge(instance);
	}

	@Override
	public void delete(PkCashflow instance) throws Exception {
		pkCashflowDao.delete(instance);
	}

	@Override
	public void insertBatch(List<PkCashflow> values) throws Exception {
		pkCashflowDao.insertBatch(values);
	}

	@Override
	public void updateBatch(List<PkCashflow> values) throws Exception {
		pkCashflowDao.updateBatch(values);
	}

	@Override
	public void deleteBatch(List<PkCashflow> values) throws Exception {
		pkCashflowDao.deleteBatch(values);
	}
}