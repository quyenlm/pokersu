package com.mrmq.poker.business.sync.impl;

import com.mrmq.poker.business.sync.AbstractSyncer;
import com.mrmq.poker.business.sync.Syncer;
import com.mrmq.poker.business.wraper.AbstractWrapper;
import com.mrmq.poker.db.entity.PkUser;
import com.mrmq.poker.db.manager.DbManager;

public class PkUserSyncer extends AbstractSyncer<AbstractWrapper<PkUser>, PkUser> implements Syncer<AbstractWrapper<PkUser>> {
	
	public PkUserSyncer(DbManager<PkUser> dbManager) {
		this.setDbManager(dbManager);
	}
}