package com.mrmq.poker.db.manager.impl;

import java.util.List;

import com.mrmq.poker.db.dao.PkGameDao;
import com.mrmq.poker.db.entity.PkGame;
import com.mrmq.poker.db.entity.PkGame.PkGameStatus;
import com.mrmq.poker.db.manager.DbManager;

public class PkGameManager implements DbManager<PkGame> {
	private PkGameDao pkGameDao;
	
	public PkGameDao getPkGameDao() {
		return pkGameDao;
	}

	public void setPkGameDao(PkGameDao pkGameDao) {
		this.pkGameDao = pkGameDao;
	}

	public List<PkGame> loadAlls() {
		List<PkGame> pkGames;
		
		pkGames = pkGameDao.findAllByProperty(PkGameDao.STATUS, PkGameStatus.ACTIVE.getNumber());
		
		return pkGames;
	}
	
	@Override
	public void insert(PkGame instance) throws Exception {
		pkGameDao.save(instance);
	}

	@Override
	public PkGame update(PkGame instance) throws Exception {
		return pkGameDao.merge(instance);
	}

	@Override
	public void delete(PkGame instance) throws Exception {
		pkGameDao.delete(instance);
	}

	@Override
	public void insertBatch(List<PkGame> values) throws Exception {
		pkGameDao.insertBatch(values);
	}

	@Override
	public void updateBatch(List<PkGame> values) throws Exception {
		pkGameDao.updateBatch(values);
	}

	@Override
	public void deleteBatch(List<PkGame> values) throws Exception {
		pkGameDao.deleteBatch(values);
	}
}