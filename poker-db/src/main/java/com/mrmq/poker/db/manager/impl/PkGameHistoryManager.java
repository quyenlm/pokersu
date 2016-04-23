package com.mrmq.poker.db.manager.impl;

import java.util.List;

import com.mrmq.poker.db.dao.PkGameHistoryDao;
import com.mrmq.poker.db.entity.PkGameHistory;
import com.mrmq.poker.db.manager.DbManager;

public class PkGameHistoryManager implements DbManager<PkGameHistory> {
	private PkGameHistoryDao pkGameHistoryDao;
	
	public PkGameHistoryDao getPkGameHistoryDao() {
		return pkGameHistoryDao;
	}

	public void setPkGameHistoryDao(PkGameHistoryDao pkGameHistoryDao) {
		this.pkGameHistoryDao = pkGameHistoryDao;
	}

	@Override
	public List<PkGameHistory> loadAlls() throws Exception {
		throw new Exception("Not support this method");
	}
	
	@Override
	public void insert(PkGameHistory instance) throws Exception {
		pkGameHistoryDao.save(instance);
	}

	@Override
	public PkGameHistory update(PkGameHistory instance) throws Exception {
		return pkGameHistoryDao.merge(instance);
	}

	@Override
	public void delete(PkGameHistory instance) throws Exception {
		pkGameHistoryDao.delete(instance);
	}

	@Override
	public void insertBatch(List<PkGameHistory> values) throws Exception {
		pkGameHistoryDao.insertBatch(values);
	}

	@Override
	public void updateBatch(List<PkGameHistory> values) throws Exception {
		pkGameHistoryDao.updateBatch(values);
	}

	@Override
	public void deleteBatch(List<PkGameHistory> values) throws Exception {
		pkGameHistoryDao.deleteBatch(values);
	}
	
	public PkGameHistory getPkGameHistoryById(Integer id) {
		return pkGameHistoryDao.findById(PkGameHistory.class, id);
	}
}