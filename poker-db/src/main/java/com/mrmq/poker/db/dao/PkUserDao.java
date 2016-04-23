package com.mrmq.poker.db.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mrmq.poker.db.entity.PkUser;

public class PkUserDao extends BaseHelperDao<PkUser> {
	private static Logger log = LoggerFactory.getLogger(PkUserDao.class);

	public static final String USER_ID = "userId";
	public static final String LOGIN = "login";
	public static final String STATUS = "status";
	
	
	
}