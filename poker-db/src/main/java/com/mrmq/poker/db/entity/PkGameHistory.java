package com.mrmq.poker.db.entity;
// Generated Oct 7, 2015 11:00:33 PM by Hibernate Tools 4.0.0

import java.math.BigDecimal;
import java.util.Date;

/**
 * PkGameHistory generated by hbm2java
 */
public class PkGameHistory implements java.io.Serializable {
	public enum PkGameHistoryStatus {
		INACTIVE(0),
		FINISHED(1),
		LOCKED(2),
		PLAYING(9);
		
		private int value = 0;
		
		PkGameHistoryStatus(int value) {
			this.value = value;
		}
		
		public int getNumber() {
			return value;
		}
	}
	
	private static final long serialVersionUID = 2546762188603117887L;
	private Integer gameHistoryId;
	private String gameId;
	private int creater;
	private String players;
	private int joinPlayer;
	private int maxPlayer;
	private BigDecimal minBet;
	private BigDecimal maxBet;
	private BigDecimal totalBet;
	private String currency;
	private String comment;
	private int status;
	private Date startTime;
	private Date endTime;
	private Date updateDate;

	public PkGameHistory() {
	}

	public PkGameHistory(String gameId, int joinPlayer, int maxPlayer, BigDecimal minBet,
			BigDecimal maxBet, BigDecimal totalBet, String currency, int status, Date startTime, Date endTime,
			Date updateDate) {
		this.gameId = gameId;
		this.joinPlayer = joinPlayer;
		this.maxPlayer = maxPlayer;
		this.minBet = minBet;
		this.maxBet = maxBet;
		this.totalBet = totalBet;
		this.currency = currency;
		this.status = status;
		this.startTime = startTime;
		this.endTime = endTime;
		this.updateDate = updateDate;
	}

	public PkGameHistory(String gameId, int joinPlayer, int maxPlayer, BigDecimal minBet,
			BigDecimal maxBet, BigDecimal totalBet, String currency, String comment, int status, Date startTime,
			Date endTime, Date updateDate) {
		this.gameId = gameId;
		this.joinPlayer = joinPlayer;
		this.maxPlayer = maxPlayer;
		this.minBet = minBet;
		this.maxBet = maxBet;
		this.totalBet = totalBet;
		this.currency = currency;
		this.comment = comment;
		this.status = status;
		this.startTime = startTime;
		this.endTime = endTime;
		this.updateDate = updateDate;
	}

	public Integer getGameHistoryId() {
		return this.gameHistoryId;
	}

	public void setGameHistoryId(Integer gameHistoryId) {
		this.gameHistoryId = gameHistoryId;
	}

	public String getGameId() {
		return this.gameId;
	}

	public void setGameId(String gameId) {
		this.gameId = gameId;
	}

	public int getJoinPlayer() {
		return this.joinPlayer;
	}

	public void setJoinPlayer(int joinPlayer) {
		this.joinPlayer = joinPlayer;
	}

	public int getMaxPlayer() {
		return this.maxPlayer;
	}

	public void setMaxPlayer(int maxPlayer) {
		this.maxPlayer = maxPlayer;
	}

	public BigDecimal getMinBet() {
		return this.minBet;
	}

	public void setMinBet(BigDecimal minBet) {
		this.minBet = minBet;
	}

	public BigDecimal getMaxBet() {
		return this.maxBet;
	}

	public void setMaxBet(BigDecimal maxBet) {
		this.maxBet = maxBet;
	}

	public BigDecimal getTotalBet() {
		return this.totalBet;
	}

	public void setTotalBet(BigDecimal totalBet) {
		this.totalBet = totalBet;
	}

	public String getCurrency() {
		return this.currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getComment() {
		return this.comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public int getStatus() {
		return this.status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Date getStartTime() {
		return this.startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return this.endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public Date getUpdateDate() {
		return this.updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	public int getCreater() {
		return creater;
	}

	public void setCreater(int creater) {
		this.creater = creater;
	}

	public String getPlayers() {
		return players;
	}

	public void setPlayers(String players) {
		this.players = players;
	}

	@Override
	public String toString() {
		return "PkGameHistory [gameHistoryId=" + gameHistoryId + ", gameId=" + gameId + ", creater=" + creater
				+ ", players=" + players + ", joinPlayer=" + joinPlayer + ", maxPlayer=" + maxPlayer + ", minBet="
				+ minBet + ", maxBet=" + maxBet + ", totalBet=" + totalBet + ", currency=" + currency + ", comment="
				+ comment + ", status=" + status + ", startTime=" + startTime + ", endTime=" + endTime + ", updateDate="
				+ updateDate + "]";
	}
}