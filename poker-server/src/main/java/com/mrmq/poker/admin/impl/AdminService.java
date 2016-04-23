package com.mrmq.poker.admin.impl;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mrmq.poker.common.proto.AdminServiceProto.LoginRequest;
import com.mrmq.poker.common.proto.AdminServiceProto.UpdateUserRequest;
import com.mrmq.poker.common.proto.ManagerServiceProto.JoinTableRequestEvent;
import com.mrmq.poker.common.proto.ManagerServiceProto.UsersRequest;
import com.mrmq.poker.common.proto.PokerModelProto.Table;
import com.mrmq.poker.common.proto.Rpc.RpcMessage;
import com.mrmq.poker.common.proto.Rpc.RpcMessage.Result;
import com.mrmq.poker.game.poker.PokerTable;
import com.mrmq.poker.handler.Handler;
import com.mrmq.poker.manager.PokerMananger;
import com.mrmq.poker.service.Service;
import com.mrmq.poker.service.Session;
import com.mrmq.poker.utils.Converter;
import com.mrmq.util.StringHelper;

public class AdminService implements Service {
	private static Logger log = LoggerFactory.getLogger(AdminService.class);
	private BlockingQueue<Object> queue = new LinkedBlockingQueue<>();
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
		
		if(LoginRequest.getDescriptor().getName().equals(payloadClass))
			handler = new AdminLoginHandler(request, session);
		else if(UsersRequest.getDescriptor().getName().equals(payloadClass))
			handler = new UsersRequestHandler(request, session);
		
		if(handler != null) {
			//Start execute
			PokerMananger.getExecutorService().submit(handler);
		}
	}

	@Override
	public void run() {
		Object event;
		
		while(isAlive) {
			try {
				event = queue.poll(10000, TimeUnit.MILLISECONDS);
				if(event != null) {
					if(event instanceof PokerTable) {
						PokerTable eventTable = (PokerTable) event;
						JoinTableRequestEvent.Builder joinTableRequestEvent = JoinTableRequestEvent.newBuilder();
						
						final PokerTable pokerTable = PokerMananger.getTable(eventTable.getTableId());
						if(pokerTable != null) {
							Table table = Converter.convertTable(pokerTable);
							joinTableRequestEvent.setTable(table);
							
							Session adminSession = PokerMananger.getSessionByUser("admin");
							if(adminSession != null)
								adminSession.send(PokerMananger.createRpcMessage(System.currentTimeMillis(),
										JoinTableRequestEvent.getDescriptor().getName(), 
										joinTableRequestEvent.build().toByteString()).build());
						} else
							log.warn("");
					}
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	@Override
	public void onEvent(Object event) {
		if(event == null)
			return;
		queue.add(event);
	}

	@Override
	public void stop() {
		this.isAlive = false;
	}
}