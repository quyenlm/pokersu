package com.mrmq.poker.admin.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mrmq.poker.common.proto.AdminServiceProto.LoginRequest;
import com.mrmq.poker.common.proto.AdminServiceProto.LoginResponse;
import com.mrmq.poker.common.proto.Rpc.RpcMessage;
import com.mrmq.poker.common.proto.Rpc.RpcMessage.Result;
import com.mrmq.poker.db.entity.PkUser;
import com.mrmq.poker.glossary.UserError;
import com.mrmq.poker.handler.impl.AbstractHandler;
import com.mrmq.poker.manager.PokerMananger;
import com.mrmq.poker.service.Session;
import com.mrmq.poker.utils.Converter;
import com.mrmq.util.CryptoHelper;


public class AdminLoginHandler extends AbstractHandler<LoginRequest, LoginResponse> {
	private static Logger log = LoggerFactory.getLogger(AdminLoginHandler.class);
	
	public AdminLoginHandler(RpcMessage request, Session session) {
		super(request, session);
	}

	@Override
	public void handle() throws Exception {
		LoginRequest request = getRequest();
		
		log.info("[start] handle LoginRequest from LoginId: {}, requestId: {}", request.getLoginId(), rpcRequest.getId());
		
		String pass = CryptoHelper.getMd5(request.getPass());
		PkUser user = PokerMananger.getUser(request.getLoginId());
		
		if(user == null) {
			rpcResponse.setResult(Result.FAIL);
			rpcResponse.setMsgCode(UserError.ACCOUNT_NOT_EXIST.getCode());
			rpcResponse.setMessage(PokerMananger.getMsg(rpcResponse.getMsgCode()));
		} else if(!pass.equals(user.getPass())) {
			rpcResponse.setResult(Result.FAIL);
			rpcResponse.setMsgCode(UserError.PASS_NOT_MATCH.getCode());
			rpcResponse.setMessage(PokerMananger.getMsg(rpcResponse.getMsgCode()));
		} else {
			session.setUser(user);
			session.setAuthenticated(true);
			
			PokerMananger.putSessionByUser(user.getLogin(), session);
			
			//Response
			LoginResponse.Builder resBuilder = LoginResponse.newBuilder();
			resBuilder.setUser(Converter.convertUser(user));
			
			rpcResponse.setPayloadClass(LoginResponse.getDescriptor().getName());
			rpcResponse.setPayloadData(resBuilder.build().toByteString());
			rpcResponse.setResult(Result.SUCCESS);
		}
		
		response(rpcResponse.build());
		log.info("[end] handle LoginRequest from LoginId: {}, requestId: {}", request.getLoginId(), rpcRequest.getId());
	}

	@Override
	protected LoginRequest getRequest() throws InvalidProtocolBufferException {
		return LoginRequest.parseFrom(rpcRequest.getPayloadData());
	}
}