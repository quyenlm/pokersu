package com.mrmq.poker.handler.impl;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mrmq.poker.common.proto.PokerServiceProto.TableRequest;
import com.mrmq.poker.common.proto.PokerServiceProto.TableResponse;
import com.mrmq.poker.common.proto.Rpc.RpcMessage;
import com.mrmq.poker.db.entity.PkGame;
import com.mrmq.poker.db.entity.PkGame.PkGameType;
import com.mrmq.poker.game.poker.PokerTable;
import com.mrmq.poker.manager.PokerMananger;
import com.mrmq.poker.service.Session;
import com.mrmq.poker.utils.Converter;

public class GetTableHandler extends AbstractHandler<TableRequest, TableResponse> {
	private static Logger log = LoggerFactory.getLogger(GetTableHandler.class);
	
	public GetTableHandler(RpcMessage request, Session session) {
		super(request, session);
	}

	@Override
	public void handle() throws Exception {
		log.info("[start] handle TableRequest, requestId: {}", rpcRequest.getId());
		
		TableRequest request = getRequest();
		
		TableResponse.Builder resBuilder = TableResponse.newBuilder();
		resBuilder.setRoomId(request.getRoomId());
		
		final Map<String, PokerTable> tables = PokerMananger.getTables();
		boolean hasAvailableTable = false;
		
		for (PokerTable pokerTable : tables.values()) {
			if(pokerTable.getViewerCount() < pokerTable.getTableRule().getMaxViewer())
				hasAvailableTable = true;
			resBuilder.addTables(Converter.convertTable(pokerTable));
		}
		
		if(hasAvailableTable == false) {
			//Create new table
			log.info("There are no available Table, create new once");
			
			ConcurrentHashMap<String, PkGame> rules = PokerMananger.getGameRules(PkGameType.POKER);
			if(rules != null) {
				Iterator<PkGame> it = rules.values().iterator();
				
				while(it.hasNext()) {
					
					PokerTable pokerTable = new PokerTable(it.next());
					pokerTable.setTableId(PokerMananger.genTableId());
					
					PokerMananger.putTable(pokerTable);
					resBuilder.addTables(Converter.convertTable(pokerTable));
					
					log.info("Created new available Table, table: " + pokerTable);
				}
			} else
				log.warn("Not found any {} game rules. Please check NOW!", PkGameType.POKER);
			
//			//TODO add bot for test
//			new PokerBotManager(session.getUser().getLogin()).start();
		}
		
		//Response
		rpcResponse.setPayloadClass(TableResponse.getDescriptor().getName());
		rpcResponse.setPayloadData(resBuilder.build().toByteString());
		response(rpcResponse.build());
		
		log.info("[end] handle TableRequest, requestId: {}", rpcRequest.getId());
	}
	
	@Override
	protected TableRequest getRequest() throws InvalidProtocolBufferException {
		return TableRequest.parseFrom(rpcRequest.getPayloadData());
	}
}