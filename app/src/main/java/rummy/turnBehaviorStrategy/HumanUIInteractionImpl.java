package rummy.turnBehaviorStrategy;

import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;
import rummy.engine.TablePort;
import rummy.engine.RummyGame;
import rummy.meld.MeldFinder;
import rummy.meld.Meld;
import rummy.model.Player;

//HumanUIInteractionImpl - Human player UI interaction implementation class
public class HumanUIInteractionImpl implements HumanUIInteraction {

    public final RummyGame game;
    private final int delayTime = 100;
    private final int maxTimeout = 1000;

    public HumanUIInteractionImpl(RummyGame game) {
        this.game = game;
    }

    @Override
    public Card selectPickupSource(TablePort table) {

        // Enable pile touch
        table.setPilesTouchEnabled(true);

        // Reset state
        game.drawnCardFromPile = null;

        // Wait for player to double-click pile (Stock or Discard)
        int timeoutCounter = 0;
        while (game.drawnCardFromPile == null && timeoutCounter < maxTimeout) {
            delay(delayTime);
            timeoutCounter++;
        }

        // Disable pile touch
        table.setPilesTouchEnabled(false);

        return game.drawnCardFromPile;
    }

    @Override
    public Card selectDiscardCard(Hand hand, Card drawnCard) {

        game.selectedCard = null;


        Player humanPlayer = null;
        for (Player p : game.players) {
            if (p.isHuman()) {
                humanPlayer = p;
                break;
            }
        }

        if (humanPlayer == null) {
            // If no human player found, return null
            return null;
        }

        // Enable hand touch
        game.table.setHandTouchEnabled(humanPlayer, true);

        // Wait for player to double-click a card in hand
        int timeoutCounter = 0;
        while (game.selectedCard == null && timeoutCounter < maxTimeout) {
            delay(delayTime);
            timeoutCounter++;
        }

        // Disable hand touch
        game.table.setHandTouchEnabled(humanPlayer, false);

        return game.selectedCard;
    }

    @Override
    public boolean shouldDeclareRummy(Hand hand) {
        // Check if can declare Rummy
        MeldFinder meldFinder = new MeldFinder();
        Meld meld = meldFinder.detectBestMeldAndDeadwood(hand);

        boolean canDeclare = meld.getDeadwood().isEmpty();

        // Enable buttons
        game.setButtonsEnabled(true);

        // Reset state
        game.isEndingTurn = false;
        game.isDeclaring = false;
        game.declareAction = null;

        // Wait for player to click button (End Turn or Rummy)
        int timeoutCounter = 0;
        while (!game.isEndingTurn && !game.isDeclaring && timeoutCounter < maxTimeout) {
            delay(delayTime);
            timeoutCounter++;
        }

        // Disable buttons
        game.setButtonsEnabled(false);

        // Check if player clicked Rummy button
        if (game.isDeclaring && "RUMMY".equals(game.declareAction)) {
            // Player wants to declare Rummy, check if really possible
            if (canDeclare) {
                return true;
            } else {
                // Player cannot declare Rummy (hand has deadwood)
                game.setStatusText("Cannot declare RUMMY - you have deadwood!");
                return false;
            }
        }

        return false;
    }

    @Override
    public boolean shouldDeclareGin(Hand hand) {


        // Check if can declare Gin
        MeldFinder meldFinder = new MeldFinder();
        Meld meld = meldFinder.detectBestMeldAndDeadwood(hand);

        boolean canDeclare = meld.getDeadwood().isEmpty();

        // Handle together in shouldDeclareKnock
        return false; // Gin check completed in waitForDeclareOrEnd
    }

    @Override
    public boolean shouldDeclareKnock(Hand hand) {
        // Check if can declare Knock (deadwood points <= 7)
        MeldFinder meldFinder = new MeldFinder();
        Meld meld = meldFinder.detectBestMeldAndDeadwood(hand);

        // Calculate deadwood points
        int deadwoodPoints = 0;
        for (Card card : meld.getDeadwood()) {
            rummy.Rank rank = (rummy.Rank) card.getRank();
            deadwoodPoints += rank.getPointValue();
        }

        boolean canKnock = deadwoodPoints <= 7;

        // Handle together in waitForDeclareOrEnd
        return false; // Knock check completed in waitForDeclareOrEnd
    }

    /**
     * Wait for player declaration (Gin Rummy mode)
     * This method uniformly handles GIN/KNOCK/End Turn selection
     */
    public String waitForDeclareOrEnd(Hand hand) {
        // Check if can declare Gin
        MeldFinder meldFinder = new MeldFinder();
        Meld meld = meldFinder.detectBestMeldAndDeadwood(hand);

        boolean canGin = meld.getDeadwood().isEmpty();

        // Calculate deadwood points
        int deadwoodPoints = 0;
        for (Card card : meld.getDeadwood()) {
            rummy.Rank rank = (rummy.Rank) card.getRank();
            deadwoodPoints += rank.getPointValue();
        }

        boolean canKnock = deadwoodPoints <= 7;

        // Enable buttons
        game.setButtonsEnabled(true);

        // Reset state
        game.isEndingTurn = false;
        game.isDeclaring = false;
        game.declareAction = null;

        // Wait for player to click button (End Turn / GIN / KNOCK)
        int timeoutCounter = 0;
        while (!game.isEndingTurn && !game.isDeclaring && timeoutCounter < maxTimeout) {
            delay(delayTime);
            timeoutCounter++;
        }

        // Disable buttons
        game.setButtonsEnabled(false);

        // Check player's choice
        if (game.isDeclaring) {
            if ("GIN".equals(game.declareAction)) {
                if (canGin) {
                    return "GIN";
                } else {
                    game.setStatusText("Cannot declare GIN - you have deadwood!");
                    return null;
                }
            } else if ("KNOCK".equals(game.declareAction)) {
                if (canKnock) {
                    return "KNOCK";
                } else {
                    game.setStatusText("Cannot KNOCK - deadwood > 7!");
                    return null;
                }
            }
        }

        return null; // Player clicked End Turn
    }

    // Helper method: delay
    private void delay(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
