package com.mrmq.poker.servlet;

import com.mrmq.poker.common.proto.Rpc.RpcMessage;
import com.mrmq.poker.manager.PokerMananger;
import com.mrmq.poker.service.Service;
import com.mrmq.poker.service.Session;

import io.netty.channel.ChannelHandlerContext;

public class PokerServerHandler extends WebSocketServerHandler  {

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