package com.mrmq.poker.admin.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mrmq.poker.common.proto.Rpc.RpcMessage;
import com.mrmq.poker.manager.PokerMananger;
import com.mrmq.poker.service.Service;
import com.mrmq.poker.service.Session;
import com.mrmq.poker.servlet.WebSocketServerHandler;

import io.netty.channel.ChannelHandlerContext;

public class AdminHandler extends WebSocketServerHandler  {
	protected static Logger log = LoggerFactory.getLogger(AdminHandler.class);
	
	@Override
	protected void handleRpcRequest(ChannelHandlerContext ctx, RpcMessage msg) {
    	try {
    		log.info("Received RpcMessage, id: {}, service: {}, msgType: {}", msg.getId(), msg.getService(), msg.getPayloadClass());
    		
    		Session session = PokerMananger.getOrSetSession(ctx.channel());
	    	handleRpcRequest(session, (RpcMessage) msg);
    	} catch (Exception e) {
    		log.error(e.getMessage(), e);
    	}
	}
	
	private void handleRpcRequest(Session session, RpcMessage request) {
		Service service = PokerMananger.getService(request.getService());
    	if(service != null)
    		service.handleRequest(session, request);
    }
}