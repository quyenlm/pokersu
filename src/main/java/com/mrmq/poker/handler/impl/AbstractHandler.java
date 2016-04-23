package com.mrmq.poker.handler.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mrmq.poker.common.proto.Rpc.RpcMessage;
import com.mrmq.poker.handler.Handler;
import com.mrmq.poker.service.Session;

public abstract class AbstractHandler<T, V> implements Handler<T, V> {
	private static Logger log = LoggerFactory.getLogger(AbstractHandler.class);
	
	protected Session session;
	protected RpcMessage rpcRequest;
	protected RpcMessage.Builder rpcResponse;
	
	public AbstractHandler(RpcMessage request, Session session) {
		this.rpcRequest = request;
		this.rpcResponse = request.toBuilder().clearPayloadClass().clearPayloadData();
		this.session = session;
	}
	
	public void run() {
		try {
			handle();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public abstract void handle() throws Exception;
	
	protected abstract T getRequest() throws InvalidProtocolBufferException;
	
	public RpcMessage getRpcRequest() {
		return rpcRequest;
	}
	
	public Session getSession() {
		return this.session;
	}
	
	public void response(RpcMessage rpcResponse) {
		if(session != null) {
			session.send(rpcResponse);
		}
	}
}