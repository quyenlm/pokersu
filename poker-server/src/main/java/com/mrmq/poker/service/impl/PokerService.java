package com.mrmq.poker.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mrmq.poker.common.proto.AdminServiceProto.LoginRequest;
import com.mrmq.poker.common.proto.AdminServiceProto.UpdateUserRequest;
import com.mrmq.poker.common.proto.PokerServiceProto.JoinTableRequest;
import com.mrmq.poker.common.proto.PokerServiceProto.PlayerActionRequest;
import com.mrmq.poker.common.proto.PokerServiceProto.RoomRequest;
import com.mrmq.poker.common.proto.PokerServiceProto.TableRequest;
import com.mrmq.poker.common.proto.Rpc.RpcMessage;
import com.mrmq.poker.common.proto.Rpc.RpcMessage.Result;
import com.mrmq.poker.handler.Handler;
import com.mrmq.poker.handler.impl.ActionRequestHandler;
import com.mrmq.poker.handler.impl.GetRoomHandler;
import com.mrmq.poker.handler.impl.GetTableHandler;
import com.mrmq.poker.handler.impl.JoinTableHandler;
import com.mrmq.poker.handler.impl.LoginHandler;
import com.mrmq.poker.handler.impl.UpdateUserHandler;
import com.mrmq.poker.manager.PokerMananger;
import com.mrmq.poker.service.Service;
import com.mrmq.poker.service.Session;
import com.mrmq.poker.servlet.PokerServerHandler;
import com.mrmq.util.StringHelper;

public class PokerService implements Service {
	private static Logger log = LoggerFactory.getLogger(PokerServerHandler.class);
	private boolean isAlive = true;
	
	public void handleRequest(Session session, RpcMessage request) {
		if(StringHelper.isEmpty(request.getPayloadClass()) || !request.hasPayloadData()) {
			log.warn("RpcMessage invalid, id: {}, payloadClass: {}, payloadData: {}", 
					request.getId(), request.getPayloadClass(), request.getPayloadData());
			return;
		}
		
		String payloadClass = request.getPayloadClass();
		
		//Authenticate User
		if(!session.isAuthenticated() && !LoginRequest.getDescriptor().getName().equals(payloadClass)
				&& !UpdateUserRequest.getDescriptor().getName().equals(payloadClass)) {
			log.info("{} Not authenticated", session);
			
			RpcMessage.Builder rpc = request.toBuilder();
			rpc.setResult(Result.NOT_AUTHENTICATED);
			session.send(rpc.build());
			return;
		}
		
		Handler<?, ?> handler = null;
		
		if(PlayerActionRequest.getDescriptor().getName().equals(payloadClass))
			handler = new ActionRequestHandler(request, session);
		else if(LoginRequest.getDescriptor().getName().equals(payloadClass))
			handler = new LoginHandler(request, session);
		else if(JoinTableRequest.getDescriptor().getName().equals(payloadClass))
			handler = new JoinTableHandler(request, session);
		else if(RoomRequest.getDescriptor().getName().equals(payloadClass))
			handler = new GetRoomHandler(request, session);
		else if(TableRequest.getDescriptor().getName().equals(payloadClass))
			handler = new GetTableHandler(request, session);
		else if(UpdateUserRequest.getDescriptor().getName().equals(payloadClass))
			handler = new UpdateUserHandler(request, session);
		
		if(handler != null) {
			//Start execute
			PokerMananger.getExecutorService().submit(handler);
		}
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onEvent(Object event) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void stop() {
		this.isAlive = false;
	}
}