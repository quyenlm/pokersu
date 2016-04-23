package com.mrmq.poker.business;

import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mrmq.poker.business.impl.PokerBusiness;
import com.mrmq.poker.business.sync.Syncer;
import com.mrmq.poker.business.wraper.AbstractWrapper;
import com.mrmq.poker.db.entity.PkCashflow;
import com.mrmq.poker.db.entity.PkUser;
import com.mrmq.poker.db.manager.impl.PkGameHistoryManager;
import com.mrmq.poker.db.manager.impl.PkGameManager;
import com.mrmq.poker.db.manager.impl.PkUserManager;
import com.mrmq.util.IdGenerator;

public class AbstractBusiness  {
	protected static Logger log = LoggerFactory.getLogger(PokerBusiness.class);
	
	private static IdGenerator eventId = new IdGenerator();
	
	public long getEventId() {
		return eventId.next();
	}

	protected PkUserManager userManager;
	protected PkGameManager gameManager;
	protected PkGameHistoryManager gameHistoryManager;
	
	protected Syncer<AbstractWrapper<PkUser>> userSyncer;
	protected Syncer<AbstractWrapper<PkCashflow>> cashflowSyncer;
	
	public synchronized void start(ExecutorService executorService) throws Exception {
		if(userSyncer == null)
			throw new Exception("UserSyncer can not be null");
		
		if(cashflowSyncer == null)
			throw new Exception("CashflowSyncer can not be null");
		
		if(!userSyncer.isAlive())
			executorService.submit(userSyncer);
		
		if(!cashflowSyncer.isAlive())
			executorService.submit(cashflowSyncer);
		
		log.info("PokerBusiness started");
	}
	
	public PkUserManager getUserManager() {
		return userManager;
	}

	public void setUserManager(PkUserManager pkUserManager) {
		this.userManager = pkUserManager;
	}
	
	public PkGameManager getGameManager() {
		return gameManager;
	}

	public void setGameManager(PkGameManager gameManager) {
		this.gameManager = gameManager;
	}

	public PkGameHistoryManager getGameHistoryManager() {
		return gameHistoryManager;
	}

	public void setGameHistoryManager(PkGameHistoryManager gameHistoryManager) {
		this.gameHistoryManager = gameHistoryManager;
	}
	
	public Syncer<AbstractWrapper<PkUser>> getUserSyncer() {
		return userSyncer;
	}

	public void setUserSyncer(Syncer<AbstractWrapper<PkUser>> userSyncer) {
		this.userSyncer = userSyncer;
	}

	public Syncer<AbstractWrapper<PkCashflow>> getCashflowSyncer() {
		return cashflowSyncer;
	}

	public void setCashflowSyncer(Syncer<AbstractWrapper<PkCashflow>> cashflowSyncer) {
		this.cashflowSyncer = cashflowSyncer;
	}
	
	
}