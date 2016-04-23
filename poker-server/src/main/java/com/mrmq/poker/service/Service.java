package com.mrmq.poker.service;

import com.mrmq.poker.common.proto.Rpc.RpcMessage;

public interface Service extends Runnable {
	
	public void handleRequest(Session session, RpcMessage request);
	public void onEvent(Object event);
	public void stop();
}