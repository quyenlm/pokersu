package com.mrmq.poker.handler.impl;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mrmq.poker.common.bean.Card;
import com.mrmq.poker.common.bean.Player;
import com.mrmq.poker.common.glossary.MsgCode;
import com.mrmq.poker.common.glossary.ServiceType;
import com.mrmq.poker.common.glossary.UserGroupType;
import com.mrmq.poker.common.proto.PokerModelProto.RoundType;
import com.mrmq.poker.common.proto.PokerServiceProto.JoinTableRequest;
import com.mrmq.poker.common.proto.PokerServiceProto.JoinTableResponse;
import com.mrmq.poker.common.proto.PokerServiceProto.JoinTableType;
import com.mrmq.poker.common.proto.Rpc.RpcMessage;
import com.mrmq.poker.common.proto.Rpc.RpcMessage.Result;
import com.mrmq.poker.game.poker.PokerTable;
import com.mrmq.poker.manager.PokerMananger;
import com.mrmq.poker.service.Session;
import com.mrmq.poker.utils.Converter;

public class JoinTableHandler extends AbstractHandler<JoinTableRequest, JoinTableResponse> {
	private static Logger log = LoggerFactory.getLogger(JoinTableHandler.class);
	
	public JoinTableHandler(RpcMessage request, Session session) {
		super(request, session);
	}

	@Override
	public void handle() throws Exception {
		log.info("[start] handle JoinTableRequest, requestId: {}", rpcRequest.getId());
		
		JoinTableResponse.Builder resBuilder = JoinTableResponse.newBuilder();
		JoinTableRequest request = getRequest();
		
		final PokerTable table = PokerMananger.getTable(request.getTableId());
		
		if(table != null) {
			Player player = Converter.createPlayer(session.getUser(), session);
			player.setPosition(request.getSelectedPosition());
			
			synchronized (table) {
				//Join table
				MsgCode msgCode = table.addPlayer(player, request.getJoinType());
				
				if(MsgCode.SUCCESS == msgCode) {
					if(request.getJoinType() == JoinTableType.PLAY || request.getJoinType() == JoinTableType.VIEW) {
						
						//Center cards
						Iterator<Card> centerCards = table.getBoard().iterator();
						while(centerCards.hasNext()) {
							resBuilder.addCenterCards(Converter.convertCard(centerCards.next()));
						}
						
						//Add player are playing
						Iterator<Player> playersIter = table.getPlayers().iterator();
						while (playersIter.hasNext()) {
							Player playing = playersIter.next();
							resBuilder.addPlayers(Converter.convertPlayer(playing));
						}
						
						resBuilder.setCurbet(table.getTotalChips());
						if(table.getActor() != null)
							resBuilder.setCurPlayerId(table.getActor().getLoginId());
						if(table.getDealer() != null)
							resBuilder.setDealerId(table.getDealer().getLoginId());
						resBuilder.setRoundType(table.getCurRoundType());
						
						int remainTime = (int)(table.getTableRule().getTimePerturn() - (System.currentTimeMillis() - table.getStartWaitTime()));
						
						if(table.getCurRoundType() != RoundType.WAITTING)
							resBuilder.setRemainTime(remainTime > 0 ? remainTime : 0);
						
						if(UserGroupType.PLAYER.getValue().equals(session.getUser().getUserGroup()) 
								&& table.getPlayers().size() < table.getTableRule().getMinPlayer())
							PokerMananger.onEvent(table, ServiceType.ADMIN);
					}
				}
				
				rpcResponse.setMsgCode(msgCode.getCode());
				rpcResponse.setResult(Result.SUCCESS);
			}
		} else {
			log.warn("Not found tableId: {}", request.getTableId());
			
			rpcResponse.setResult(Result.SERVICE_UNAVAILABLE);
			rpcResponse.setMsgCode("");
		}
		
		//Response
		rpcResponse.setPayloadClass(JoinTableResponse.getDescriptor().getName());
		rpcResponse.setPayloadData(resBuilder.build().toByteString());
		response(rpcResponse.build());
		
		log.info("[end] handle JoinTableRequest, requestId: {}", rpcRequest.getId());
	}
	
	@Override
	protected JoinTableRequest getRequest() throws InvalidProtocolBufferException {
		return JoinTableRequest.parseFrom(rpcRequest.getPayloadData());
	}
}