package com.mrmq.poker.handler.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mrmq.poker.common.proto.PokerServiceProto.RoomRequest;
import com.mrmq.poker.common.proto.PokerServiceProto.RoomResponse;
import com.mrmq.poker.common.proto.Rpc.RpcMessage;
import com.mrmq.poker.service.Session;

public class GetRoomHandler extends AbstractHandler<RoomRequest, RoomResponse> {
	private static Logger log = LoggerFactory.getLogger(AbstractHandler.class);
	
	public GetRoomHandler(RpcMessage request, Session session) {
		super(request, session);
	}

	@Override
	public void handle() {
		log.info("[start] handle, requestId: {}", rpcRequest.getId());
		
		log.info("[end] handle, requestId: {}", rpcRequest.getId());
	}

	@Override
	protected RoomRequest getRequest() throws InvalidProtocolBufferException {
		return RoomRequest.parseFrom(rpcRequest.getPayloadData());
	}

}
