package com.mrmq.poker.gateway;

import com.mrmq.poker.gateway.bean.DepositResult;

public interface Gateway {
	public DepositResult deposit(String cardType, String cardSeri, String cardPin);
}