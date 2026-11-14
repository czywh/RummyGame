package rummy;

import ch.aplu.jcardgame.*;
import ch.aplu.jgamegrid.*;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class Round {
    private final Rummy game;
    private final int roundNumber;
    private final Properties properties;
    private final StringBuilder logResult;
    
    // Round-specific state
    private Hand[] hands;
    private Hand pack;
    private Hand discard;
    private Hand playingArea;
    private Card selected;
    private Card drawnCard;
    private boolean isEndingTurn = false;
    
    // Auto movement tracking
    private final List<List<String>> playerAutoMovements = new ArrayList<>();
    private final int[] autoIndexHands = new int[2];
    
    // Constants
    private final int nbPlayers = 2;
    private final int nbStartCards;
    private final int thinkingTime;
    private final int delayTime;
    private final boolean isAuto;
    private final int COMPUTER_PLAYER_INDEX = 0;
    private final int HUMAN_PLAYER_INDEX = 1;
    
    // Locations
    private final Location[] handLocations = {
            new Location(350, 75),
            new Location(350, 625),
    };
    private final Location playingLocation = new Location(350, 350);
    private final Location packLocation = new Location(75, 350);
    private final Location discardLocation = new Location(625, 350);
    private final Location packNameLocation = new Location(30, 280);
    private final Location discardNameLocation = new Location(560, 280);
    private final int handWidth = 400;
    private final int pileWidth = 40;
    private final int cardWidth = 40;
    
    // UI Components
    private TextActor packNameActor;
    private TextActor discardNameActor;
    private final GGButton endTurnActor;
    private final Location endTurnLocation = new Location(80, 610);
    
    enum CardAction {
        DISCARD,
        STOCKPILE,
        RUMMY,
        GIN,
        KNOCK,
        NONE;
    }
    
    public Round(Rummy game, int roundNumber, Properties properties, StringBuilder logResult) {
        this.game = game;
        this.roundNumber = roundNumber;
        this.properties = properties;
        this.logResult = logResult;
        this.nbStartCards = Integer.parseInt(properties.getProperty("number_cards", "13"));
        this.thinkingTime = Integer.parseInt(properties.getProperty("thinkingTime", "200"));
        this.delayTime = Integer.parseInt(properties.getProperty("delayTime", "50"));
        this.isAuto = Boolean.parseBoolean(properties.getProperty("isAuto"));
        this.endTurnActor = new GGButton("sprites/end.gif", false);
    }
    
    public boolean playRound() {
        initRound();
        return playARound();
    }
    
    private void initRound() {
        hands = new Hand[nbPlayers];
        for (int i = 0; i < nbPlayers; i++) {
            hands[i] = new Hand(null);
        }
        dealingOut(hands);

        for (int i = 0; i < nbPlayers; i++) {
            sortHand(hands[i]);
        }
        arrangeStockpile();

        playingArea = new Hand(null);
        playingArea.setView(game, new RowLayout(playingLocation, (playingArea.getNumberOfCards() + 3) * cardWidth));
        playingArea.draw();

        // Set up human player for interaction
        CardListener cardListener = new CardAdapter() {
            public void leftDoubleClicked(Card card) {
                selected = card;
                hands[HUMAN_PLAYER_INDEX].setTouchEnabled(false);
            }
        };
        hands[HUMAN_PLAYER_INDEX].addCardListener(cardListener);

        // graphics
        RowLayout[] layouts = new RowLayout[nbPlayers];
        for (int i = 0; i < nbPlayers; i++) {
            layouts[i] = new RowLayout(handLocations[i], handWidth);
            layouts[i].setRotationAngle(i);
            hands[i].setView(game, layouts[i]);
            hands[i].setTargetArea(new TargetArea(playingLocation));
            hands[i].draw();
        }

        setupPiles();
    }
    
    private boolean playARound() {
        int nextPlayer = HUMAN_PLAYER_INDEX;
        addRoundInfoToLog(roundNumber);
        addPlayerCardsToLog();
        int i = 0;
        boolean isContinue = true;
        setupPlayerAutoMovements();
        
        while (isContinue) {
            i++;
            addTurnInfoToLog(i);
            for (int j = 0; j < nbPlayers; j++) {
                Hand hand = hands[nextPlayer];

                if (isAuto) {
                    int nextPlayerAutoIndex = autoIndexHands[nextPlayer];
                    List<String> nextPlayerMovement = playerAutoMovements.get(nextPlayer);
                    String nextMovement = "";
                    boolean hasRunAuto = false;
                    if (nextPlayerMovement.size() > nextPlayerAutoIndex) {
                        nextMovement = nextPlayerMovement.get(nextPlayerAutoIndex);
                        if (!nextMovement.isEmpty()) {
                            hasRunAuto = true;
                            nextPlayerAutoIndex++;
                            autoIndexHands[nextPlayer] = nextPlayerAutoIndex;
                            game.setStatus("Player " + nextPlayer + " is playing");
                            
                            List<CardAction> cardActions = getActionFromAutoMovement(nextMovement);
                            CardAction cardAction = cardActions.get(0);
                            Card card = null;
                            
                            if (cardAction == CardAction.DISCARD) {
                                card = processTopCardFromPile(discard, hand);
                                selected = getCardElementFromAutoMovement(hand, nextMovement);
                                discardCardFromHand(selected, hand);
                            } else if (cardAction == CardAction.STOCKPILE) {
                                card = processTopCardFromPile(pack, hand);
                                selected = getCardElementFromAutoMovement(hand, nextMovement);
                                discardCardFromHand(selected, hand);
                            }

                            game.delay(thinkingTime);
                            if (cardActions.size() > 1) {
                                CardAction lastAction = cardActions.get(1);
                                if (lastAction == CardAction.RUMMY) {
                                    game.setStatus("Player " + nextPlayer + " declares Rummy!");
                                    isContinue = false;
                                    addCardPlayedToLog(nextPlayer, selected, card, lastAction.name());
                                    break;
                                } else if (lastAction == CardAction.GIN) {
                                    game.setStatus("Player " + nextPlayer + " declares Gin!");
                                    addCardPlayedToLog(nextPlayer, selected, card, lastAction.name());
                                    isContinue = false;
                                    break;
                                } else if (lastAction == CardAction.KNOCK) {
                                    game.setStatus("Player " + nextPlayer + " knocks!");
                                    addCardPlayedToLog(nextPlayer, selected, card, lastAction.name());
                                    isContinue = false;
                                    break;
                                }
                            } else {
                                addCardPlayedToLog(nextPlayer, selected, card, null);
                            }
                            game.delay(delayTime);
                        }
                    }

                    if (!hasRunAuto) {
                        processNonAutoPlaying(nextPlayer, hand);
                    }
                }

                if (!isAuto) {
                    processNonAutoPlaying(nextPlayer, hand);
                }

                if (pack.isEmpty()) {
                    game.setStatus("Stockpile is exhausted. Calculating players' scores now.");
                    isContinue = false;
                }

                nextPlayer = (nextPlayer + 1) % nbPlayers;
            }
        }
        addEndOfRoundToLog();
        return false;
    }
    
    // Helper methods (moved from Rummy class)
    private void setupPiles() {
        discard = new Hand(null);
        RowLayout discardLayout = new RowLayout(discardLocation, pileWidth);
        discardLayout.setRotationAngle(270);
        discard.setView(game, discardLayout);
        discard.draw();
        discardNameActor = new TextActor("Discard Pile", Color.WHITE, game.getBgColor(), new Font("Arial", Font.BOLD, 18));
        game.addActor(discardNameActor, discardNameLocation);

        RowLayout packLayout = new RowLayout(packLocation, pileWidth);
        packLayout.setRotationAngle(90);
        pack.setView(game, packLayout);
        pack.draw();
        packNameActor = new TextActor("Stockpile", Color.WHITE, game.getBgColor(), new Font("Arial", Font.BOLD, 18));
        game.addActor(packNameActor, packNameLocation);

        discard.addCardListener(new CardAdapter() {
            @Override
            public void leftDoubleClicked(Card card) {
                drawnCard = card;
                discard.remove(drawnCard, true);
                discard.setTouchEnabled(false);
                discard.draw();
            }
        });

        pack.addCardListener(new CardAdapter() {
            @Override
            public void leftDoubleClicked(Card card) {
                drawnCard = card;
                pack.remove(drawnCard, true);
                pack.setTouchEnabled(false);
                pack.draw();
            }
        });
    }
    
    private void sortHand(Hand hand) {
        List<Card> cards = hand.getCardList();
        Comparator<Card> cardComparator = (o1, o2) -> {
            Suit suit1 = (Suit) o1.getSuit();
            Suit suit2 = (Suit) o2.getSuit();
            Rank rank1 = (Rank) o1.getRank();
            Rank rank2 = (Rank) o2.getRank();

            if (suit1.ordinal() - suit2.ordinal() != 0) {
                return suit1.ordinal() - suit2.ordinal();
            }

            return rank1.getShortHandValue() - rank2.getShortHandValue();
        };

        cards.sort(cardComparator);
    }
    
    private void arrangeStockpile() {
        String roundString = "rounds." + roundNumber;
        String stockpileKey = roundString + ".stockpile.cards";
        pack.shuffle(false);
        String topCardsValue = properties.getProperty(stockpileKey);
        if (topCardsValue == null) {
            return;
        }
        String[] topCards = topCardsValue.split(",");
        for (int i = topCards.length - 1; i >= 0; i--) {
            String topCard = topCards[i];
            if (topCard.length() <= 1) {
                continue;
            }
            Card card = getCardFromList(pack.getCardList(), topCard);
            List<Card> cardList = pack.getCardList();
            if (card != null) {
                cardList.remove(card);
                cardList.add(card);
            }
        }
    }
    
    private void dealingOut(Hand[] hands) {
        pack = new Hand(null);
        String roundString = "rounds." + roundNumber;
        for (int i = 0; i < nbPlayers; i++) {
            String initialCardsKey = roundString + ".players." + i + ".initialcards";
            String initialCardsValue = properties.getProperty(initialCardsKey);
            if (initialCardsValue == null) {
                continue;
            }
            String[] initialCards = initialCardsValue.split(",");
            for (String initialCard : initialCards) {
                if (initialCard.length() <= 1) {
                    continue;
                }
                Card card = getCardFromList(pack.getCardList(), initialCard);
                if (card != null) {
                    card.removeFromHand(false);
                    hands[i].insert(card, false);
                }
            }
        }

        for (int i = 0; i < nbPlayers; i++) {
            int cardsToDealt = nbStartCards - hands[i].getNumberOfCards();
            for (int j = 0; j < cardsToDealt; j++) {
                if (pack.isEmpty()) return;
                Card dealt = Rummy.randomCard(pack.getCardList());
                dealt.removeFromHand(false);
                hands[i].insert(dealt, false);
            }
        }
    }
    
    private void setupPlayerAutoMovements() {
        String roundString = "rounds." + roundNumber;
        String player0AutoMovement = properties.getProperty(roundString + ".players.0.cardsPlayed");
        String player1AutoMovement = properties.getProperty(roundString + ".players.1.cardsPlayed");

        String[] playerMovements = new String[]{"", ""};
        if (player0AutoMovement != null) {
            playerMovements[0] = player0AutoMovement;
        }

        if (player1AutoMovement != null) {
            playerMovements[1] = player1AutoMovement;
        }

        for (int i = 0; i < playerMovements.length; i++) {
            String movementString = playerMovements[i];
            List<String> movements = Arrays.asList(movementString.split(","));
            playerAutoMovements.add(movements);
        }
    }
    
    private List<CardAction> getActionFromAutoMovement(String nextMovement) {
        List<CardAction> actions = new ArrayList<>();
        String[] movementComponents = nextMovement.split("-");
        switch (movementComponents.length) {
            case 1:
                actions.add(CardAction.NONE);
                break;
            case 2:
                actions.add(CardAction.valueOf(movementComponents[0]));
                break;
            case 3:
                actions.add(CardAction.valueOf(movementComponents[0]));
                actions.add(CardAction.valueOf(movementComponents[2]));
                break;
        }
        return actions;
    }
    
    private Card getCardElementFromAutoMovement(Hand hand, String nextMovement) {
        String[] movementComponents = nextMovement.split("-");
        switch (movementComponents.length) {
            case 1:
                return getCardFromList(hand.getCardList(), movementComponents[0]);
            case 2:
                return getCardFromList(hand.getCardList(), movementComponents[1]);
            case 3:
                return getCardFromList(hand.getCardList(), movementComponents[1]);
        }
        return null;
    }
    
    private Card getCardFromList(List<Card> cards, String cardName) {
        Rank existingRank = getRankFromString(cardName);
        Suit existingSuit = getSuitFromString(cardName);
        for (Card card : cards) {
            Suit suit = (Suit) card.getSuit();
            Rank rank = (Rank) card.getRank();
            if (suit.getSuitShortHand().equals(existingSuit.getSuitShortHand())
                    && rank.getShortHandValue() == existingRank.getShortHandValue()) {
                return card;
            }
        }
        return null;
    }
    
    private Rank getRankFromString(String cardName) {
        String rankString = cardName.substring(0, cardName.length() - 1);
        Integer rankValue = Integer.parseInt(rankString);

        for (Rank rank : Rank.values()) {
            if (rank.getShortHandValue() == rankValue) {
                return rank;
            }
        }
        return Rank.ACE;
    }
    
    private Suit getSuitFromString(String cardName) {
        String suitString = cardName.substring(cardName.length() - 1, cardName.length());

        for (Suit suit : Suit.values()) {
            if (suit.getSuitShortHand().equals(suitString)) {
                return suit;
            }
        }
        return Suit.CLUBS;
    }
    
    private Card dealTopCard(Hand hand) {
        return hand.getCardList().get(hand.getCardList().size() - 1);
    }
    
    private Card processTopCardFromPile(Hand pile, Hand hand) {
        game.delay(thinkingTime);

        Card card = dealTopCard(pile);
        pile.remove(card, false);
        pile.draw();
        hand.insert(card, false);
        sortHand(hand);
        hand.draw();
        return card;
    }
    
    private void discardCardFromHand(Card card, Hand hand) {
        card.removeFromHand(false);
        discard.insert(card, false);
        discard.draw();
        hand.draw();
    }
    
    private void waitingForHumanToSelectCard(Hand hand) {
        hand.setTouchEnabled(true);
        selected = null;
        while (null == selected) game.delay(delayTime);
        hand.setTouchEnabled(false);
    }
    
    private void drawCardToHand(Hand hand) {
        hand.insert(drawnCard, false);
        sortHand(hand);
        hand.draw();
    }
    
    private void setTouchEnableIfNotNull(Hand hand, boolean isEnabled) {
        if (hand != null) {
            hand.setTouchEnabled(isEnabled);
        }
    }
    
    private void waitingForHumanToSelectPile(Hand pile1, Hand pile2) {
        setTouchEnableIfNotNull(pile1, true);
        setTouchEnableIfNotNull(pile2, true);
        drawnCard = null;
        while (null == drawnCard) game.delay(delayTime);
    }
    
    private void waitingForHumanToEndTurn() {
        endTurnActor.setMouseTouchEnabled(true);
        isEndingTurn = false;
        while (!isEndingTurn) {
            game.delay(delayTime);
        }
        endTurnActor.setMouseTouchEnabled(false);
    }
    
    public Card getRandomCard(Hand hand) {
        game.delay(thinkingTime);
        int x = Rummy.random.nextInt(hand.getCardList().size());
        return hand.getCardList().get(x);
    }
    
    private void processNonAutoPlaying(int nextPlayer, Hand hand) {
        if (HUMAN_PLAYER_INDEX == nextPlayer) {
            if (!discard.isEmpty()) {
                game.setStatus("Player " + nextPlayer + " is playing. Please double click on a pile to draw");
                waitingForHumanToSelectPile(pack, discard);
            } else {
                game.setStatus("Player " + nextPlayer + " is playing first. Please double click on the stockpile to draw");
                waitingForHumanToSelectPile(pack, null);
            }
            drawCardToHand(hand);
            game.setStatus("Player " + nextPlayer + " is playing. Please double click on a card in hand to select");

            waitingForHumanToSelectCard(hand);
            discardCardFromHand(selected, hand);

            game.setStatus("Player " + nextPlayer + " is playing. Please click on end turn or declare rummy/gin/knock");
            waitingForHumanToEndTurn();
            addCardPlayedToLog(nextPlayer, selected, drawnCard, null);
        } else {
            game.setStatusText("Player " + nextPlayer + " thinking...");
            if (!discard.isEmpty()) {
                boolean isPickingDiscard = new Random().nextBoolean();
                if (isPickingDiscard) {
                    game.setStatusText("Player " + nextPlayer + " is picking a card from discard pile...");
                    drawnCard = processTopCardFromPile(discard, hand);
                } else {
                    game.setStatusText("Player " + nextPlayer + " is picking a card from stockpile...");
                    drawnCard = processTopCardFromPile(pack, hands[nextPlayer]);
                }
            } else {
                game.setStatusText("Player " + nextPlayer + " is picking a card from stockpile...");
                drawnCard = processTopCardFromPile(pack, hands[nextPlayer]);
            }
            selected = getRandomCard(hands[nextPlayer]);
            discardCardFromHand(selected, hand);
            addCardPlayedToLog(nextPlayer, selected, drawnCard, null);
        }
    }
    
    // Logging methods
    private void addCardPlayedToLog(int player, Card discardCard, Card pickupCard, String action) {
        logResult.append("P" + player + "-");
        logResult.append(cardDescriptionForLog(pickupCard) + "-");
        logResult.append(cardDescriptionForLog(discardCard));
        if (action != null) {
            logResult.append("-" + action);
        }
        logResult.append(",");
    }
    
    private void addRoundInfoToLog(int roundNumber) {
        logResult.append("\n");
        logResult.append("Round" + roundNumber + ":");
    }
    
    private void addTurnInfoToLog(int turnNumber) {
        logResult.append("\n");
        logResult.append("Turn" + turnNumber + ":");
    }
    
    private void addPlayerCardsToLog() {
        logResult.append("\n");
        logResult.append("Initial Cards:");
        for (int i = 0; i < nbPlayers; i++) {
            logResult.append("P" + i + "-");
            logResult.append(convertCardListoString(hands[i]));
        }
    }
    
    private String convertCardListoString(Hand hand) {
        StringBuilder sb = new StringBuilder();
        sb.append(hand.getCardList().stream().map(card -> {
            Rank rank = (Rank) card.getRank();
            Suit suit = (Suit) card.getSuit();
            return rank.getCardLog() + suit.getSuitShortHand();
        }).collect(Collectors.joining(",")));
        sb.append("-");
        return sb.toString();
    }
    
    private void addEndOfRoundToLog() {
        logResult.append("\n");
        logResult.append("Round" + roundNumber + " End:P0-" + "0" + ",P1-" + "0");
    }
    
    private String cardDescriptionForLog(Card card) {
        Rank cardRank = (Rank) card.getRank();
        Suit cardSuit = (Suit) card.getSuit();
        return cardRank.getCardLog() + cardSuit.getSuitShortHand();
    }
    
    // Getters for access from Rummy class
    public Hand[] getHands() {
        return hands;
    }
    
    public Hand getPack() {
        return pack;
    }
    
    public Hand getDiscard() {
        return discard;
    }
}
