package com.mrmq.poker.common.bean;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mrmq.poker.common.proto.PokerModelProto.ActionType;
import com.mrmq.poker.common.proto.PokerModelProto.PlayerState;
import com.mrmq.util.StringHelper;

/**
 * A Texas Hold'em player
 * 
 * The player's actions are delegated to a {@link Client}
 * 
 */
public class Player {
	private static Logger log = LoggerFactory.getLogger(Player.class);
	
	private PlayerState playState = PlayerState.PLAYER_VIEWING;
	
	private String loginId;
    private String name;

    /** Client application responsible for the actual behavior. */
    private final Client client;

    /** Hand of cards. */
    private final Hand hand;
    private HandValue handValue;
    
    /** Current amount of cash. */
    private Long cash;
    
    /** Whether the player has hole cards. */
    private boolean hasCards;

    /** Current bet. */
    private long bet;
    private long totalBet;

    /** Last action performed. */
    private ActionType action;
    private int position = 0;
    private String avataUrl;
    
    /**
     * Constructor.
     * 
     * @param name The player's name.
     * @param cash The player's starting amount of cash.
     * @param client The client application.
     */
    public Player(String name, Long cash, Client client) {
        this.name = name;
        this.cash = cash;
        this.client = client;

        hand = new Hand();

        resetHand();
    }

    /**
     * Returns the client.
     * 
     * @return The client.
     */
    public Client getClient() {
        return client;
    }

    /**
     * Prepares the player for another hand.
     */
    public void resetHand() {
        hasCards = false;
        hand.removeAllCards();
        totalBet = 0;
        resetBet();
    }

    /**
     * Resets the player's bet.
     */
    public void resetBet() {
        bet = 0;
        action = (hasCards() && cash == 0) ? ActionType.ALL_IN : null;
    }

    /**
     * Sets the hole cards.
     */
    public void setCards(List<Card> cards) {
        hand.removeAllCards();
        if (cards != null) {
            if (cards.size() == 2) {
                hand.addCards(cards);
                hasCards = true;
                log.info("Deal {}'s cards:\t{}", loginId, hand);
                handValue = new HandValue(hand);
            } else {
                throw new IllegalArgumentException("Invalid number of cards");
            }
        }
    }

    /**
     * Returns whether the player has his hole cards dealt.
     * 
     * @return True if the hole cards are dealt, otherwise false.
     */
    public boolean hasCards() {
        return hasCards;
    }

    /**
     * Returns the player's name.
     * 
     * @return The name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the player's current amount of cash.
     * 
     * @return The amount of cash.
     */
    public Long getCash() {
        return cash;
    }

    /**
     * Returns the player's current bet.
     * 
     * @return The current bet.
     */
    public long getBet() {
        return bet;
    }
    
    /**
     * Sets the player's current bet.
     * 
     * @param bet The current bet.
     */
    public void setBet(long bet) {
        this.bet = bet;
    }

    /**
     * Returns the player's most recent action.
     * 
     * @return The action.
     */
    public ActionType getAction() {
        return action;
    }
    
    /**
     * Sets the player's most recent action.
     * 
     * @param action The action.
     */
    public void setAction(ActionType action) {
        this.action = action;
    }

    /**
     * Indicates whether this player is all-in.
     * 
     * @return True if all-in, otherwise false.
     */
    public boolean isAllIn() {
        return hasCards() && (cash == 0);
    }

    public boolean isPlayed() {
        return playState == PlayerState.PLAYER_PLAYING || playState == PlayerState.PLAYER_FOLDED;
    }
    
    /**
     * Returns the player's hole cards.
     * 
     * @return The hole cards.
     */
    public Card[] getCards() {
        return hand.getCards();
    }

    /**
     * Posts the small blind.
     * 
     * @param blind The small blind.
     */
    public void postSmallBlind(long blind) {
        action = ActionType.POST_SMALL_BLIND;
        cash -= blind;
        bet += blind;
        totalBet += blind;
    }

    /**
     * Posts the big blinds.
     * 
     * @param blind The big blind.
     */
    public void postBigBlind(long blind) {
        action = ActionType.POST_BIG_BLIND;
        cash -= blind;
        bet += blind;
        totalBet += blind;
    }
    
    /**
     * Pays an amount of cash.
     * 
     * @param amount
     *            The amount of cash to pay.
     */
    public void payCash(long amount) {
    	synchronized (cash) {
    		if (amount > cash) {
                throw new IllegalStateException("Player asked to pay more cash than he owns!");
            }
            cash -= amount;
            totalBet += amount;
		}
    }
    
    /**
     * Wins an amount of money.
     * 
     * @param amount
     *            The amount won.
     */
    public void win(long amount) {
        cash += amount;
    }

    /**
     * Returns a clone of this player with only public information.
     * 
     * @return The cloned player.
     */
    public Player publicClone() {
        Player clone = new Player(name, cash, null);
        clone.hasCards = hasCards;
        clone.bet = bet;
        clone.action = action;
        return clone;
    }

	public PlayerState getPlayState() {
		return playState;
	}

	public void setPlayState(PlayerState playState) {
		this.playState = playState;
	}

	public String getLoginId() {
		return loginId;
	}

	public void setLoginId(String loginId) {
		this.loginId = loginId;
	}

	public long getTotalBet() {
		return totalBet;
	}

	public void setTotalBet(long totalBet) {
		this.totalBet = totalBet;
	}

	public HandValue getHandValue() {
		return handValue;
	}

	public void setHandValue(HandValue handValue) {
		this.handValue = handValue;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null)
			return false;
		if(obj instanceof Player)
			return StringHelper.isEqual(this.getLoginId(), ((Player) obj).getLoginId());
		return false;
	}
	
	@Override
	public String toString() {
		return "Player=" + loginId;
	}

	public String getAvataUrl() {
		return avataUrl;
	}

	public void setAvataUrl(String avata) {
		this.avataUrl = avata;
	}
	
}
