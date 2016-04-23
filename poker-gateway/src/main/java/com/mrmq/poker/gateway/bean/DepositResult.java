package com.mrmq.poker.gateway.bean;

import java.math.BigDecimal;

import com.mrmq.poker.common.glossary.MsgCode;

public class DepositResult {
	private MsgCode msgCode;
	private BigDecimal amount;
	private String transactionId;
	
	public MsgCode getMsgCode() {
		return msgCode;
	}
	public void setMsgCode(MsgCode msgCode) {
		this.msgCode = msgCode;
	}
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	
	public String getTransactionId() {
		return transactionId;
	}
	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}
	
	@Override
	public String toString() {
		return "DepositResult [msgCode=" + msgCode + ", amount=" + amount
				+ ", transactionId=" + transactionId + "]";
	}
}
