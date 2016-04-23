package com.mrmq.poker.utils;

import java.util.Iterator;

import com.google.protobuf.ByteString;
import com.mrmq.poker.common.glossary.UserGroupType;
import com.mrmq.poker.common.proto.AdminModelProto.User;
import com.mrmq.poker.common.proto.PokerModelProto.Card;
import com.mrmq.poker.common.proto.PokerModelProto.Card.CardRank;
import com.mrmq.poker.common.proto.PokerModelProto.Card.Suits;
import com.mrmq.poker.common.proto.PokerModelProto.Chip;
import com.mrmq.poker.common.proto.PokerModelProto.Hand;
import com.mrmq.poker.common.proto.PokerModelProto.HandValue;
import com.mrmq.poker.common.proto.PokerModelProto.Player;
import com.mrmq.poker.common.proto.PokerModelProto.PlayerState;
import com.mrmq.poker.common.proto.PokerModelProto.Table;
import com.mrmq.poker.common.proto.PokerModelProto.TableRule;
import com.mrmq.poker.common.proto.PokerServiceProto.PlayerActionEvent;
import com.mrmq.poker.common.proto.PokerServiceProto.PlayerActionResponse;
import com.mrmq.poker.common.proto.Rpc.RpcMessage;
import com.mrmq.poker.common.proto.Rpc.RpcMessage.Result;
import com.mrmq.poker.db.entity.PkGame;
import com.mrmq.poker.db.entity.PkUser;
import com.mrmq.poker.game.poker.PokerTable;
import com.mrmq.poker.service.Session;

public class Converter {
	public static Table convertTable(PokerTable pokerTable) {
		Table.Builder builder = Table.newBuilder();
		
		builder.setRoomId("001");
		builder.setTableId(pokerTable.getTableId());
		builder.setRoundType(pokerTable.getCurRoundType());
		builder.setTableRule(convertTableRule(pokerTable.getTableRule()));
		
		//Add player
		Iterator<com.mrmq.poker.common.bean.Player> it = pokerTable.getPlayers().iterator();
		while(it.hasNext()) {
			builder.addPlayers(convertPlayer(it.next()));
		}
		
		//Add common card
		Iterator<com.mrmq.poker.common.bean.Card> cardIt = pokerTable.getBoard().iterator();
		while(cardIt.hasNext()) {
			builder.addBoards(convertCard(cardIt.next()));
		}
		
		return builder.build();
	}
	
	public static TableRule convertTableRule(PkGame pkGame) {
		TableRule.Builder ruleBuilder = TableRule.newBuilder();
		ruleBuilder.setMaxPlayer(pkGame.getMaxPlayer());
		ruleBuilder.setMaxViewer(pkGame.getMaxViewer());
		ruleBuilder.setMinPlayer(pkGame.getMinPlayer());
		
		ruleBuilder.setBigBlind(pkGame.getBigBlind().longValue());
		ruleBuilder.setSmallBlind(pkGame.getSmallBlind().longValue());
		ruleBuilder.setMaxRaise(pkGame.getMaxBet().longValue());
		ruleBuilder.setMinRaise(pkGame.getMinBet().longValue());
		
		ruleBuilder.setTimePerTurn(pkGame.getTimePerturn());
		
		return ruleBuilder.build();
	}
	
	public static com.mrmq.poker.common.bean.Player createPlayer(PkUser user, Session session) {
		com.mrmq.poker.common.bean.Player player = new com.mrmq.poker.common.bean.Player(user.getUserName(), user.getBalance().longValue(), session);
		player.setAvataUrl(user.getAvataUrl());
		player.setLoginId(user.getLogin());
		player.setPlayState(PlayerState.PLAYER_VIEWING);
		
		return player;
	}
	
	public static User convertUser(PkUser user) {
		User.Builder builder = User.newBuilder();

		builder.setLoginId(user.getLogin());
		builder.setCash(user.getBalance().longValue());
		if(user.getAvataUrl() != null)
			builder.setAvataUrl(user.getAvataUrl());
		
		if(user.getUserName() != null)
			builder.setName(user.getUserName());
		
		if(UserGroupType.DEMO.getValue().equals(user.getUserGroup()) && user.getPass() != null)
			builder.setPass(user.getPass());
		
		return builder.build();
	}
	public static Player convertPlayer(com.mrmq.poker.common.bean.Player player) {
		Player.Builder builder = Player.newBuilder();
		
		builder.setLoginId(player.getLoginId());
		builder.setName(player.getName());
		builder.setPosition(player.getPosition());
		
		if(player.getCash() > -1)
			builder.setBalance(player.getCash());
		
		if(player.getBet() > 0)
			builder.setRaise(player.getBet());
		
		if(player.getPlayState() != null)
			builder.setPlayerState(player.getPlayState());
		
		if(player.hasCards())
			builder.setHand(convertHand(player.getCards(), 5));
		
		if(player.getHandValue() != null)
			builder.setHandValue(convertHandValue(player.getHandValue()));
		
		if(player.getAction() != null)
			builder.setLastAction(player.getAction());
		
		if(player.getAvataUrl() != null)
			builder.setAvatarUrl(player.getAvataUrl());
		
		return builder.build();
	}
	
	public static Card convertCard(com.mrmq.poker.common.bean.Card card) {
		Card.Builder builder = Card.newBuilder();
		builder.setCardRank(CardRank.valueOf(card.getRank()));
		builder.setSuit(Suits.valueOf(card.getSuit()));
		return builder.build();
	}
	
	public static Chip convertChip(com.mrmq.poker.common.bean.Pot pot) {
		Chip.Builder builder = Chip.newBuilder();
		builder.setBet(pot.getBet());
		return builder.build();
	}
	
	public static Hand convertHand(com.mrmq.poker.common.bean.Card[] cards, int maxCard) {
		Hand.Builder builder = Hand.newBuilder();
		if(cards != null && cards.length > 0) {
			for(int i = 0; i < cards.length; i++)
				builder.addCards(convertCard(cards[i]));
		}
		builder.setMaxCard(maxCard);
		return builder.build();
	}
	
	public static HandValue convertHandValue(com.mrmq.poker.common.bean.HandValue handValue) {
		return HandValue.valueOf(handValue.getType().getValue());
	}
	
	public static PlayerActionResponse convertPlayerActionResponse(PlayerActionEvent event) {
		PlayerActionResponse.Builder builder = PlayerActionResponse.newBuilder();
    	builder.setRoundType(event.getRoundType());
    	
    	//TODO check amount
    	builder.setActionType(event.getActionType());
    	builder.setRemainAmount(event.getRaiseAmount());
    	builder.setRaiseAmount(event.getRaiseAmount());
    	builder.setTotalRaiseAmount(event.getTotalRaiseAmount());
    	
    	builder.setNextPlayerId(event.getNextPlayerId());
    	
    	//Add chips
		builder.addAllCenterChips(event.getCenterChipsList());
    			
		return builder.build();
	}
	
	
}
