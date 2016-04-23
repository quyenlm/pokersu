package com.mrmq.poker.business.wraper.impl;

import com.mrmq.poker.business.wraper.AbstractWrapper;
import com.mrmq.poker.business.wraper.Wrapper;
import com.mrmq.poker.db.DbAction;
import com.mrmq.poker.db.entity.PkCashflow;

public class PkCashflowWrapper extends AbstractWrapper<PkCashflow> implements Wrapper<PkCashflow> {
	
	public PkCashflowWrapper(DbAction action, PkCashflow value, long eventId) {
		super(action, value, eventId);
	}
	
}
