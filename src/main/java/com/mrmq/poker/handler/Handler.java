package com.mrmq.poker.handler;

import com.mrmq.poker.common.proto.Rpc.RpcMessage;
import com.mrmq.poker.service.Session;

public interface Handler<T, V> extends Runnable {
	public void handle() throws Exception;
	public RpcMessage getRpcRequest();
	public Session getSession();
}