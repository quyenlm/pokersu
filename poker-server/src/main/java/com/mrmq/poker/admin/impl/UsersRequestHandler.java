package com.mrmq.poker.admin.impl;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mrmq.poker.common.glossary.UserGroupType;
import com.mrmq.poker.common.proto.ManagerModelProto.UserType;
import com.mrmq.poker.common.proto.ManagerServiceProto.UsersRequest;
import com.mrmq.poker.common.proto.ManagerServiceProto.UsersResponse;
import com.mrmq.poker.common.proto.Rpc.RpcMessage;
import com.mrmq.poker.common.proto.Rpc.RpcMessage.Result;
import com.mrmq.poker.db.entity.PkUser;
import com.mrmq.poker.handler.impl.AbstractHandler;
import com.mrmq.poker.manager.PokerMananger;
import com.mrmq.poker.service.Session;
import com.mrmq.poker.utils.Converter;


public class UsersRequestHandler extends AbstractHandler<UsersRequest, UsersResponse> {
	private static Logger log = LoggerFactory.getLogger(UsersRequestHandler.class);
	
	public UsersRequestHandler(RpcMessage request, Session session) {
		super(request, session);
	}

	@Override
	public void handle() throws Exception {
		log.info("[start] handle UsersRequest from LoginId: {}, requestId: {}", session.getUser().getLogin(), rpcRequest.getId());
		
		UsersResponse.Builder resBuilder = UsersResponse.newBuilder();
		resBuilder.setUserType(UserType.BOT);
    	
		Iterator<PkUser> iter = PokerMananger.getUsers().values().iterator();
		PkUser tempUser;
		while(iter.hasNext()) {
			tempUser = iter.next();
			if(UserGroupType.DEMO.getValue().equals(tempUser.getUserGroup()))
				resBuilder.addUsers(Converter.convertUser(tempUser));
		}
		
		rpcResponse.setPayloadClass(UsersResponse.getDescriptor().getName());
		rpcResponse.setPayloadData(resBuilder.build().toByteString());
		rpcResponse.setResult(Result.SUCCESS);
		
		response(rpcResponse.build());
		log.info("[end] handle UsersRequest from LoginId: {}, requestId: {}", session.getUser().getLogin(), rpcRequest.getId());
	}

	@Override
	protected UsersRequest getRequest() throws InvalidProtocolBufferException {
		return UsersRequest.parseFrom(rpcRequest.getPayloadData());
	}
}