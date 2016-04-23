package com.mrmq.poker.handler.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mrmq.poker.common.proto.PokerServiceProto.PlayerActionRequest;
import com.mrmq.poker.common.proto.PokerServiceProto.PlayerActionResponse;
import com.mrmq.poker.common.proto.Rpc.RpcMessage;
import com.mrmq.poker.common.proto.Rpc.RpcMessage.Result;
import com.mrmq.poker.game.poker.PokerTable;
import com.mrmq.poker.manager.PokerMananger;
import com.mrmq.poker.service.Session;

public class ActionRequestHandler extends AbstractHandler<PlayerActionRequest, PlayerActionResponse> {
	private static Logger log = LoggerFactory.getLogger(ActionRequestHandler.class);
	
	public ActionRequestHandler(RpcMessage request, Session session) {
		super(request, session);
	}

	@Override
	public void handle() throws Exception {
		log.info("[start] handle ActionRequest, requestId: {}", rpcRequest.getId());
		
		PlayerActionRequest request = getRequest();
		
		final PokerTable table = PokerMananger.getTable(request.getTableId());
		
		if(table != null) {
			table.onAction(request.getAction());
		} else {
			log.warn("Not found tableId: {}", request.getTableId());
			
			rpcResponse.setResult(Result.SERVICE_UNAVAILABLE);
			rpcResponse.setMsgCode("Table does not exist or has stop");
			response(rpcResponse.build());
		}
		
		log.info("[end] handle ActionRequest, requestId: {}", rpcRequest.getId());
	}
	
	@Override
	protected PlayerActionRequest getRequest() throws InvalidProtocolBufferException {
		return PlayerActionRequest.parseFrom(rpcRequest.getPayloadData());
	}
}