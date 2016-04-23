package com.mrmq.poker.admin;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mrmq.poker.client.AbstractClientHandler;
import com.mrmq.poker.client.impl.PokerClient;
import com.mrmq.poker.client.manager.PokerBotManager;
import com.mrmq.poker.client.manager.PokerClientManager;
import com.mrmq.poker.client.utils.Helper;
import com.mrmq.poker.common.proto.AdminModelProto.User;
import com.mrmq.poker.common.proto.AdminServiceProto.LoginResponse;
import com.mrmq.poker.common.proto.ManagerModelProto.UserType;
import com.mrmq.poker.common.proto.ManagerServiceProto.JoinTableRequestEvent;
import com.mrmq.poker.common.proto.ManagerServiceProto.UsersRequest;
import com.mrmq.poker.common.proto.ManagerServiceProto.UsersResponse;
import com.mrmq.poker.common.proto.PokerModelProto.Player;
import com.mrmq.poker.common.proto.PokerModelProto.Table;
import com.mrmq.poker.common.proto.Rpc.RpcMessage;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;

public class AdminClientHandler extends AbstractClientHandler {
	private static Logger log = LoggerFactory.getLogger(AdminClientHandler.class);
	
	public AdminClientHandler(WebSocketClientHandshaker handshaker) {
		super(handshaker);
	}

	protected void handleRpcMessage(ChannelHandlerContext ctx, RpcMessage msg) throws Exception {
		log.info("Received RpcMessage, id: {}, payload: {}", msg.getId(), msg.getPayloadClass());
		
		if(LoginResponse.getDescriptor().getName().equals(msg.getPayloadClass())) {
			UsersRequest.Builder request = UsersRequest.newBuilder();
			request.setUserType(UserType.BOT);
			RpcMessage usersRequest = PokerClientManager.createAdminRpcRequest(UsersRequest.getDescriptor().getName(), request.build().toByteString());
			request(ctx, usersRequest);
		} else if(UsersResponse.getDescriptor().getName().equals(msg.getPayloadClass())) {
			//UsersResponse
			try {
				UsersResponse response = UsersResponse.parseFrom(msg.getPayloadData());
				log.info("UsersResponseSize: {}", response.getUsersCount());
				
				if(response.getUsersCount() > 0) {
					Iterator<User> iter = response.getUsersList().iterator();
					User user = null;
					while(iter.hasNext()) {
						user = iter.next();
						log.info("" + user);
						PokerBotManager.getAllUsers().put(user.getLoginId(), user);
					}
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			} finally {
				PokerBotManager.getCounter().countDown();
			}
		} else if(JoinTableRequestEvent.getDescriptor().getName().equals(msg.getPayloadClass())) {
			Table table = null;
			//JoinTableRequestEvent
			try {
				JoinTableRequestEvent event = JoinTableRequestEvent.parseFrom(msg.getPayloadData());
				log.info("JoinTableRequestEvent: {}", event);
				
				if(!event.hasTable()) {
					log.warn("Not found any table in {}", msg.getId());
					return;
				}
				
				table = event.getTable();
				int count = table.getTableRule().getMinPlayer() - table.getPlayersCount();
				
				if(count > 0 && !PokerBotManager.getCurTables().containsKey(table.getTableId())) {
					PokerBotManager.getCurTables().put(table.getTableId(), table);
							
					Map<Integer, Boolean> mapPosition = new HashMap<Integer, Boolean>();
					for (Player player : table.getPlayersList()) {
						mapPosition.put(player.getPosition(), Boolean.TRUE);
					}
					
					log.warn("Table {} need {} add players to start", table.getTableId(), count);
					
					//Find free player to join game
					List<User> freeUsers = PokerBotManager.findFreeUser(count);
					if(freeUsers.size() > 0) {
						for (User user : freeUsers) {
							
							//find suit position for player
							Integer position = Helper.findNextPosition(mapPosition, table.getTableRule().getMaxPlayer());
							if(position != null) {
								mapPosition.put(position, Boolean.TRUE);
								
								PokerClient pokerClient = new PokerClient(PokerClientManager.getConfigs().getPokerUrl(), 
										user.getLoginId(), user.getPass(), position);
								pokerClient.setTable(table);
								
								PokerClientManager.getHandlerService().submit(pokerClient);
							} else {
								log.warn("Not found suit position for {}", user);
							}
						}
					}
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			} finally {
				if(table != null)
					PokerBotManager.getCurTables().remove(table.getTableId());
			}
		}
	}
}
