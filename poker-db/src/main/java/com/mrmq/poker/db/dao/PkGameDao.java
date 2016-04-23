package com.mrmq.poker.db.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mrmq.poker.db.entity.PkGame;

public class PkGameDao extends BaseHelperDao<PkGame> {
	private static Logger log = LoggerFactory.getLogger(PkGameDao.class);

	public static final String GAME_ID = "gameId";
	public static final String STATUS = "status";
	
}