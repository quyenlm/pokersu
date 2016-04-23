package com.mrmq.poker.business.wraper.impl;

import com.mrmq.poker.business.wraper.AbstractWrapper;
import com.mrmq.poker.business.wraper.Wrapper;
import com.mrmq.poker.db.DbAction;
import com.mrmq.poker.db.entity.PkUser;

public class PkUserWrapper extends AbstractWrapper<PkUser> implements Wrapper<PkUser> {
	
	public PkUserWrapper(DbAction action, PkUser value, long eventId) {
		super(action, value, eventId);
	}
	
}
