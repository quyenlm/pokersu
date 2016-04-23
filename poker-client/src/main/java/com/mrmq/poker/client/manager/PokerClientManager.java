package com.mrmq.poker.client.manager;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.springframework.context.ApplicationContext;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.protobuf.ByteString;
import com.mrmq.poker.client.setting.Configs;
import com.mrmq.poker.common.glossary.ServiceType;
import com.mrmq.poker.common.proto.AdminModelProto.User;
import com.mrmq.poker.common.proto.AdminServiceProto.LoginRequest;
import com.mrmq.poker.common.proto.AdminServiceProto.UpdateUserRequest;
import com.mrmq.poker.common.proto.AdminServiceProto.UpdateUserRequest.UserRequestCommand;
import com.mrmq.poker.common.proto.PokerModelProto.ActionType;
import com.mrmq.poker.common.proto.PokerModelProto.Table;
import com.mrmq.poker.common.proto.PokerServiceProto.JoinTableRequest;
import com.mrmq.poker.common.proto.PokerServiceProto.JoinTableType;
import com.mrmq.poker.common.proto.PokerServiceProto.PlayerAction;
import com.mrmq.poker.common.proto.PokerServiceProto.PlayerActionRequest;
import com.mrmq.poker.common.proto.PokerServiceProto.RoomRequest;
import com.mrmq.poker.common.proto.PokerServiceProto.TableRequest;
import com.mrmq.poker.common.proto.Rpc.RpcMessage;

public class PokerClientManager {
	private static Properties msgs;
	private static Configs configs;
	
	private static ExecutorService handlerService;
	
	public static void init(ApplicationContext context) {
		configs = (Configs) context.getBean("configs");
		msgs = (Properties) context.getBean("msgs");
		
		///Handle request
		final ThreadFactory handleFactory = new ThreadFactoryBuilder()
				.setNameFormat("Handler-%d").setDaemon(true).build();
		handlerService = Executors.newFixedThreadPool(configs.getThreadPoolSize(), handleFactory);
	}
	
	public static RpcMessage createLoginRequest(String loginId, String pass) {
		LoginRequest.Builder loginRequest = LoginRequest.newBuilder();
		
		loginRequest.setLoginId(loginId);
		loginRequest.setPass(pass);
		
		return createPokerRpcRequest(LoginRequest.getDescriptor().getName(), loginRequest.build().toByteString());
	}
	
	public static RpcMessage createJoinTableRequest(Table table, int selectedPosition) {
		
		JoinTableRequest.Builder joinTableReq = JoinTableRequest.newBuilder();
		joinTableReq.setJoinType(JoinTableType.PLAY);
		joinTableReq.setTableId(table.getTableId());
		joinTableReq.setSelectedPosition(selectedPosition);
		
		return createPokerRpcRequest(JoinTableRequest.getDescriptor().getName(), joinTableReq.build().toByteString());
	}
	
	public static RpcMessage createTableRequest() {
		TableRequest.Builder tableRequest = TableRequest.newBuilder();
		tableRequest.setRoomId("00001");
		
		return createPokerRpcRequest(TableRequest.getDescriptor().getName(), tableRequest.build().toByteString());
	}
	
	public static RpcMessage createRoomRequest() {
		RoomRequest.Builder roomRequest = RoomRequest.newBuilder();
		roomRequest.setHallId("01");
		return createPokerRpcRequest(RoomRequest.getDescriptor().getName(), roomRequest.build().toByteString());
	}
	
	public static RpcMessage createActionRequest(Table table, String playerId, ActionType action, Long amount) {
		PlayerAction.Builder gameAction = PlayerAction.newBuilder();
		gameAction.setPlayerId(playerId);
		gameAction.setActionType(action);
		gameAction.setAmount(amount);
		
		PlayerActionRequest.Builder actionRequest = PlayerActionRequest.newBuilder();
		actionRequest.setTableId(table.getTableId());
		actionRequest.setAction(gameAction);
		
		return createPokerRpcRequest(PlayerActionRequest.getDescriptor().getName(), actionRequest.build().toByteString());
	}
	
	public static RpcMessage createUpdateUserRequest(String loginId, String avataUrl, UserRequestCommand command) {
		User.Builder user = User.newBuilder();
		
		user.setLoginId(loginId);		
		user.setAvataUrl(avataUrl);
		user.setName(loginId);
		if(command == UserRequestCommand.UPDATE || command == UserRequestCommand.DELETE)
			user.setPass("123456");
		
		user.setPassNew("123456");
		
		UpdateUserRequest.Builder request = UpdateUserRequest.newBuilder();
		request.setCommand(command);
		request.setUser(user);
		
		return createPokerRpcRequest(UpdateUserRequest.getDescriptor().getName(), request.build().toByteString());
	}
	
	public static RpcMessage createPokerRpcRequest(String clazz, ByteString data) {
		RpcMessage.Builder request = RpcMessage.newBuilder();
		request.setId(System.currentTimeMillis());
		request.setPayloadClass(clazz);
		request.setPayloadData(data);
		request.setService(ServiceType.POKER.getValue());
		request.setVersion(configs.getPokerProtoVersion());
		request.setSourceId(1L);
		
		return request.build();
	}
	
	public static RpcMessage createAdminRpcRequest(String clazz, ByteString data) {
		RpcMessage.Builder request = RpcMessage.newBuilder();
		request.setId(System.currentTimeMillis());
		request.setPayloadClass(clazz);
		request.setPayloadData(data);
		request.setService(ServiceType.ADMIN.getValue());
		request.setVersion(configs.getAdminProtoVersion());
		request.setSourceId(1L);
		
		return request.build();
	}

	public static ExecutorService getHandlerService() {
		return handlerService;
	}

	public static void setHandlerService(ExecutorService handlerService) {
		PokerClientManager.handlerService = handlerService;
	}

	public static Configs getConfigs() {
		return configs;
	}

	public static Properties getMsgs() {
		return msgs;
	}
	
	public static String getMsgs(String key) {
		if(msgs.containsKey(key))
			return msgs.getProperty(key);
		return key;
	}
}