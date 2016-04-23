package com.mrmq.poker.db.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mrmq.poker.db.entity.PkCashflow;

public class PkCashflowDao extends BaseHelperDao<PkCashflow> {
	private static Logger log = LoggerFactory.getLogger(PkCashflowDao.class);

	public static final String USER_ID = "userId";
	public static final String STATUS = "status";	
	
}