package com.mrmq.poker.business.sync.impl;

import com.mrmq.poker.business.sync.AbstractSyncer;
import com.mrmq.poker.business.sync.Syncer;
import com.mrmq.poker.business.wraper.AbstractWrapper;
import com.mrmq.poker.db.entity.PkCashflow;
import com.mrmq.poker.db.manager.DbManager;

public class PkCashflowSyncer extends AbstractSyncer<AbstractWrapper<PkCashflow>, PkCashflow> implements Syncer<AbstractWrapper<PkCashflow>> {
	
	public PkCashflowSyncer(DbManager<PkCashflow> dbManager) {
		this.setDbManager(dbManager);
	}
}
