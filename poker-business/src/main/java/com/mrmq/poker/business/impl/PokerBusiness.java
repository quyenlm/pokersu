package com.mrmq.poker.business.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.mrmq.poker.business.AbstractBusiness;
import com.mrmq.poker.business.wraper.impl.PkCashflowWrapper;
import com.mrmq.poker.business.wraper.impl.PkUserWrapper;
import com.mrmq.poker.common.bean.Player;
import com.mrmq.poker.common.glossary.MsgCode;
import com.mrmq.poker.common.proto.AdminModelProto.DepositInfo;
import com.mrmq.poker.db.DbAction;
import com.mrmq.poker.db.entity.PkCashflow;
import com.mrmq.poker.db.entity.PkCashflow.PkCashflowSourceType;
import com.mrmq.poker.db.entity.PkCashflow.PkCashflowStatus;
import com.mrmq.poker.db.entity.PkCashflow.PkCashflowType;
import com.mrmq.poker.db.entity.PkGame;
import com.mrmq.poker.db.entity.PkGameHistory;
import com.mrmq.poker.db.entity.PkGameHistory.PkGameHistoryStatus;
import com.mrmq.poker.db.entity.PkUser;
import com.mrmq.poker.gateway.Gateway;
import com.mrmq.poker.gateway.GatewayFactory;
import com.mrmq.poker.gateway.GatewayFactory.GateWayType;
import com.mrmq.poker.gateway.bean.DepositResult;

public class PokerBusiness extends AbstractBusiness {
	/**
	* Update User info exclude Balance, PreBalance, Credit 
	* @param 
	**/
	public boolean updateUser(PkUser user) throws Exception {
		if(user == null)
			throw new Exception("User can not be null");
		
		boolean result = false;
		
		synchronized (user) {
			long eventId = getEventId();
			
			//Update User info
			userSyncer.put(new PkUserWrapper(DbAction.UPDATE, user.clone(), eventId));
			
			result = true;
		}
		
		return result;
	}
	
	public MsgCode deposit(PkUser user, DepositInfo depositInfo, PkCashflowSourceType sourceType) throws Exception {
		MsgCode msgCode = null;
		Gateway gateway = GatewayFactory.createGateway(GateWayType.BAO_KIM_VALUE);
		
		DepositResult depositResult = gateway.deposit(depositInfo.getBillType(), depositInfo.getBillSeri(), depositInfo.getBillNumber());
		
		if(MsgCode.SUCCESS == depositResult.getMsgCode()) {
			//Success
			msgCode = changeCashBalance(user, depositResult.getAmount().longValue(), PkCashflowType.DEPOSIT, sourceType, 1, PkCashflowStatus.ACTIVE.getNumber());
		} else {
			msgCode = MsgCode.FAIL;
			msgCode.setMsg(depositResult.getMsgCode().getMsg());
			changeCashBalance(user, depositResult.getAmount().longValue(), PkCashflowType.DEPOSIT, sourceType, 1, PkCashflowStatus.INACTIVE.getNumber());
		}
		
		return msgCode;
	}

	public MsgCode changeCashBalance(PkUser user, Long cash, PkCashflowType type, PkCashflowSourceType sourceType, Integer source, Integer status) throws Exception {
		if(user == null)
			throw new Exception("User can not be null");
		if(cash == null || cash <= 0)
			throw new Exception("Invalid cash: " + cash);
		
		MsgCode result = MsgCode.UNKNOWN;
		
		synchronized (user) {
			long eventId = getEventId();
			
			if(PkCashflowType.BET == type || PkCashflowType.WITHDRAW == type || PkCashflowType.TAX == type)
				cash = -cash;
			
			//Update balance in DB
			PkUser tempUser = userManager.addBalance(cash, user.getUserId());
			
			if(tempUser == null) {
				result = MsgCode.FAIL;
			} else {
				user.setBalance(tempUser.getBalance());
				user.setPrevBalance(tempUser.getPrevBalance());
				
				log.info("EventId={}, User {} {} {}, current balance: {}", eventId, user.getLogin(), type, cash, user.getBalance());
				
				//Insert cashflow
				PkCashflow pkCashflow = new PkCashflow();
				pkCashflow.setUserId(user.getUserId());
				pkCashflow.setAmount(new BigDecimal(cash));
				pkCashflow.setCashBalance(user.getBalance());
				pkCashflow.setPreBalance(user.getPrevBalance());
				pkCashflow.setCurrency(user.getCurrency());
				pkCashflow.setSource(source);
				pkCashflow.setSourceType(sourceType.getNumber());
				pkCashflow.setPromo(new BigDecimal("0"));
				pkCashflow.setTaxes(new BigDecimal("0"));
				pkCashflow.setType(type.getNumber());
				pkCashflow.setStatus(status);
				pkCashflow.setInputDate(new Date(System.currentTimeMillis()));
				pkCashflow.setUpdateDate(pkCashflow.getInputDate());
				
				cashflowSyncer.put(new PkCashflowWrapper(DbAction.INSERT, pkCashflow, eventId));
				result = MsgCode.SUCCESS;
			}
		}
		
		return result;
	}

	public PkGameHistory createPkGameHistory(PkGame pkGame, List<Player> players) throws Exception {
		if(players == null || players.size() <= 0)
			throw new Exception("Invalid players: " + players);
		
		Iterator<String> it = Iterables.transform(players, new Function<Player, String>() {
			public String apply(Player player) {
				return player.getLoginId();
			}
		}).iterator();
		String playerIds = Joiner.on(',').join(it);
		
		PkGameHistory pkGameHistory = new PkGameHistory();
		
		pkGameHistory.setCurrency(pkGame.getCurrency());
		pkGameHistory.setGameId(pkGame.getGameId());
		pkGameHistory.setPlayers(playerIds);
		pkGameHistory.setCreater(0); //SYSTEM
		pkGameHistory.setJoinPlayer(players.size());
		pkGameHistory.setMaxBet(pkGame.getMaxBet());
		pkGameHistory.setMinBet(pkGame.getMinBet());
		pkGameHistory.setStatus(PkGameHistoryStatus.PLAYING.getNumber());
		pkGameHistory.setTotalBet(new BigDecimal("0"));
		pkGameHistory.setMaxPlayer(pkGame.getMaxPlayer());
		pkGameHistory.setStartTime(new Date(System.currentTimeMillis()));
		pkGameHistory.setEndTime(new Date(System.currentTimeMillis() + pkGame.getTimePergame()));
		pkGameHistory.setUpdateDate(pkGameHistory.getStartTime());
		
		gameHistoryManager.insert(pkGameHistory);
		log.info("Created GameHistory: {}", pkGameHistory);
		return pkGameHistory;
	}
	
	public PkGameHistory updatePkGameHistory(Integer gameId, BigDecimal totalBet, String comment) throws Exception {
		PkGameHistory pkGameHistory  = getGameHistoryManager().getPkGameHistoryById(gameId);
        if(pkGameHistory != null) {
        	
        	pkGameHistory.setEndTime(new Date(System.currentTimeMillis()));
        	pkGameHistory.setTotalBet(totalBet);
        	pkGameHistory.setComment(comment);
        	pkGameHistory.setStatus(PkGameHistoryStatus.FINISHED.getNumber());
        	
        	getGameHistoryManager().update(pkGameHistory);
        }
        return pkGameHistory;
	}
}
