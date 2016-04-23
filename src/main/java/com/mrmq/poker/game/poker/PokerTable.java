package com.mrmq.poker.game.poker;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.ByteString;
import com.mrmq.concurrent.Lock;
import com.mrmq.poker.common.bean.Card;
import com.mrmq.poker.common.bean.Deck;
import com.mrmq.poker.common.bean.Hand;
import com.mrmq.poker.common.bean.HandValue;
import com.mrmq.poker.common.bean.Player;
import com.mrmq.poker.common.bean.Pot;
import com.mrmq.poker.common.glossary.MsgCode;
import com.mrmq.poker.common.proto.PokerModelProto.ActionType;
import com.mrmq.poker.common.proto.PokerModelProto.PlayerState;
import com.mrmq.poker.common.proto.PokerModelProto.RoundType;
import com.mrmq.poker.common.proto.PokerServiceProto.EndGameEvent;
import com.mrmq.poker.common.proto.PokerServiceProto.JoinTableEvent;
import com.mrmq.poker.common.proto.PokerServiceProto.JoinTableType;
import com.mrmq.poker.common.proto.PokerServiceProto.NewRoundEvent;
import com.mrmq.poker.common.proto.PokerServiceProto.PlayerAction;
import com.mrmq.poker.common.proto.PokerServiceProto.PlayerActionEvent;
import com.mrmq.poker.common.proto.PokerServiceProto.PlayerActionResponse;
import com.mrmq.poker.common.proto.PokerServiceProto.StartGameEvent;
import com.mrmq.poker.common.proto.Rpc.RpcMessage;
import com.mrmq.poker.db.entity.PkCashflow.PkCashflowSourceType;
import com.mrmq.poker.db.entity.PkCashflow.PkCashflowStatus;
import com.mrmq.poker.db.entity.PkCashflow.PkCashflowType;
import com.mrmq.poker.db.entity.PkGame;
import com.mrmq.poker.db.entity.PkGameHistory;
import com.mrmq.poker.db.entity.PkUser;
import com.mrmq.poker.game.GameTable;
import com.mrmq.poker.manager.PokerMananger;
import com.mrmq.poker.servlet.PokerServerHandler;
import com.mrmq.poker.utils.Converter;

/**
 * Limit Texas Hold'em poker table.
 * This class forms the heart of the poker engine. It controls the game flow for a single poker table.
 */
public class PokerTable implements GameTable, Runnable {
	private static Logger log = LoggerFactory.getLogger(PokerServerHandler.class);
	
    private String tableId;
    private Integer gameHistoryId = 0;
    private PkGame tableRule;
    
    private AtomicBoolean isAlive = new AtomicBoolean(true);
    private boolean isPlaying = false;
    
    /** In fixed-limit games, the maximum number of raises per betting round. */
    private static final int MAX_RAISES = 3;
    
    /** Whether players will always call the showdown, or fold when no chance. */
    private static final boolean ALWAYS_CALL_SHOWDOWN = false;
    
    private final List<Player> viewers;
    private final List<Player> players;
    
    private final Deck deck;
    
    /** The community cards on the board. */
    private final List<Card> board;
    
    private int dealerPosition = -1;
    private Player dealer;

    private int actorPosition = -1;
    private Player actor;

    /** The minimum bet in the current hand. */
    private long minBet;
    
    /** The current bet in the current hand. */
    private long bet;
    
    /** The player who bet or raised last (aggressor). */
    private Player lastBettor;
    
    /** All pots in the current hand (main pot and any side pots). */
    private final List<Pot> pots;
    
    /** Number of raises in the current betting round. */
    private long raiseCounter;
    private int playerWhenStart = 0;
    
    private long startWaitTime = System.currentTimeMillis();
    private RoundType curRoundType = RoundType.WAITTING;
    
    //Temp variable
    private List<Player> actors = new ArrayList<Player>();
    private List<Card> addCards = new ArrayList<Card>();
    private List<Pot> addChips = new ArrayList<Pot>();
    
    public PokerTable(PkGame pkGame) {
        viewers = new ArrayList<Player>();
        players = new ArrayList<Player>();
        deck = new Deck();
        board = new ArrayList<Card>();
        pots = new ArrayList<Pot>();
        
		this.tableRule = pkGame;
    }
    
    int timeToPreloop = 0;
    
    public void run() {
    	while (isAlive.get()) {
    		try {
	        	//waiting for enough player
    			curRoundType = RoundType.WAITTING;
	        	waitForPlayer();
	        	
	        	log.info("Game will start at {} seconds...", tableRule.getTimePerturn()/1000);
	        	
//	        	Lock.wait((long)tableRule.getTimePerturn(), TimeUnit.MILLISECONDS);
	        	Lock.wait(1000L, TimeUnit.MILLISECONDS);
	        	
	        	isPlaying = true;
	        	PkGameHistory pkGameHistory  = PokerMananger.getPokerBusiness().createPkGameHistory(tableRule, players);
	        	if(pkGameHistory != null) {
	        		gameHistoryId = pkGameHistory.getGameHistoryId();
	        		log.info("Start new game, gameHistoryId: {}", gameHistoryId);
	        		pkGameHistory = null;
	        	} else
	        		throw new Exception("Cannot create Game. Please contact admin for supporting");
	        	
	        	//Reset table
	            resetTable();
	            
	            // Small blind.           
	            rotateActor();
	            postSmallBlind(actor);
	            actors.add(actor);
	            
	            // Big blind.
	            rotateActor();
	            postBigBlind(actor);
	            actors.add(actor);
	            
	            startWaitTime = System.currentTimeMillis();
	            
	            //Notify Game start
	            rotateActor();
	            notifyGameStared(RoundType.BIG_BLIND, actors, dealer.getLoginId(), actor.getLoginId());
	            actors.clear();
	            
	            Lock.wait(1000L, TimeUnit.MILLISECONDS);
	            
	            //dealHoleCards
	            dealHoleCards();
	            addCards.addAll(dealCommunityCards(RoundType.BIG_BLIND, 3));
	            notifyNewRoundStarted(RoundType.BIG_BLIND_END, addCards, addChips, dealer.getLoginId(), actor.getLoginId());
	            addCards.clear();
	            addChips.clear();
	            
	            //Wait for deal card
	            Lock.wait(getPlayerToAct() * 1000L + 1000L, TimeUnit.MILLISECONDS);
	            
	            // Pre-Flop
	            doBettingRound(RoundType.PRE_FLOP);
	            
	            // Flop.
	            if (getPlayerToAct() > 1) {
	                bet = 0;
	                
	                doBettingRound(RoundType.FLOP);
	
	                // Turn.
	                if (getPlayerToAct() > 1) {
	                    bet = 0;
	                    dealCommunityCards(RoundType.TURN, 1);
	                    minBet = 2 * tableRule.getBigBlind().longValue();
	                    doBettingRound(RoundType.TURN);
	
	                    // River.
	                    if (getPlayerToAct() > 1) {
	                        bet = 0;
	                        dealCommunityCards(RoundType.RIVER, 1);
	                        doBettingRound(RoundType.RIVER);
	
	                        // Showdown.
	                        if (getPlayerToAct() > 1) {
	                            bet = 0;
	                            doShowdownRound();
	                        } else {
	                        	log.warn("Round: {}, PlayerToAct = 0", RoundType.SHOW_DOWN);
	                        	notifyGameEnd(RoundType.RIVER);
	                        }
	                    } else {
	                    	log.warn("Round: {}, PlayerToAct = 0", RoundType.RIVER);
	                    	notifyGameEnd(RoundType.TURN);
	                    }
	                } else {
	                	log.warn("Round: {}, PlayerToAct = 0", RoundType.TURN);
	                	notifyGameEnd(RoundType.FLOP);
	                }
	            } else {
	            	log.warn("Round: {}, PlayerToAct = 0", RoundType.FLOP);
	            	notifyGameEnd(RoundType.PRE_FLOP);
	            }
	        
		        // Game over.
		        board.clear();
		        pots.clear();
		        bet = 0;
		        isPlaying = false;
	    	} catch (Exception e) {
	    		log.error(e.getMessage(), e);
	    	}
    	}
    	
        for (Player player : players) {
            player.resetHand();
        }
        
        isAlive.set(false);
        PokerMananger.removeTable(tableId);
        log.info("TableId {} over", tableId);
    }
    
    /**
     * Resets the game for a new hand.
     */
    private void resetTable() {
    	log.info("[start] reset table");
    	
        // Clear the board.
        board.clear();
        pots.clear();
        
        // Rotate the dealer button.
        playerWhenStart = getPlayerToAct();
        dealerPosition = (dealerPosition + 1) % playerWhenStart;
        dealer = players.get(dealerPosition);

        // Shuffle the deck.
        deck.shuffle();

        // Determine the first player to act.
        actorPosition = dealerPosition;
        actor = players.get(actorPosition);
        
        // Set the initial bet to the big blind.
        minBet = tableRule.getBigBlind().longValue();
        bet = minBet;
        
        log.info("[end] reset table, dealer: {}, firstPlayer: {}", dealer.getLoginId(), actor.getLoginId());
    }

    private void waitForPlayer() {
    	boolean isFirst = true;
    	
    	while(isAlive.get()) {
    		int playerCount = getPlayerToPlay();
    		
    		if(playerCount >= getTableRule().getMinPlayer()) {
    			log.info("Enough players ({}) to start new game", playerCount);
    			
    			synchronized (players) {
    				Iterator<Player> it = players.iterator();
        			while(it.hasNext()) {
        				Player player = it.next();
        				player.setPlayState(PlayerState.PLAYER_PLAYING);
        			}
				}
    			break;
    		} else if(isFirst) {
    			log.info("Notify to wait enough {} player for new game", getTableRule().getMinPlayer());
    			isFirst = false;
    			
    			Iterator<Player> it1 = players.iterator();
    			while(it1.hasNext()) {
    				Player player = it1.next();
    				
    				//TODO notify waiting for player
//    				player.getClient().send();
    			}
    		}
    		
    		Lock.wait(100L, TimeUnit.MILLISECONDS);
    	}
    }
    
    /**
     * Rotates the position of the player in turn (the actor).
     */
    private int rotateActor() {
    	Player tempPlayer = actor;
    	int tempPosition = actorPosition;
    	int totalPlayer = getPlayerToAct();
    	
    	int count = 0;
    	while(count++ < totalPlayer) {
    		tempPosition = (tempPosition + 1) % totalPlayer;
    		tempPlayer = players.get(tempPosition);
    		
    		if(tempPlayer.getPlayState() == PlayerState.PLAYER_PLAYING)
    			break;
    	}
    	
    	actor = tempPlayer;
    	actorPosition = tempPosition;
    	
        return actorPosition;
    }
    
    /**
     * Posts the small blind.
     */
    private long postSmallBlind(Player actor) {
        final long smallBlind = tableRule.getBigBlind().longValue() / 2;
        
        actor.postSmallBlind(smallBlind);
        
        contributeChip(actor, smallBlind);
        
        return smallBlind;
    }
    
    /**
     * Posts the big blind.
     */
    private long postBigBlind(Player actor) {
        actor.postBigBlind(tableRule.getBigBlind().longValue());
        contributeChip(actor, tableRule.getBigBlind().longValue());
        
        return tableRule.getBigBlind().longValue();
    }
    
    /**
     * Deals the Hole Cards.
     */
    private void dealHoleCards() {
        for (Player player : players) {
            player.setCards(deck.deal(2));
        }
        log.info("Dealer {} deals the hole cards", dealer);
    }
    
    /**
     * Deals a number of community cards.
     * 
     * @param phaseName The name of the phase.
     * @param noOfCards The number of cards to deal.
     */
    private List<Card> dealCommunityCards(RoundType roundType, int noOfCards) {
    	 List<Card> dealedCards = new ArrayList<Card>();
    	 
        for (int i = 0; i < noOfCards; i++) {
        	Card card = deck.deal();
            board.add(card);
            dealedCards.add(card);
        }
        
        log.info("Dealer {} deals the {} Community Cards", dealer, roundType);
        
        return dealedCards;
    }
    
    /**
     * Performs a betting round.
     * @throws Exception 
     */
    private void doBettingRound(RoundType roundType) throws Exception {
    	log.info("[start] round {}", roundType);
    	curRoundType = roundType;
    	
        // Determine the number of active players.
        int playersToAct = getPlayerToAct();
        
        // Determine the initial player and bet size.
        if (board.size() == 0) {
            // Pre-Flop; player left of big blind starts, bet is the big blind.
            bet = tableRule.getBigBlind().longValue();
        } else {
            // Otherwise, player left of dealer starts, no initial bet.
            bet = 0;
        }
        
        lastBettor = null;
        raiseCounter = 0;
        
        while (playersToAct > 0) {
            //rotateActor();
            PlayerAction action = null;
            
            if (actor.isAllIn()) {
            	log.info("{} is Allin", actor.getLoginId());
                // Player is all-in, so must check.
                action = PlayerAction.newBuilder()
    					.setPlayerId(actor.getLoginId())
    					.setAmount(0)
    					.setActionType(ActionType.CHECK)
    					.build();
                playersToAct--;
                rotateActor(); //Switch to next player
            } else {
                // Otherwise allow client to act.
                Set<ActionType> allowedActions = getAllowedActions(actor);
                
                //waiting for action of actor
                action = waitForPlayerAction(actor, minBet, bet, allowedActions);
                
                playersToAct--;
                if (action.getActionType() == ActionType.CHECK) {
                    // Do nothing.
                } else if (action.getActionType() == ActionType.CALL) {
                	
                    long betIncrement = action.getAmount() - actor.getBet();
                    if (betIncrement > actor.getCash()) {
                        betIncrement = actor.getCash();
                    }
                   
                    actor.payCash(betIncrement);
                    actor.setBet(actor.getBet() + betIncrement);
                    contributeChip(actor, betIncrement);
                    
                } else if (action.getActionType() == ActionType.BET) {
                	
                    long amount = action.getAmount();
                    if (amount < minBet && amount < actor.getCash()) {
                    	throw new IllegalStateException("Illegal client action: raise (" + amount +  ") less than minimum bet (" + minBet + ")!");
                    }
                    
                    actor.payCash(amount);
                    actor.setBet(amount);
                    
                    contributeChip(actor, amount);
                    bet = amount;
                    minBet = amount;
                    lastBettor = actor;
                    playersToAct = getPlayerToAct();
                    
                } else if (action.getActionType() == ActionType.RAISE) {
                	long amount = action.getAmount();
                    if (amount < minBet && amount < actor.getCash()) {
                        throw new IllegalStateException("Illegal client action: raise (" + amount +  ") less than minimum bet (" + minBet + ")!");
                    }
                    bet += amount;
                    minBet = amount;
                    long betIncrement = bet - actor.getBet();
                    if (betIncrement > actor.getCash()) {
                        betIncrement = actor.getCash();
                    }
                    
                    
                    actor.setBet(bet);
                    actor.payCash(betIncrement);
                    contributeChip(actor, betIncrement);
                    lastBettor = actor;
                    raiseCounter++;
                    
                    if (raiseCounter < MAX_RAISES) {
                        // All players get another turn.
                        playersToAct = getPlayerToAct();
                    } else {
                        // Max. number of raises reached; other players get one more turn.
                        playersToAct = getPlayerToAct() - 1;
                    }
                } else if (action.getActionType() == ActionType.FOLD) {
                    actor.setCards(null);
                    actor.setPlayState(PlayerState.PLAYER_FOLDED);
                    
                    if (getPlayerToAct() == 1) {
                    	
                        // Only one player left, so he wins the entire pot.
                        Player winner = getLastPlayerToWin();
                        Long amount = getTotalChips();
                        winner.win(amount);
                        
                        String winerText = String.format("%s wins %s $u", winner, amount);
                        log.info(winerText);
                        
                        calculateBetMoney(amount);
                        
                        //Update game status
                        PokerMananger.getPokerBusiness().updatePkGameHistory(gameHistoryId, new BigDecimal(amount), winerText);
                        
                        playersToAct = 0;
                    }
                } else {
                    // Programming error, should never happen.
                    throw new IllegalStateException("Invalid action: " + action);
                }
                
                Player curActor = actor;
                
                //Switch to next player
                rotateActor();
                
                //Notify player action to other player
                notifyPlayerActed(roundType, curActor, action, actor.getLoginId());
            }
        }
        
        // Reset player's bets.
        for (Player player : players) {
            player.resetBet();
        }
        
        log.info("[end] round {}", roundType);
    }
    
    /**
     * Returns the allowed actions of a specific player.
     * 
     * @param player The player.
     * 
     * @return The allowed actions.
     */
    private Set<ActionType> getAllowedActions(Player player) {
        Set<ActionType> actions = new HashSet<ActionType>();
        
        if (player.isAllIn()) {
            actions.add(ActionType.CHECK);
        } else {
        	long actorBet = actor.getBet();
            if (bet == 0) {
                actions.add(ActionType.CHECK);
                if (raiseCounter < MAX_RAISES) {
                    actions.add(ActionType.BET);
                }
            } else {
                if (actorBet < bet) {
                    actions.add(ActionType.CALL);
                    if (raiseCounter < MAX_RAISES) {
                        actions.add(ActionType.RAISE);
                    }
                } else {
                    actions.add(ActionType.CHECK);
                    if (raiseCounter < MAX_RAISES) {
                        actions.add(ActionType.RAISE);
                    }
                }
            }
            actions.add(ActionType.FOLD);
        }
        
        return actions;
    }
    
    /**
     * Contributes to the chips
     * 
     * @param amount The amount to contribute.
     */
    private void contributeChip(Player actor, long amount) {
        for (Pot chip : pots) {
            if (!chip.hasContributer(actor)) {
            	long chipBet = chip.getBet();
            	
                if (amount >= chipBet) {
                    // Regular call, bet or raise.
                    chip.addContributer(actor);
                    amount -= chip.getBet();
                } else {
                    // Partial call (all-in); redistribute pots.
                    pots.add(chip.split(actor, amount));
                    amount = 0;
                }
            }
            if (amount <= 0) {
                break;
            }
        }
        
        if (amount > 0) {
            Pot chip = new Pot(amount);
            chip.addContributer(actor);
            pots.add(chip);
        }
    }
    
    /**
     * Calculate when all other player folded
     * */
    private void calculateBetMoney(Long winAmount) throws Exception {
    	Iterator<Player> it = players.iterator();
    	while(it.hasNext()) {
    		Player player = it.next();
    		
    		if(player.getPlayState() == PlayerState.PLAYER_FOLDED) {
    			//Reduce bet amount
    			if(player.getTotalBet() > 0)
                PokerMananger.getPokerBusiness().changeCashBalance(PokerMananger.getUser(player.getLoginId()), 
                		player.getTotalBet(), PkCashflowType.BET, PkCashflowSourceType.GAME, gameHistoryId, PkCashflowStatus.ACTIVE.getNumber());
    		} else if(player.getPlayState() == PlayerState.PLAYER_PLAYING) {
    			//Add win amount
    			Long changeAmount = winAmount > player.getTotalBet() ? winAmount - player.getTotalBet() : player.getTotalBet() - winAmount;
                PokerMananger.getPokerBusiness().changeCashBalance(PokerMananger.getUser(player.getLoginId()), 
                		changeAmount, PkCashflowType.WIN, PkCashflowSourceType.GAME, gameHistoryId, PkCashflowStatus.ACTIVE.getNumber());
    		}
    	}
    }
    
    /**
     * Calculate when all communities cards has open
     * */
    private void doShowdownRound() throws Exception {
    	log.info("Showdown info, pots:");
    	curRoundType = RoundType.SHOW_DOWN;
    	
        for (Pot pot : pots) {
            log.info("\t" + pot);
        }
        log.info("Total pot: %{}\n", getTotalChips());
        
        // Determine show order; start with all-in players...
        List<Player> showingPlayers = new ArrayList<Player>();
        for (Pot pot : pots) {
            for (Player contributor : pot.getContributors()) {
                if (!showingPlayers.contains(contributor) && contributor.isAllIn()) {
                    showingPlayers.add(contributor);
                }
            }
        }
        
        // ...then last player to bet or raise (aggressor)...
        if (lastBettor != null) {
            if (!showingPlayers.contains(lastBettor)) {
                showingPlayers.add(lastBettor);
            }
        }
        
        //...and finally the remaining players, starting left of the button.
        int pos = (dealerPosition + 1) % playerWhenStart;
        while (showingPlayers.size() < playerWhenStart) {
            Player player = players.get(pos);
            if (!showingPlayers.contains(player)) {
                showingPlayers.add(player);
            }
            pos = (pos + 1) % playerWhenStart;
        }
        
        // Players automatically show or fold in order.
        boolean firstToShow = true;
        int bestHandValue = -1;
        for (Player playerToShow : showingPlayers) {
        	
        	//Reduce cash of player
            PokerMananger.getPokerBusiness().changeCashBalance(PokerMananger.getUser(playerToShow.getLoginId()), 
            		playerToShow.getTotalBet(), PkCashflowType.BET, PkCashflowSourceType.GAME, gameHistoryId, PkCashflowStatus.ACTIVE.getNumber());
            
            Hand hand = new Hand(board);
            hand.addCards(playerToShow.getCards());
            HandValue handValue = new HandValue(hand);
            playerToShow.setHandValue(handValue);
            
            boolean doShow = ALWAYS_CALL_SHOWDOWN;
            
            if (!doShow) {
                if (playerToShow.isAllIn()) {
                    // All-in players must always show.
                    doShow = true;
                    firstToShow = false;
                } else if (firstToShow) {
                    // First player must always show.
                    doShow = true;
                    bestHandValue = handValue.getValue();
                    firstToShow = false;
                } else {
                    // Remaining players only show when having a chance to win.
                    if (handValue.getValue() >= bestHandValue) {
                        doShow = true;
                        bestHandValue = handValue.getValue();
                    }
                }
            }
            
            if (doShow) {
                log.info("{} has {}", playerToShow, handValue.getDescription());
            } else {
                // Fold.
                playerToShow.setCards(null);
                playerToShow.setPlayState(PlayerState.PLAYER_WAITTING);
                log.info("{} folds", playerToShow);
            }
        }
        
        // Sort players by hand value (highest to lowest).
        Map<HandValue, List<Player>> rankedPlayers = new TreeMap<HandValue, List<Player>>();
        for (Player player : players) {
            // Create a hand with the community cards and the player's hole cards.
            Hand hand = new Hand(board);
            hand.addCards(player.getCards());
            // Store the player together with other players with the same hand value.
            HandValue handValue = new HandValue(hand);
            player.setHandValue(handValue);
            log.info("{}'s handValue: {}", player, handValue);
            
            List<Player> playerList = rankedPlayers.get(handValue);
            if (playerList == null) {
                playerList = new ArrayList<Player>();
            }
            playerList.add(player);
            rankedPlayers.put(handValue, playerList);
        }

        // Per rank (single or multiple winners), calculate pot distribution.
        Long totalPot = getTotalChips();
        Map<Player, Long> potDivision = new HashMap<Player, Long>();
        for (HandValue handValue : rankedPlayers.keySet()) {
            List<Player> winners = rankedPlayers.get(handValue);
            for (Pot pot : pots) {
                // Determine how many winners share this pot.
                int noOfWinnersInPot = 0;
                for (Player winner : winners) {
                    if (pot.hasContributer(winner)) {
                        noOfWinnersInPot++;
                    }
                }
                if (noOfWinnersInPot > 0) {
                    // Divide pot over winners.
                	long potShare = pot.getValue() / noOfWinnersInPot;
                    for (Player winner : winners) {
                        if (pot.hasContributer(winner)) {
                        	Long oldShare = potDivision.get(winner);
                            if (oldShare != null) {
                                potDivision.put(winner, oldShare + potShare);
                            } else {
                                potDivision.put(winner, potShare);
                            }
                            
                        }
                    }
                    // Determine if we have any odd chips left in the pot.
                    long oddChips = pot.getValue() % noOfWinnersInPot;
                    if (oddChips > 0) {
                        // Divide odd chips over winners, starting left of the dealer.
                        pos = dealerPosition;
                        
                        while (oddChips > 0) {
                            pos = (pos + 1) % players.size();
                            Player winner = players.get(pos);
                            Long oldShare = potDivision.get(winner);
                            if (oldShare != null) {
                                potDivision.put(winner, oldShare + 1);
                                log.info("{} receives an odd chip from the pot", winner);
                                oddChips--;
                            }
                        }
                        
                    }
                    pot.clear();
                }
            }
        }
        
        // Divide winnings.
        StringBuilder winnerText = new StringBuilder();
        int totalWon = 0;
        for (Player winner : potDivision.keySet()) {
            Long potShare = potDivision.get(winner);
            winner.win(potShare);
            
            //Increase cash of player
			PkUser user = PokerMananger.getUser(winner.getLoginId());
			if(user != null) {
				PokerMananger.getPokerBusiness().changeCashBalance(user, potShare, PkCashflowType.WIN, 
						PkCashflowSourceType.GAME, gameHistoryId, PkCashflowStatus.ACTIVE.getNumber());
			}
            
            totalWon += potShare;
            if (winnerText.length() > 0) {
                winnerText.append(", ");
            }
            winnerText.append(String.format("%s wins $u %d", winner, potShare));
        }
        winnerText.append('.');
        log.info(winnerText.toString());
        
        // Sanity check.
        if (totalWon != totalPot) {
            throw new IllegalStateException(String.format("Incorrect pot division! TotalWon (%s) <> ToltalPot (%s)", totalWon, totalPot));
        }
        
        //Update game status
        PokerMananger.getPokerBusiness().updatePkGameHistory(gameHistoryId, new BigDecimal(totalWon), winnerText.toString());
        
        notifyGameEnd(RoundType.SHOW_DOWN);
    }
    
    /**
     * Returns the total chip size.
     * 
     * @return The total chip size.
     */
    public Long getTotalChips() {
    	Long totalChip = 0L;
    	
    	synchronized (pots) {
    		for (Pot pot : pots) {
                totalChip += pot.getValue();
            }
		}
        
        return totalChip;
    }
    
    /**
     * Notifies clients that a player has acted.
     */
    private void notifyPlayerActed(RoundType roundType, Player curActor, PlayerAction action, String nextPlayerId) {
    	log.info("[start] notifyPlayerActed, roundType: {}", roundType);
    	
    	PlayerActionEvent.Builder event = PlayerActionEvent.newBuilder();
    	event.setRoundType(roundType);
    	
    	event.setPlayerId(curActor.getLoginId());
    	event.setActionType(action.getActionType());
    	event.setRemainAmount(curActor.getCash());
    	event.setRaiseAmount(action.getAmount());
    	event.setTotalRaiseAmount(curActor.getTotalBet());
    	
    	event.setNextPlayerId(nextPlayerId);
    	
    	//Add chips
		Iterator<com.mrmq.poker.common.bean.Pot> chipIter = this.pots.iterator();
		while(chipIter.hasNext()) {
			event.addCenterChips(Converter.convertChip(chipIter.next()));
		}
    	
		//Response to Actor
		PlayerActionResponse toActor = Converter.convertPlayerActionResponse(event.build());
		RpcMessage.Builder rpcToActor = PokerMananger.createRpcMessage();
		rpcToActor.setPayloadClass(PlayerActionResponse.getDescriptor().getName());
		rpcToActor.setPayloadData(toActor.toByteString());
    	
		//Notify to other player
		RpcMessage.Builder rpcToViewBuilder = PokerMananger.createRpcMessage();
    	rpcToViewBuilder.setPayloadClass(PlayerActionEvent.getDescriptor().getName());
    	rpcToViewBuilder.setPayloadData(event.build().toByteString());
    	RpcMessage rpcToViewer = rpcToViewBuilder.build();
    	
    	Iterator<Player> it = players.iterator();
    	Player player;
    	
    	//Notify to other Player
    	while(it.hasNext()) {
    		player = it.next();
    		if(!player.getLoginId().equals(action.getPlayerId()))
    			player.getClient().send(rpcToViewer);
    		else
    			player.getClient().send(rpcToActor.build());
    	}
    	
    	//Notify to viewer
    	if(viewers.size() > 0) {
	    	it = viewers.iterator();
	    	while(it.hasNext()) {
	    		player = it.next();
	    		player.getClient().send(rpcToViewer);
	    	}
    	}
    	
    	log.info("[end] notifyPlayerActed");
    }

    /**
     * Notifies clients that a player has acted.
     */
    private void notifyGameStared(RoundType roundType, List<Player> actionPlayers, String dealerId, String nextPlayerId) {
    	log.info("[start] notifyGameStared, roundType: {}", roundType);
    	
    	StartGameEvent.Builder event = StartGameEvent.newBuilder();
    	event.setRoundType(roundType);
    	
    	//Dealer
    	event.setDealerId(dealerId);
    	
    	//SmallBlind & BigBlind
    	event.setSmallBlindId(actionPlayers.get(0).getLoginId());
    	event.setBigBlindId(actionPlayers.get(1).getLoginId());
    	
    	//First player
    	event.setNextPlayerId(nextPlayerId);
		
		synchronized (players) {
			//Return all Player
			Player temp;
			Iterator<Player> playerIter = players.iterator();
			while(playerIter.hasNext()) {
				temp = playerIter.next();
				if(PlayerState.PLAYER_PLAYING.equals(temp.getPlayState()))
					event.addPlayers(Converter.convertPlayer(temp));
			}
		}
		
		
		//Create rpc
		RpcMessage.Builder rpc = PokerMananger.createRpcMessage();
    	rpc.setPayloadClass(StartGameEvent.getDescriptor().getName());
    	rpc.setPayloadData(event.build().toByteString());
    	
    	Iterator<Player> it = players.iterator();
    	Player player;
    	
    	//Notify to Player
    	while(it.hasNext()) {
    		player = it.next();
    		player.getClient().send(rpc.build());
    	}
    	
    	//Notify to viewer
    	it = viewers.iterator();
    	while(it.hasNext()) {
    		player = it.next();
    		player.getClient().send(rpc.build());
    	}
    	log.info("[end] notifyGameStared");
    }
    
    /**
     * Notifies clients that a player has acted.
     */
    private void notifyNewRoundStarted(RoundType roundType, List<Card> addCards, List<Pot> addChips, String dealerId, String nextPlayerId) {
    	log.info("[start] notifyNewRoundStared, roundType: {}", roundType);
    	
    	NewRoundEvent.Builder event = NewRoundEvent.newBuilder();
    	event.setRoundType(roundType);
    	
    	//First player
    	event.setFirstPlayerId(nextPlayerId);
    	
    	//Add common card
		Iterator<Card> cardIt = addCards.iterator();
		while(cardIt.hasNext()) {
			event.addAddCards(Converter.convertCard(cardIt.next()));
		}
    	
		//Add chips
		Iterator<Pot> chipIter = addChips.iterator();
		while(chipIter.hasNext()) {
			event.addCenterChips(Converter.convertChip(chipIter.next()));
		}
		event.setMyRank(com.mrmq.poker.common.proto.PokerModelProto.HandValue.HIGH_CARD);
		
		RpcMessage.Builder rpc = PokerMananger.createRpcMessage();
		
    	rpc.setPayloadClass(NewRoundEvent.getDescriptor().getName());
    	rpc.setPayloadData(event.build().toByteString());
    	
    	Iterator<Player> it = players.iterator();
    	Player player;
    	
    	//Notify to Player
    	while(it.hasNext()) {
    		player = it.next();
    		player.getClient().send(rpc.build());
    	}
    	
    	//Notify to viewer
    	it = viewers.iterator();
    	while(it.hasNext()) {
    		player = it.next();
    		player.getClient().send(rpc.build());
    	}
    	log.info("[end] notifyNewRoundStared");
    }
    
    private void notifyGameEnd(RoundType roundType) {
    	log.info("[start] notifyGameEnd, roundType: {}", roundType);
    	
    	EndGameEvent.Builder event = EndGameEvent.newBuilder();
    	event.setRoundType(roundType);
    	
    	//Center chip
    	Iterator<Pot> potIter = pots.iterator();
		while(potIter.hasNext()) {
			event.addCenterChips(Converter.convertChip(potIter.next()));
		}
    	
		//Add history card
		Iterator<Card> cardIt = board.iterator();
		while(cardIt.hasNext()) {
			event.addHistoryCards(Converter.convertCard(cardIt.next()));
		}
				
    	//Add players
		Iterator<Player> playerIter = getPlayers().iterator();
		while(playerIter.hasNext()) {
			event.addPlayers(Converter.convertPlayer(playerIter.next()));
		}
		
		//Create rpc
		RpcMessage.Builder rpc = PokerMananger.createRpcMessage();
    	rpc.setPayloadClass(EndGameEvent.getDescriptor().getName());
    	rpc.setPayloadData(event.build().toByteString());
    	
    	Iterator<Player> it = players.iterator();
    	Player player;
    	
    	//Notify to Player
    	while(it.hasNext()) {
    		player = it.next();
    		player.getClient().send(rpc.build());
    	}
    	
    	//Notify to viewer
    	it = viewers.iterator();
    	while(it.hasNext()) {
    		player = it.next();
    		player.getClient().send(rpc.build());
    	}
    	
    	log.info("[end] notifyGameEnd");
    }
    
    private void notifyAddPlayer(Player player, JoinTableType joinType) {
    	//Notify to other player
		JoinTableEvent.Builder joinTableEventB = JoinTableEvent.newBuilder();
		joinTableEventB.setJoinType(joinType);
		joinTableEventB.setPlayer(Converter.convertPlayer(player));
		ByteString joinTableEvent = joinTableEventB.build().toByteString();
		
		Iterator<Player> playersIter = getPlayers().iterator();
		while (playersIter.hasNext()) {
			Player playing = playersIter.next();
			if(!playing.getLoginId().equals(player.getLoginId()))
				playing.getClient().send(JoinTableEvent.getDescriptor().getName(), joinTableEvent);
		}
    }
    
    /**
     * Adds a player.
     * 
     * @param player - The player
     * @param joinType - VIEW, LEAVE OR PLAY
     */
    public MsgCode addPlayer(Player player, JoinTableType joinType) {
    	int size = 0;
    	MsgCode msgCode = MsgCode.FAIL;
    	
		if(JoinTableType.PLAY == joinType) {
			synchronized (players) {
				size = players.size();
				
				if(size < tableRule.getMaxPlayer() || players.contains(player)) {
					player.setPlayState(PlayerState.PLAYER_WAITTING);
					
					if(!players.contains(player)) {
						Iterator<Player> it = players.iterator();
						int index = 0;
						while(it.hasNext()) {
							if(it.next().getPosition() >= player.getPosition())
								break;
							index++;
						}
						players.add(index, player);
					} else {
						//Replace old player with new player
						for(int i = 0; i < players.size(); i++)
							if(players.get(i).equals(players)) {
								Player old = players.get(i);
								player.setBet(old.getBet());
								player.setTotalBet(old.getTotalBet());
								players.set(i, player);
							}
					}
    				msgCode = MsgCode.SUCCESS;
    			}
				
				notifyAddPlayer(player, joinType);
			}
		} else if(JoinTableType.VIEW == joinType) {
			synchronized (viewers) {
				size = viewers.size();
				
				if(size < tableRule.getMaxViewer()) {
					player.setPlayState(PlayerState.PLAYER_VIEWING);
					viewers.add(player);
					msgCode = MsgCode.SUCCESS;
    			}
			}
		} else if(JoinTableType.LEAVE == joinType) {
			if(players.contains(player)) {
				if(player.isPlayed()) {
					synchronized (players) {
						players.remove(player);
						notifyAddPlayer(player, joinType);
					}
				}
			} else {
				synchronized (viewers) {
					viewers.remove(player);
				}
			}
			
			msgCode = MsgCode.SUCCESS;
		}
		
		return msgCode;
    }
    
    private BlockingQueue<PlayerAction> queue = new LinkedBlockingQueue<>();
	private Object actionLock = new Object();
    
    public MsgCode onAction(final PlayerAction action) throws InterruptedException {
		MsgCode msgCode = MsgCode.FAIL;
		
		log.info("onAction: {}, {}", action.getPlayerId(), action.getActionType());
		msgCode = MsgCode.SUCCESS;
		queue.put(action);
		
		return msgCode;
	}
    
    public PlayerAction waitForPlayerAction(Player player, long minBet, long currentBet, Set<ActionType> allowedActions) throws Exception {
		startWaitTime = System.currentTimeMillis();
		
    	log.info("waiting for player {} action", player.getLoginId());
		PlayerAction curAction = null;
		PlayerAction ruleAction;
		long start = System.currentTimeMillis();
		
		synchronized (actionLock) {
			ruleAction = PlayerAction.newBuilder()
					.setPlayerId(player.getLoginId())
					.setAmount(currentBet)
					.setActionType(ActionType.BET).build();
		}
		
		while(System.currentTimeMillis() - start < getTableRule().getTimePerturn()) {
			try {
				curAction = queue.poll(100, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				log.error(e.getMessage(), e);
			}
			if(curAction != null) {
				if(player.getLoginId().equals(curAction.getPlayerId()))
					break;
				else
					log.warn("waiting for player {} action, but {} act. Ignore {} action", player.getLoginId(), curAction.getPlayerId(), curAction.getPlayerId());
			}
		}
		
		if(curAction == null) {
			log.info("Timed-out waiting for player {} action, set action = FOLD", player.getLoginId());
			curAction = PlayerAction.newBuilder()
					.setPlayerId(player.getLoginId())
					.setAmount(0)
					.setActionType(ActionType.FOLD)
					.build();
		} else {
			ruleAction = null;
			if(curAction.getActionType() == ActionType.CALL 
					|| curAction.getActionType() == ActionType.RAISE
					|| curAction.getActionType() == ActionType.ALL_IN) {								
			}
				
			log.info("Player {} action: {}", player.getLoginId(), curAction);
		}
		return curAction;
	}
        
	public String getTableId() {
		return tableId;
	}

	public void setTableId(String tableId) {
		this.tableId = tableId;
	}

	@Override
	public String getId() {
		return tableId;
	}

	@Override
	public boolean isAlive() {
		return isAlive.get();
	}
    
	private int getPlayerToAct() {
    	int count = 0;
    	
    	synchronized (players) {
    		for (Player player : players) {
    			if(player.getPlayState() == PlayerState.PLAYER_PLAYING)
    				count++;
    		}
		}
    	
    	return count;
    }
	
	private int getPlayerToPlay() {
    	int count = 0;
    	
    	synchronized (players) {
    		// Determine the active players.
            Iterator<Player> it = players.iterator();
            while(it.hasNext()) {
            	Player player = it.next();
                player.resetHand();
                
                if(player.getClient() == null || !player.getClient().isAuthenticated()) {
                	log.info("Player {} session {} released", player.getLoginId(), player.getClient());
                	player.setPlayState(PlayerState.PLAYER_VIEWING);
                	it.remove();
                } else if (player.getCash() < tableRule.getBigBlind().longValue()) {
                	log.info("Player {} does not has enough money, current money {}, require {}, ", player.getLoginId(), player.getCards(), tableRule.getBigBlind());
                	player.setPlayState(PlayerState.PLAYER_VIEWING);
                	it.remove();
                } else {
                	// Player must be able to afford at least the big blind.
                    player.setPlayState(PlayerState.PLAYER_WAITTING);
                    count++;
                }
            }
		}
    	
    	return count;
    }
	
	private Player getLastPlayerToWin() {
		Player lastPlayer = null;
		
    	synchronized (players) {
    		for (Player player : players) {
    			if(player.getPlayState() == PlayerState.PLAYER_PLAYING) {
    				lastPlayer = player;
    				break;
    			}
    		}
		}
    	
    	return lastPlayer;
    }
	
	public int getViewerCount() {
		return viewers == null ? 0 : viewers.size();
	}

	public PkGame getTableRule() {
		return tableRule;
	}

	public List<Player> getPlayers() {
		return players;
	}

	public List<Card> getBoard() {
		return board;
	}

	@Override
	public String toString() {
		return "PokerTable [tableId=" + tableId + ", gameId=" + gameHistoryId + ", tableRule=" + tableRule + "]";
	}
	
	public Player getActor() {
		return actor;
	}
	
	public Player getDealer() {
		return dealer;
	}

	public boolean isPlaying() {
		return isPlaying;
	}

	public long getStartWaitTime() {
		return startWaitTime;
	}

	public void setStartWaitTime(long startWaitTime) {
		this.startWaitTime = startWaitTime;
	}

	public RoundType getCurRoundType() {
		return curRoundType;
	}

	public void setCurRoundType(RoundType curRoundType) {
		this.curRoundType = curRoundType;
	}

}
