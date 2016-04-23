package com.mrmq.poker.client.impl;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mrmq.poker.client.AbstractClientHandler;
import com.mrmq.poker.client.manager.PokerClientManager;
import com.mrmq.poker.common.proto.AdminServiceProto.LoginResponse;
import com.mrmq.poker.common.proto.AdminServiceProto.UpdateUserResponse;
import com.mrmq.poker.common.proto.PokerModelProto.ActionType;
import com.mrmq.poker.common.proto.PokerModelProto.Table;
import com.mrmq.poker.common.proto.PokerServiceProto.EndGameEvent;
import com.mrmq.poker.common.proto.PokerServiceProto.JoinTableResponse;
import com.mrmq.poker.common.proto.PokerServiceProto.NewRoundEvent;
import com.mrmq.poker.common.proto.PokerServiceProto.PlayerActionEvent;
import com.mrmq.poker.common.proto.PokerServiceProto.StartGameEvent;
import com.mrmq.poker.common.proto.PokerServiceProto.TableResponse;
import com.mrmq.poker.common.proto.Rpc.RpcMessage;
import com.mrmq.poker.common.proto.Rpc.RpcMessage.Result;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;

public class PokerClientHandler extends AbstractClientHandler {
	private static Logger log = LoggerFactory.getLogger(PokerClientHandler.class);
	
	private int position;
	private long lastBet = 0;
    private Table table;
    
	public PokerClientHandler(WebSocketClientHandshaker handshaker) {
		super(handshaker);
	}
    
	protected void handleRpcMessage(ChannelHandlerContext ctx, RpcMessage msg) throws Exception {
		log.info("Received RpcMessage, id: {}, payload: {}", msg.getId(), msg.getPayloadClass());
		
		Thread.sleep(1000);
		
		if(LoginResponse.getDescriptor().getName().equals(msg.getPayloadClass())) {
			//Login
			if(msg.getResult() == Result.SUCCESS) {
				RpcMessage tableRequest = PokerClientManager.createTableRequest();
				request(ctx, tableRequest);
			}
		}
		
		if(TableResponse.getDescriptor().getName().equals(msg.getPayloadClass())) {
			TableResponse response = TableResponse.parseFrom(msg.getPayloadData());
			log.info("TableResponse: " + response);
			
			if(response.getTablesCount() > 0) {
				Iterator<Table> iter = response.getTablesList().iterator();
				Table tempTable = null;
				
				if(table == null) {
					table = iter.next();
				} else
				while(iter.hasNext()) {
					tempTable = iter.next();
					if(tempTable.getTableId().equals(table.getTableId())) {
						table = tempTable;
						break;
					}
				}
				
				if(tempTable == null || table == tempTable) {
					log.info("Join to table: {}", table);
					
					RpcMessage joinTableRequest = PokerClientManager.createJoinTableRequest(table, position);
					request(ctx, joinTableRequest);
				} else
					log.warn("Not found table to join, Table: {}", table);
			}
		}
		
		if(JoinTableResponse.getDescriptor().getName().equals(msg.getPayloadClass())) {
			JoinTableResponse response = JoinTableResponse.parseFrom(msg.getPayloadData());
			log.info("JoinTableResponse: {}", response);
		}
		
		if(StartGameEvent.getDescriptor().getName().equals(msg.getPayloadClass())) {
			StartGameEvent gameEvent = StartGameEvent.parseFrom(msg.getPayloadData());
			log.info("StartGameEvent: {}", gameEvent);
			lastBet = table.getTableRule().getBigBlind();
		}
		
		if(NewRoundEvent.getDescriptor().getName().equals(msg.getPayloadClass())) {
			NewRoundEvent gameEvent = NewRoundEvent.parseFrom(msg.getPayloadData());
			log.info("NewRoundEvent: {}", gameEvent);
			
			if(gameEvent.getFirstPlayerId().equals(loginId)) {
				Thread.sleep(5000);
				
				RpcMessage actionRequest = PokerClientManager.createActionRequest(table, loginId, ActionType.RAISE, lastBet);
				request(ctx, actionRequest);
			}
		}
		
		if(EndGameEvent.getDescriptor().getName().equals(msg.getPayloadClass())) {
			EndGameEvent gameEvent = EndGameEvent.parseFrom(msg.getPayloadData());
			log.info("EndGameEvent: {}", gameEvent);
		}
		
		if(PlayerActionEvent.getDescriptor().getName().equals(msg.getPayloadClass())) {
			PlayerActionEvent gameEvent = PlayerActionEvent.parseFrom(msg.getPayloadData());
			log.info("PlayerActionEvent: {}", gameEvent);
			lastBet = gameEvent.getRaiseAmount();
			
			if(gameEvent.getNextPlayerId().equals(loginId)) {
				Thread.sleep(5000);
				
				RpcMessage actionRequest = PokerClientManager.createActionRequest(table, loginId, ActionType.CALL, lastBet);
				request(ctx, actionRequest);
			}
		}
		
		if(UpdateUserResponse.getDescriptor().getName().equals(msg.getPayloadClass())) {
			UpdateUserResponse response = UpdateUserResponse.parseFrom(msg.getPayloadData());
			log.info("UpdateUserResponse: {}", response);
		}
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public Table getTable() {
		return table;
	}

	public void setTable(Table table) {
		this.table = table;
	}
}