package com.mrmq.poker.db.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mrmq.poker.db.entity.PkGameHistory;

public class PkGameHistoryDao extends BaseHelperDao<PkGameHistory> {
	private static Logger log = LoggerFactory.getLogger(PkGameHistoryDao.class);

	public static final String GAME_ID = "gameId";
	public static final String STATUS = "status";
	
}