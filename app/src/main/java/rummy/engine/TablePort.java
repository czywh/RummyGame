package rummy.engine;

import ch.aplu.jcardgame.*;
import ch.aplu.jgamegrid.*;

import java.awt.*;
import java.util.*;
import java.util.List;
import rummy.model.Player;
import rummy.meld.MeldFinder;
import rummy.Rank;
import rummy.Suit;



/**
 * TablePort - Table port (Port Pattern)
 */
public class TablePort {
    private final Deck deck;
    private final boolean ginMode;
    private final Random random = new Random(30008);
    private Hand stock;
    private Hand discard;
    private final Map<Player, Hand> hands = new HashMap<>();
    private int currentRound = 0;
    private boolean roundEnded = false; // Round end flag

    private Card lastDrawn = null;      // Recently drawn card
    private Card lastDiscarded = null;  // Recently discarded card
    private String lastAction = null;   // Recent declaration action (KNOCK/GIN/RUMMY)
    private Player declarer = null;     // Declarer

    // Predefined moves (Auto mode)
    private final Map<Player, List<String>> playerAutoMovements = new HashMap<>();
    private final Map<Player, Integer> autoMovementIndex = new HashMap<>();

    public TablePort(Deck deck, boolean ginMode) {
        this.deck = deck;
        this.ginMode = ginMode;
    }

    //  Main dealing entry
    public void dealInitialHands(List<Player> players, int nbStartCards,
                                 Properties props, int roundNumber, CardGame game) {
        this.currentRound = roundNumber;


        stock = deck.toHand(false);
        discard = new Hand(deck);


        Location discardLoc = new Location(625, 350);
        RowLayout discardLayout = new RowLayout(discardLoc, 40);
        discardLayout.setRotationAngle(270);
        discard.setView(game, discardLayout);

        Location stockLoc = new Location(75, 350);
        RowLayout stockLayout = new RowLayout(stockLoc, 40);
        stockLayout.setRotationAngle(90);
        stock.setView(game, stockLayout);

        // 1. Generate Hand for each player
        for (Player p : players) {
            Hand h = new Hand(deck);
            hands.put(p, h);
        }

        // 2. Read initial cards from properties
        String roundKey = "rounds." + roundNumber;
        for (Player p : players) {
            String key = roundKey + ".players." + p.getId() + ".initialcards";
            String value = props.getProperty(key);
            if (value != null) {
                for (String cardStr : value.split(",")) {
                    cardStr = cardStr.trim();
                    if (cardStr.isEmpty()) continue;
                    Card c = getCardByShortName(stock, cardStr);
                    if (c != null) {
                        c.removeFromHand(false);
                        hands.get(p).insert(c, false);
                    }
                }
            }
        }

        // 3. Complete hands to nbStartCards
        for (Player p : players) {
            Hand h = hands.get(p);
            int need = nbStartCards - h.getNumberOfCards();
            for (int j = 0; j < need && !stock.isEmpty(); j++) {
                Card c = randomCard(stock);
                c.removeFromHand(false);
                h.insert(c, false);
            }
        }

        // 4. Set stockpile top card order
        String stockKey = roundKey + ".stockpile.cards";
        String stockValue = props.getProperty(stockKey);
        if (stockValue != null) {
            List<Card> list = stock.getCardList();
            String[] topCards = stockValue.split(",");
            // Move specified cards to top of pile
            for (int i = topCards.length - 1; i >= 0; i--) {
                String cardStr = topCards[i].trim();
                if (cardStr.isEmpty()) continue;
                Card card = getCardByShortName(stock, cardStr);
                if (card != null) {
                    list.remove(card);
                    list.add(card);
                }
            }
        }

        // 5 Read predefined moves (Auto mode)
        setupPlayerAutoMovements(players, props, roundNumber);

        // 6. ayout UI
        setupLayout(game, players);
    }

    /**
     * Read player's predefined moves (from properties)
     */
    private void setupPlayerAutoMovements(List<Player> players, Properties props, int roundNumber) {
        playerAutoMovements.clear();
        autoMovementIndex.clear();

        String roundKey = "rounds." + roundNumber;
        for (Player p : players) {
            String key = roundKey + ".players." + p.getId() + ".cardsPlayed";
            String movementString = props.getProperty(key, "");

            List<String> movements = new ArrayList<>();
            if (!movementString.isEmpty()) {
                for (String m : movementString.split(",")) {
                    if (!m.trim().isEmpty()) {
                        movements.add(m.trim());
                    }
                }
            }

            playerAutoMovements.put(p, movements);
            autoMovementIndex.put(p, 0);
        }
    }

    // UI Layout Helper
    private void setupLayout(CardGame game, List<Player> players) {
        if (game == null) {
            throw new IllegalStateException("CardGame is null in setupLayout!");
        }
        if (stock == null || discard == null) {
            throw new IllegalStateException("Stock or discard Hand is null in setupLayout!");
        }

        // stock and discard setView() already completed in dealInitialHands()
        // Only need draw() here
        stock.draw();
        discard.draw();

        // Set up player hand UI
        Location[] handLoc = {new Location(350, 75), new Location(350, 625)};
        int handWidth = 400;

        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            Hand h = hands.get(p);
            RowLayout hl = new RowLayout(handLoc[i], handWidth);
            hl.setRotationAngle(i == 0 ? 0 : 180);
            h.setView(game, hl);
            h.draw();
        }
    }

    /**
     * Set up UI listeners for human player
     */
    public void setupHumanPlayerListeners(RummyGame game, Player humanPlayer) {
        // 1. Add listener for stockpile (Stock)
        if (stock != null) {
            stock.addCardListener(new CardAdapter() {
                @Override
                public void leftDoubleClicked(Card card) {
                    game.drawnCardFromPile = card;
                    stock.remove(card, false);
                    stock.setTouchEnabled(false);
                    stock.draw();
                }
            });
        }

        // 2. Add listener for discard pile (Discard)
        if (discard != null) {
            discard.addCardListener(new CardAdapter() {
                @Override
                public void leftDoubleClicked(Card card) {
                    game.drawnCardFromPile = card;
                    discard.remove(card, false);
                    discard.setTouchEnabled(false);
                    discard.draw();
                }
            });
        }

        // 3. Add listener for human player's hand
        Hand humanHand = hands.get(humanPlayer);
        if (humanHand != null) {
            humanHand.addCardListener(new CardAdapter() {
                @Override
                public void leftDoubleClicked(Card card) {
                    game.selectedCard = card;
                    humanHand.setTouchEnabled(false);
                }
            });
        }
    }

    /**
     * Enable/disable stockpile touch
     */
    public void setPilesTouchEnabled(boolean enabled) {
        if (stock != null) {
            stock.setTouchEnabled(enabled);
        }
        if (discard != null && !discard.isEmpty()) {
            discard.setTouchEnabled(enabled);
        }
    }

    /**
     * Enable/disable player hand touch
     */
    public void setHandTouchEnabled(Player player, boolean enabled) {
        Hand hand = hands.get(player);
        if (hand != null) {
            hand.setTouchEnabled(enabled);
        }
    }

    /**
     * Sort hand (for better UI display)
     */
    public void sortHand(Player player) {
        Hand hand = hands.get(player);
        if (hand == null) return;

        List<Card> cards = hand.getCardList();
        cards.sort((o1, o2) -> {
            Suit suit1 = (Suit) o1.getSuit();
            Suit suit2 = (Suit) o2.getSuit();
            Rank rank1 = (Rank) o1.getRank();
            Rank rank2 = (Rank) o2.getRank();

            if (suit1.ordinal() != suit2.ordinal()) {
                return suit1.ordinal() - suit2.ordinal();
            }

            return rank1.getShortHandValue() - rank2.getShortHandValue();
        });
    }

    // =============== Basic Operations ===============
    public Card peekDiscardTop() {
        return discard.isEmpty() ? null : discard.getLast();
    }

    public Card drawFromStock(Player p) {
        if (stock.isEmpty()) return null;
        Card c = stock.getLast();
        //  Use old project method: removeFromHand + insert
        c.removeFromHand(false);
        getHand(p).insert(c, false);
        lastDrawn = c; // Record drawn card
        return c;
    }

    public Card drawFromDiscard(Player p) {
        if (discard.isEmpty()) return null;
        Card c = discard.getLast();
        //  Use old project method: removeFromHand + insert
        c.removeFromHand(false);
        getHand(p).insert(c, false);
        lastDrawn = c; // Record drawn card
        return c;
    }

    public void discard(Player p, Card c) {
        if (c == null) return;

        // Defensive check: ensure discard Hand is initialized
        if (discard == null) {
            throw new IllegalStateException("Discard pile is null! dealInitialHands() may not have been called.");
        }

        //   removeFromHand + insert
        c.removeFromHand(false);
        discard.insert(c, false);
        discard.draw(); //  refresh discard pile display
        lastDiscarded = c; // Record discarded card
    }

    // =============== Helper Functions ===============
    private Card randomCard(Hand hand) {
        List<Card> list = hand.getCardList();
        if (list.isEmpty()) return null;
        return list.get(random.nextInt(list.size()));
    }

    private Card getCardByShortName(Hand hand, String name) {
        // For example "7S" -> Rank 7, Suit SPADES
        if (name.length() < 2) return null;
        String rankPart = name.substring(0, name.length() - 1);
        String suitPart = name.substring(name.length() - 1);
        int rankVal = Integer.parseInt(rankPart);
        for (Card c : hand.getCardList()) {
            Rank r = (Rank) c.getRank();
            Suit s = (Suit) c.getSuit();
            if (r.getShortHandValue() == rankVal && s.getSuitShortHand().equals(suitPart)) {
                return c;
            }
        }
        return null;
    }


    // =============== getters ===============
    public Hand getStock() { return stock; }
    public Hand getDiscard() { return discard; }
    public Hand getHand(Player p) { return hands.get(p); }
    public boolean isGinMode() { return ginMode; }



    // =============== Turn Control ===============
    /**
     * End current turn (called by DeclarePolicy)
     */
    public void endRound(Player declarer, String action) {
        this.roundEnded = true;
        this.declarer = declarer;
        this.lastAction = action; // Record declaration action
    }

    /**
     * End current turn (no declaration)
     */
    public void endRound() {
        this.roundEnded = true;
    }

    /**
     * Reset round state (called at start of each new round)
     */
    public void resetRound() {
        this.roundEnded = false;
    }

    /**
     * Reset turn log (called at start of each new player turn)
     */
    public void resetTurnLog() {
        this.lastDrawn = null;
        this.lastDiscarded = null;
        this.lastAction = null;
        this.declarer = null;
    }

    /**
     * Check if round has ended
     */
    public boolean isRoundEnded() {
        return roundEnded;
    }

    /**
     * Check if stockpile is empty
     */
    public boolean isStockEmpty() {
        return stock == null || stock.isEmpty();
    }

    /**
     * Check if discard pile is empty
     */
    public boolean isDiscardEmpty() {
        return discard == null || discard.isEmpty();
    }

    // =============== Log Retrieval ===============
    public Card getLastDrawn() { return lastDrawn; }
    public Card getLastDiscarded() { return lastDiscarded; }
    public String getLastAction() { return lastAction; }
    public Player getDeclarer() { return declarer; }

    // =============== Predefined Move Management ===============
    /**
     * Check if player has any predefined moves
     */
    public boolean hasAutoMovement(Player p) {
        List<String> movements = playerAutoMovements.get(p);
        int index = autoMovementIndex.get(p);
        return movements != null && index < movements.size();
    }

    /**
     * Execute player's next predefined move
     * @return Whether round should end (because declared RUMMY/GIN/KNOCK)
     */
    public boolean executeAutoMovement(Player p) {
        if (!hasAutoMovement(p)) {
            return false; // No more predefined moves
        }

        List<String> movements = playerAutoMovements.get(p);
        int index = autoMovementIndex.get(p);
        String movement = movements.get(index);
        autoMovementIndex.put(p, index + 1);

        // Parse move: format "DISCARD-10C" or "STOCKPILE-12S" or "STOCKPILE-3D-RUMMY"
        String[] parts = movement.split("-");
        if (parts.length < 2) {
            return false; // Format error
        }

        String action = parts[0]; // DISCARD or STOCKPILE
        String cardToDiscardStr = parts[1]; // Card to discard, e.g. "10C"
        String declareAction = parts.length > 2 ? parts[2] : null; // RUMMY/GIN/KNOCK

        // 1. Draw card
        Card drawnCard;
        if ("DISCARD".equals(action)) {
            drawnCard = drawFromDiscard(p);
        } else { // STOCKPILE
            drawnCard = drawFromStock(p);
        }

        // 2. Discard card
        Hand hand = getHand(p);
        Card cardToDiscard = findCardByName(hand, cardToDiscardStr);
        if (cardToDiscard != null) {
            discard(p, cardToDiscard);
        }

        // 3. Declaration
        if (declareAction != null) {
            endRound(p, declareAction);
            return true; // End round
        }

        return false; // Continue round
    }

    /**
     * Find card by short name (e.g. "10C" = 10 of Clubs)
     */
    private Card findCardByName(Hand hand, String cardName) {
        if (cardName == null || cardName.length() < 2) return null;

        // Parse rank and suit
        String rankPart = cardName.substring(0, cardName.length() - 1);
        String suitPart = cardName.substring(cardName.length() - 1);

        int rankValue;
        try {
            rankValue = Integer.parseInt(rankPart);
        } catch (NumberFormatException e) {
            return null;
        }

        // Traverse hand to find matching card
        for (Card c : hand.getCardList()) {
            Rank r = (Rank) c.getRank();
            Suit s = (Suit) c.getSuit();
            if (r.getShortHandValue() == rankValue && s.getSuitShortHand().equals(suitPart)) {
                return c;
            }
        }

        return null;
    }
}