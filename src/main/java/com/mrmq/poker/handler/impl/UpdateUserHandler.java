package com.mrmq.poker.handler.impl;

import java.math.BigDecimal;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mrmq.poker.common.proto.AdminModelProto.User;
import com.mrmq.poker.common.proto.AdminServiceProto.UpdateUserRequest;
import com.mrmq.poker.common.proto.AdminServiceProto.UpdateUserRequest.UserRequestCommand;
import com.mrmq.poker.common.proto.AdminServiceProto.UpdateUserResponse;
import com.mrmq.poker.common.proto.Rpc.RpcMessage;
import com.mrmq.poker.common.proto.Rpc.RpcMessage.Result;
import com.mrmq.poker.db.entity.PkUser;
import com.mrmq.poker.db.entity.PkUser.PkUserGroup;
import com.mrmq.poker.db.entity.PkUser.PkUserStatus;
import com.mrmq.poker.glossary.UserError;
import com.mrmq.poker.manager.PokerMananger;
import com.mrmq.poker.service.Session;
import com.mrmq.poker.utils.Converter;
import com.mrmq.util.CryptoHelper;

public class UpdateUserHandler extends AbstractHandler<UpdateUserRequest, UpdateUserResponse> {
	private static Logger log = LoggerFactory.getLogger(UpdateUserHandler.class);
	
	public UpdateUserHandler(RpcMessage request, Session session) {
		super(request, session);
	}

	@Override
	public void handle() throws Exception {
		UpdateUserRequest request = getRequest();
		
		log.info("[start] handle UpdateUserRequest from LoginId: {}, requestId: {}, command: {}", request.getUser().getLoginId(), rpcRequest.getId(), request.getCommand());
		
		if(UserRequestCommand.REGISTER == request.getCommand()) {
			registerUser(request);
		} else if(UserRequestCommand.UPDATE == request.getCommand()) {
			updateUser(request);
		} else if(UserRequestCommand.DELETE == request.getCommand()) {
			deleteUser(request);
		}
		
		response(rpcResponse.build());
		log.info("[end] handle UpdateUserRequest, requestId: {}", rpcRequest.getId());
	}

	private void registerUser(UpdateUserRequest request) throws Exception {
		log.info("Validate user: {}", request.getUser());
		
		UserError validateError = validateUser(request.getUser());
		
		if(UserError.SUCCESS == validateError) {
			PkUser user = PokerMananger.getUser(request.getUser().getLoginId());
			
			if(user != null) {
				
				log.info("User with loginId {} exist. Please choose other loginId", request.getUser().getLoginId());
				
				rpcResponse.setResult(Result.FAIL);
				rpcResponse.setMsgCode(UserError.ACCOUNT_EXIST.getCode());
				rpcResponse.setMessage(PokerMananger.getMsg(rpcResponse.getMsgCode()));
				
			} else {
				//Create new user
				user = new PkUser();
				
				user.setLogin(request.getUser().getLoginId());
				user.setPass(CryptoHelper.getMd5(request.getUser().getPassNew()));
				user.setUserName(request.getUser().getName());
				user.setAvataUrl(request.getUser().getAvataUrl());
				user.setUserGroup(PkUserGroup.DEMO.getValue());
				user.setBalance(new BigDecimal("0"));
				user.setPrevBalance(new BigDecimal("0"));
				user.setCredit(new BigDecimal("0"));
				user.setTaxes(new BigDecimal("0"));
				user.setCurrency("VND");
				user.setStatus(PkUserStatus.ACTIVE.getNumber());
				user.setRegDate(new Date(System.currentTimeMillis()));
				user.setUpdateDate(user.getRegDate());
				
				PokerMananger.putUser(user);
				PokerMananger.getPokerBusiness().getUserManager().insert(user);
				log.info("Registed user: {}", user);
				
				//Response
				UpdateUserResponse.Builder resBuilder = UpdateUserResponse.newBuilder();
				resBuilder.setUser(Converter.convertUser(user));
				
				rpcResponse.setPayloadClass(UpdateUserResponse.getDescriptor().getName());
				rpcResponse.setPayloadData(resBuilder.build().toByteString());
				rpcResponse.setResult(Result.SUCCESS);
			}
		} else {
			rpcResponse.setResult(Result.FAIL);
			rpcResponse.setMsgCode(validateError.getCode());
			rpcResponse.setMessage(PokerMananger.getMsg(rpcResponse.getMsgCode()));
		}
	}
	
	private void updateUser(UpdateUserRequest request) throws Exception {
		log.info("updateUser {}", request.getUser());
		
		if(!session.isAuthenticated()) {
			rpcResponse.setResult(Result.NOT_AUTHENTICATED);
			return ;
		}
		
		PkUser user = PokerMananger.getUser(session.getUser().getLogin());
		
		if(user == null) {
			rpcResponse.setResult(Result.FAIL);
			rpcResponse.setMsgCode(UserError.ACCOUNT_NOT_EXIST.getCode());
			rpcResponse.setMessage(PokerMananger.getMsg(rpcResponse.getMsgCode()));
		} else {
			if(request.getUser().hasPassNew() && !user.getPass().equals(CryptoHelper.getMd5(request.getUser().getPass()))) {
				//Check pass
				rpcResponse.setResult(Result.FAIL);
				rpcResponse.setMsgCode(UserError.PASS_NOT_MATCH.getCode());
				rpcResponse.setMessage(PokerMananger.getMsg(rpcResponse.getMsgCode()));
			} else {
				//Set new values
				user.setPass(CryptoHelper.getMd5(request.getUser().getPassNew()));
				if(request.getUser().hasName())
					user.setUserName(request.getUser().getName());
				if(request.getUser().hasAvataUrl())
					user.setAvataUrl(request.getUser().getAvataUrl());
				
				//Update to DB
				if(request.getUser().hasName() || request.getUser().hasAvataUrl() || request.getUser().hasPassNew()) {
					PokerMananger.getPokerBusiness().updateUser(user);
				}
				
				//Response
				UpdateUserResponse.Builder resBuilder = UpdateUserResponse.newBuilder();
				resBuilder.setUser(Converter.convertUser(user));
				
				rpcResponse.setPayloadClass(UpdateUserResponse.getDescriptor().getName());
				rpcResponse.setPayloadData(resBuilder.build().toByteString());
				rpcResponse.setResult(Result.SUCCESS);
			}
		}
	}
	
	private void deleteUser(UpdateUserRequest request) throws Exception {
		log.info("deleteUser {}", request.getUser());
		
		if(!session.isAuthenticated()) {
			rpcResponse.setResult(Result.NOT_AUTHENTICATED);
			return ;
		}
		
		PkUser user = PokerMananger.getUser(request.getUser().getLoginId());
		
		if(user == null) {
			rpcResponse.setResult(Result.FAIL);
			rpcResponse.setMsgCode(UserError.ACCOUNT_NOT_EXIST.getCode());
			rpcResponse.setMessage(PokerMananger.getMsg(rpcResponse.getMsgCode()));
		} else {
			user.setStatus(PkUserStatus.DEACTIVATE.getNumber());
			
			//Update to DB
			PokerMananger.getPokerBusiness().updateUser(user);
			
			rpcResponse.setResult(Result.SUCCESS);
		}
	}
	
	private UserError validateUser(User user) {
		if(user.getLoginId().trim().length() < 6) {
			return UserError.ACCOUNT_INVALID;
		}
		
		if(user.getPassNew().trim().length() < 6) {
			return UserError.PASS_INVALID;
		}
		
		if(user.getName().trim().length() < 3)
			return UserError.NAME_INVALID;
		
		return UserError.SUCCESS;
	}
	
	@Override
	protected UpdateUserRequest getRequest() throws InvalidProtocolBufferException {
		return UpdateUserRequest.parseFrom(rpcRequest.getPayloadData());
	}	
}